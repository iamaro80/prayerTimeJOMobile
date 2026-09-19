package jo.aliftaa.prayertimes.notification

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import jo.aliftaa.prayertimes.data.network.PrayerApiClient
import jo.aliftaa.prayertimes.data.repository.PreferencesRepository
import jo.aliftaa.prayertimes.data.repository.PrayerRepository
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class SyncScheduler(private val context: Context) {

    companion object {
        const val WORK_NAME = "daily_prayer_sync_8am"
        private const val TAG = "SyncScheduler"
    }

    fun scheduleDailySync() {
        val initialDelay = calculateDelayTo8AMJordan()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<PrayerSyncWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )

        Log.d(TAG, "Scheduled 8:00 AM Jordan sync with delay ${initialDelay / 1000 / 60} minutes")
    }

    private fun calculateDelayTo8AMJordan(): Long {
        val jordanTz = TimeZone.getTimeZone("Asia/Amman")
        val now = Calendar.getInstance(jordanTz)

        val target = Calendar.getInstance(jordanTz).apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (now.after(target)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }
}

class PrayerSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = PreferencesRepository(context)
            val repo = PrayerRepository(context, PrayerApiClient(), prefs)
            val result = repo.getPrayerTimes(forceRefresh = true)

            if (result.isSuccess) {
                val feed = result.getOrNull()
                if (feed != null) {
                    PrayerNotificationHelper(context).scheduleAlarms(feed.prayers, prefs)
                    try {
                        jo.aliftaa.prayertimes.widget.PrayerWidgetProvider.triggerUpdate(context)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
