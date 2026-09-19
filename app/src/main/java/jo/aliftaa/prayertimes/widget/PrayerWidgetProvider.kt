package jo.aliftaa.prayertimes.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import jo.aliftaa.prayertimes.MainActivity
import jo.aliftaa.prayertimes.R
import jo.aliftaa.prayertimes.data.model.Prayer
import jo.aliftaa.prayertimes.data.parser.PrayerRssParser
import jo.aliftaa.prayertimes.data.repository.PreferencesRepository
import jo.aliftaa.prayertimes.utils.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Calendar
import java.util.Locale

class PrayerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(4000L) {
                    updateWidgets(context, appWidgetManager, appWidgetIds)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, PrayerWidgetProvider::class.java)
            )
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeoutOrNull(4000L) {
                        updateWidgets(context, appWidgetManager, appWidgetIds)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "jo.aliftaa.prayertimes.ACTION_UPDATE_WIDGET"

        fun triggerUpdate(context: Context) {
            val intent = Intent(context, PrayerWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }

        suspend fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val preferencesRepo = PreferencesRepository(context)
            val cachedXml = preferencesRepo.cachedXmlFlow.first()
            val lang = PreferencesRepository.getLanguageSync(context)
            val isArabic = lang == "ar"
            val isFriday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY

            val prayers = if (cachedXml != null) {
                try {
                    val parser = PrayerRssParser()
                    val locale = if (isArabic) Locale.forLanguageTag("ar-JO-u-nu-latn") else Locale(lang)
                    val hijriDate = DateUtils.getFormattedHijriDate(locale)
                    parser.parse(cachedXml, hijriDate).prayers
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }

            var currentPrayerName = "--"
            var nextPrayerName = "--"
            var countdownText = "--:--"

            if (prayers.isNotEmpty()) {
                val now = Calendar.getInstance()
                val currentSeconds = (now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)) * 60 + now.get(Calendar.SECOND)

                val fajr = prayers.find { it.id == "fajr" }
                val shurooq = prayers.find { it.id == "shurooq" }
                val dhuhr = prayers.find { it.id == "dhuhr" }
                val asr = prayers.find { it.id == "asr" }
                val maghrib = prayers.find { it.id == "maghrib" }
                val isha = prayers.find { it.id == "isha" }

                val fSec = fajr?.let { it.hour * 3600 + it.minute * 60 } ?: 0
                val sSec = shurooq?.let { it.hour * 3600 + it.minute * 60 } ?: 0
                val dSec = dhuhr?.let { it.hour * 3600 + it.minute * 60 } ?: 0
                val aSec = asr?.let { it.hour * 3600 + it.minute * 60 } ?: 0
                val mSec = maghrib?.let { it.hour * 3600 + it.minute * 60 } ?: 0
                val iSec = isha?.let { it.hour * 3600 + it.minute * 60 } ?: 0

                var currentP: Prayer? = null
                var nextP: Prayer? = null
                var remainingSec = 0L

                when {
                    currentSeconds < fSec -> {
                        currentP = isha
                        nextP = fajr
                        remainingSec = (fSec - currentSeconds).toLong()
                    }
                    currentSeconds in fSec until sSec -> {
                        currentP = fajr
                        nextP = dhuhr
                        remainingSec = (dSec - currentSeconds).toLong()
                    }
                    currentSeconds in sSec until dSec -> {
                        currentP = null
                        nextP = dhuhr
                        remainingSec = (dSec - currentSeconds).toLong()
                    }
                    currentSeconds in dSec until aSec -> {
                        currentP = dhuhr
                        nextP = asr
                        remainingSec = (aSec - currentSeconds).toLong()
                    }
                    currentSeconds in aSec until mSec -> {
                        currentP = asr
                        nextP = maghrib
                        remainingSec = (mSec - currentSeconds).toLong()
                    }
                    currentSeconds in mSec until iSec -> {
                        currentP = maghrib
                        nextP = isha
                        remainingSec = (iSec - currentSeconds).toLong()
                    }
                    else -> {
                        currentP = isha
                        nextP = fajr
                        remainingSec = ((86400 - currentSeconds) + fSec).toLong()
                    }
                }

                fun formatPrayerName(p: Prayer?): String {
                    if (p == null) return "--"
                    if (isFriday && p.id.equals("dhuhr", ignoreCase = true)) {
                        return context.getString(R.string.prayer_jumuah)
                    }
                    return if (isArabic) p.nameAr else p.nameEn
                }

                currentPrayerName = currentP?.let { formatPrayerName(it) } ?: "--"
                nextPrayerName = nextP?.let { formatPrayerName(it) } ?: "--"

                val hours = remainingSec / 3600
                val minutes = (remainingSec % 3600) / 60
                countdownText = String.format(Locale.US, "%02d:%02d", hours, minutes)
            }

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.prayer_app_widget).apply {
                    setTextViewText(R.id.widget_title, context.getString(R.string.app_name))
                    setTextViewText(R.id.widget_location, context.getString(R.string.location_title))
                    setTextViewText(R.id.widget_current_label, context.getString(R.string.current_prayer))
                    setTextViewText(R.id.widget_current_prayer, currentPrayerName)
                    setTextViewText(R.id.widget_next_label, context.getString(R.string.next_prayer))
                    setTextViewText(R.id.widget_next_prayer, nextPrayerName)
                    setTextViewText(R.id.widget_time_label, context.getString(R.string.time_remaining))
                    setTextViewText(R.id.widget_countdown, countdownText)

                    // Launch MainActivity on widget tap
                    val intent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
