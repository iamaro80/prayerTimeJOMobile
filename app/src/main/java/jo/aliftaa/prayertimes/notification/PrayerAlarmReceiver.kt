package jo.aliftaa.prayertimes.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PrayerAlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "PrayerAlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val prayerId = intent.getStringExtra("prayer_id") ?: ""
        val prayerNameAr = intent.getStringExtra("prayer_name_ar") ?: ""
        val prayerNameEn = intent.getStringExtra("prayer_name_en") ?: ""
        val soundEnabled = intent.getBooleanExtra("sound_enabled", true)
        val trackId = intent.getStringExtra("track_id") ?: "azan_1"
        val reminderMinutes = intent.getIntExtra("reminder_minutes", 15)

        // Shurooq is NOT a prayer
        if (prayerId.lowercase() == "shurooq") {
            return
        }

        val helper = PrayerNotificationHelper(context)

        when (action) {
            "jo.aliftaa.prayertimes.ACTION_PRAYER_ALARM" -> {
                Log.d(TAG, "Triggering prayer notification for $prayerId")
                helper.showPrayerNotification(prayerNameAr, soundEnabled, trackId)
            }
            "jo.aliftaa.prayertimes.ACTION_REMINDER_ALARM" -> {
                Log.d(TAG, "Triggering reminder notification for $prayerId ($reminderMinutes mins)")
                helper.showReminderNotification(prayerNameAr, reminderMinutes)
            }
        }
    }
}
