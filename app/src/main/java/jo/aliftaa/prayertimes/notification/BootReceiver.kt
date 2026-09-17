package jo.aliftaa.prayertimes.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import jo.aliftaa.prayertimes.data.network.PrayerApiClient
import jo.aliftaa.prayertimes.data.repository.PreferencesRepository
import jo.aliftaa.prayertimes.data.repository.PrayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED) {

            Log.d("BootReceiver", "Re-scheduling prayer alarms and work on $action")
            SyncScheduler(context).scheduleDailySync()

            CoroutineScope(Dispatchers.IO).launch {
                val prefs = PreferencesRepository(context)
                val repo = PrayerRepository(context, PrayerApiClient(), prefs)
                val result = repo.getPrayerTimes(forceRefresh = false)
                result.getOrNull()?.let { feed ->
                    PrayerNotificationHelper(context).scheduleAlarms(feed.prayers, prefs)
                }
            }
        }
    }
}
