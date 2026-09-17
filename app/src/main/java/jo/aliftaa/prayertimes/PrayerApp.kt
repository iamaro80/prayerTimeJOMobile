package jo.aliftaa.prayertimes

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import jo.aliftaa.prayertimes.data.repository.PreferencesRepository
import java.util.Locale

class PrayerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("PrayerApp", "FATAL CRASH", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    override fun attachBaseContext(base: Context) {
        val lang = try {
            PreferencesRepository.getLanguageSync(base)
        } catch (e: Throwable) {
            Log.w("PrayerApp", "Could not load language preference in attachBaseContext, falling back to 'ar'", e)
            "ar"
        }
        val locale = if (lang == "ar") {
            Locale.forLanguageTag("ar-JO-u-nu-latn")
        } else {
            Locale(lang)
        }
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        val context = base.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
}

