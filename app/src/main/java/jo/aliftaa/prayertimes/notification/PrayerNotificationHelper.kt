package jo.aliftaa.prayertimes.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import jo.aliftaa.prayertimes.MainActivity
import jo.aliftaa.prayertimes.R
import jo.aliftaa.prayertimes.data.model.AzanTrack
import jo.aliftaa.prayertimes.data.model.Prayer
import jo.aliftaa.prayertimes.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar

class PrayerNotificationHelper(private val context: Context) {

    companion object {
        const val TAG = "PrayerNotifHelper"
        const val CHANNEL_PRAYER_PREFIX = "prayer_channel_"
        const val CHANNEL_REMINDER_ID = "prayer_reminder_channel"

        val AVAILABLE_TRACKS = listOf(
            AzanTrack("azan_1", R.raw.azan_1, R.string.track_1),
            AzanTrack("azan_2", R.raw.azan_2, R.string.track_2),
            AzanTrack("azan_3", R.raw.azan_3, R.string.track_3),
            AzanTrack("azan_4", R.raw.azan_4, R.string.track_4),
            AzanTrack("azan_5", R.raw.azan_5, R.string.track_5),
            AzanTrack("azan_6", R.raw.azan_6, R.string.track_6),
            AzanTrack("azan_7", R.raw.azan_7, R.string.track_7),
            AzanTrack("azan_8", R.raw.azan_8, R.string.track_8),
            AzanTrack("azan_9", R.raw.azan_9, R.string.track_9),
            AzanTrack("azan_10", R.raw.azan_10, R.string.track_10),
            AzanTrack("azan_11", R.raw.azan_11, R.string.track_11),
            AzanTrack("azan_12", R.raw.azan_12, R.string.track_12)
        )

        fun getTrackRawResId(trackId: String): Int {
            return AVAILABLE_TRACKS.find { it.id == trackId }?.rawResId ?: R.raw.azan_1
        }
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Reminder channel
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER_ID,
                context.getString(R.string.section_reminder),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pre-prayer reminders"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(reminderChannel)

            // Create channels for each audio track
            for (track in AVAILABLE_TRACKS) {
                val soundUri = Uri.parse("android.resource://${context.packageName}/${track.rawResId}")
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                val channel = NotificationChannel(
                    "$CHANNEL_PRAYER_PREFIX${track.id}",
                    context.getString(track.titleResId),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    setSound(soundUri, audioAttributes)
                    enableVibration(true)
                    description = "Prayer call notification"
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Silent prayer channel
            val silentChannel = NotificationChannel(
                "${CHANNEL_PRAYER_PREFIX}silent",
                "Prayer Silent",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(silentChannel)
        }
    }

    suspend fun scheduleAlarms(prayers: List<Prayer>, preferencesRepo: PreferencesRepository) {
        val now = System.currentTimeMillis()
        val reminderMinutes = try {
            preferencesRepo.reminderMinutesFlow.first()
        } catch (e: Exception) {
            15
        }

        for (prayer in prayers) {
            // RULE: Shurooq is NOT a prayer and must NOT trigger notification
            if (!prayer.isPrayer || prayer.id == "shurooq") continue

            val isNotifEnabled = preferencesRepo.isPrayerNotificationEnabled(prayer.id)
            if (!isNotifEnabled) continue

            val isSoundEnabled = preferencesRepo.isPrayerSoundEnabled(prayer.id)
            val trackId = preferencesRepo.getPrayerTrack(prayer.id)

            val prayerCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, prayer.hour)
                set(Calendar.MINUTE, prayer.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // If time already passed today, schedule for tomorrow
            if (prayerCal.timeInMillis <= now) {
                prayerCal.add(Calendar.DAY_OF_YEAR, 1)
            }

            // 1. Schedule exact prayer alarm
            scheduleExactAlarm(
                timeMs = prayerCal.timeInMillis,
                action = "jo.aliftaa.prayertimes.ACTION_PRAYER_ALARM",
                requestCode = prayer.id.hashCode(),
                prayerId = prayer.id,
                prayerNameAr = prayer.nameAr,
                prayerNameEn = prayer.nameEn,
                soundEnabled = isSoundEnabled,
                trackId = trackId
            )

            // 2. Schedule pre-prayer reminder if enabled
            if (reminderMinutes > 0) {
                val reminderCal = (prayerCal.clone() as Calendar).apply {
                    add(Calendar.MINUTE, -reminderMinutes)
                }

                // Only schedule if reminder is strictly in the future
                if (reminderCal.timeInMillis > now) {
                    scheduleExactAlarm(
                        timeMs = reminderCal.timeInMillis,
                        action = "jo.aliftaa.prayertimes.ACTION_REMINDER_ALARM",
                        requestCode = (prayer.id + "_reminder").hashCode(),
                        prayerId = prayer.id,
                        prayerNameAr = prayer.nameAr,
                        prayerNameEn = prayer.nameEn,
                        soundEnabled = false,
                        trackId = "",
                        reminderMinutes = reminderMinutes
                    )
                }
            }
        }
    }

    private fun scheduleExactAlarm(
        timeMs: Long,
        action: String,
        requestCode: Int,
        prayerId: String,
        prayerNameAr: String,
        prayerNameEn: String,
        soundEnabled: Boolean,
        trackId: String,
        reminderMinutes: Int = 0
    ) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            this.action = action
            putExtra("prayer_id", prayerId)
            putExtra("prayer_name_ar", prayerNameAr)
            putExtra("prayer_name_en", prayerNameEn)
            putExtra("sound_enabled", soundEnabled)
            putExtra("track_id", trackId)
            putExtra("reminder_minutes", reminderMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
            }
            Log.d(TAG, "Scheduled alarm for $prayerId at $timeMs (action: $action)")
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission missing, falling back to set(): ${e.message}")
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
        }
    }

    fun showPrayerNotification(
        prayerName: String,
        soundEnabled: Boolean,
        trackId: String
    ) {
        val channelId = if (soundEnabled && trackId.isNotEmpty()) {
            "$CHANNEL_PRAYER_PREFIX$trackId"
        } else {
            "${CHANNEL_PRAYER_PREFIX}silent"
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(context.getString(R.string.notif_prayer_title, prayerName))
            .setContentText(context.getString(R.string.notif_prayer_body, prayerName))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .build()

        notificationManager.notify(prayerName.hashCode(), notification)
    }

    fun showReminderNotification(prayerName: String, minutes: Int) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(context.getString(R.string.notif_reminder_title, prayerName))
            .setContentText(context.getString(R.string.notif_reminder_body, minutes, prayerName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .build()

        notificationManager.notify((prayerName + "_reminder").hashCode(), notification)
    }
}
