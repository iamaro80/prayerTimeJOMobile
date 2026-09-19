package jo.aliftaa.prayertimes.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "prayer_settings")

class PreferencesRepository(private val context: Context) {

    companion object {
        val KEY_LANGUAGE = stringPreferencesKey("app_language") // "ar" or "en"
        val KEY_THEME = stringPreferencesKey("app_theme")       // "green", "emerald", "blue", "gold", "teal"
        val KEY_DARK_MODE = stringPreferencesKey("dark_mode")   // "system", "light", "dark"
        val KEY_TIME_FORMAT_24 = booleanPreferencesKey("time_format_24") // false = 12h, true = 24h

        // Per-prayer notification enabled
        val KEY_NOTIF_FAJR = booleanPreferencesKey("notif_fajr")
        val KEY_NOTIF_DHUHR = booleanPreferencesKey("notif_dhuhr")
        val KEY_NOTIF_ASR = booleanPreferencesKey("notif_asr")
        val KEY_NOTIF_MAGHRIB = booleanPreferencesKey("notif_maghrib")
        val KEY_NOTIF_ISHA = booleanPreferencesKey("notif_isha")

        // Per-prayer sound enabled
        val KEY_SOUND_FAJR = booleanPreferencesKey("sound_fajr")
        val KEY_SOUND_DHUHR = booleanPreferencesKey("sound_dhuhr")
        val KEY_SOUND_ASR = booleanPreferencesKey("sound_asr")
        val KEY_SOUND_MAGHRIB = booleanPreferencesKey("sound_maghrib")
        val KEY_SOUND_ISHA = booleanPreferencesKey("sound_isha")

        // Per-prayer selected audio track
        val KEY_TRACK_FAJR = stringPreferencesKey("track_fajr")
        val KEY_TRACK_DHUHR = stringPreferencesKey("track_dhuhr")
        val KEY_TRACK_ASR = stringPreferencesKey("track_asr")
        val KEY_TRACK_MAGHRIB = stringPreferencesKey("track_maghrib")
        val KEY_TRACK_ISHA = stringPreferencesKey("track_isha")

        // Pre-prayer reminder duration (minutes before: 0 = off, 5, 10, 15, 45, 60)
        val KEY_REMINDER_MINUTES = intPreferencesKey("reminder_minutes")

        // Cache
        val KEY_CACHED_XML = stringPreferencesKey("cached_xml_feed")
        val KEY_LAST_SYNC_TIME = longPreferencesKey("last_sync_timestamp")

        private const val EARLY_PREFS_NAME = "prayer_early_prefs"
        private const val KEY_EARLY_LANGUAGE = "app_language"
        private const val KEY_EARLY_THEME = "app_theme"
        private const val KEY_EARLY_DARK_MODE = "dark_mode"
        private const val KEY_EARLY_FONT_SCALE = "font_scale"
        private const val KEY_EARLY_SIMPLE_MODE = "simple_mode"

        val KEY_FONT_SCALE = stringPreferencesKey("font_scale") // "small", "default", "medium", "large"
        val KEY_SIMPLE_MODE = stringPreferencesKey("simple_mode") // "system", "light", "dark"

        fun getLanguageSync(context: Context): String {
            val prefs = context.getSharedPreferences(EARLY_PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_EARLY_LANGUAGE, "ar") ?: "ar"
        }

        fun setLanguageSync(context: Context, language: String) {
            context.getSharedPreferences(EARLY_PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_EARLY_LANGUAGE, language)
                .apply()
        }

        fun getThemeSync(context: Context): String {
            val prefs = context.getSharedPreferences(EARLY_PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_EARLY_THEME, "emerald_original") ?: "emerald_original"
        }

        fun setThemeSync(context: Context, theme: String) {
            context.getSharedPreferences(EARLY_PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_EARLY_THEME, theme)
                .apply()
        }

        fun getDarkModeSync(context: Context): String {
            val prefs = context.getSharedPreferences(EARLY_PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_EARLY_DARK_MODE, "system") ?: "system"
        }

        fun setDarkModeSync(context: Context, mode: String) {
            context.getSharedPreferences(EARLY_PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_EARLY_DARK_MODE, mode)
                .apply()
        }

        fun getFontScaleSync(context: Context): String {
            val prefs = context.getSharedPreferences(EARLY_PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_EARLY_FONT_SCALE, "default") ?: "default"
        }

        fun setFontScaleSync(context: Context, scale: String) {
            context.getSharedPreferences(EARLY_PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_EARLY_FONT_SCALE, scale)
                .apply()
        }

        fun getSimpleModeSync(context: Context): String {
            val prefs = context.getSharedPreferences(EARLY_PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_EARLY_SIMPLE_MODE, "system") ?: "system"
        }

        fun setSimpleModeSync(context: Context, mode: String) {
            context.getSharedPreferences(EARLY_PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_EARLY_SIMPLE_MODE, mode)
                .apply()
        }
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { it[KEY_LANGUAGE] ?: "ar" }
    val themeFlow: Flow<String> = context.dataStore.data.map { it[KEY_THEME] ?: "emerald_original" }
    val darkModeFlow: Flow<String> = context.dataStore.data.map { it[KEY_DARK_MODE] ?: "system" }
    val fontScaleFlow: Flow<String> = context.dataStore.data.map { it[KEY_FONT_SCALE] ?: "default" }
    val simpleModeFlow: Flow<String> = context.dataStore.data.map { it[KEY_SIMPLE_MODE] ?: "system" }
    val is24HourFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_TIME_FORMAT_24] ?: false }
    val reminderMinutesFlow: Flow<Int> = context.dataStore.data.map { it[KEY_REMINDER_MINUTES] ?: 15 }
    val lastSyncTimeFlow: Flow<Long> = context.dataStore.data.map { it[KEY_LAST_SYNC_TIME] ?: 0L }
    val cachedXmlFlow: Flow<String?> = context.dataStore.data.map { it[KEY_CACHED_XML] }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = language }
        setLanguageSync(context, language)
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
        setThemeSync(context, theme)
    }

    suspend fun setDarkMode(mode: String) {
        context.dataStore.edit { it[KEY_DARK_MODE] = mode }
        setDarkModeSync(context, mode)
    }

    suspend fun setFontScale(scale: String) {
        context.dataStore.edit { it[KEY_FONT_SCALE] = scale }
        setFontScaleSync(context, scale)
    }

    suspend fun setSimpleMode(mode: String) {
        context.dataStore.edit { it[KEY_SIMPLE_MODE] = mode }
        setSimpleModeSync(context, mode)
    }

    suspend fun set24HourFormat(is24Hour: Boolean) {
        context.dataStore.edit { it[KEY_TIME_FORMAT_24] = is24Hour }
    }

    suspend fun setReminderMinutes(minutes: Int) {
        context.dataStore.edit { it[KEY_REMINDER_MINUTES] = minutes }
    }

    suspend fun setPrayerNotification(prayerId: String, enabled: Boolean) {
        val key = when (prayerId.lowercase()) {
            "fajr" -> KEY_NOTIF_FAJR
            "dhuhr" -> KEY_NOTIF_DHUHR
            "asr" -> KEY_NOTIF_ASR
            "maghrib" -> KEY_NOTIF_MAGHRIB
            "isha" -> KEY_NOTIF_ISHA
            else -> return
        }
        context.dataStore.edit { it[key] = enabled }
    }

    suspend fun isPrayerNotificationEnabled(prayerId: String): Boolean {
        val prefs = context.dataStore.data.first()
        val key = when (prayerId.lowercase()) {
            "fajr" -> KEY_NOTIF_FAJR
            "dhuhr" -> KEY_NOTIF_DHUHR
            "asr" -> KEY_NOTIF_ASR
            "maghrib" -> KEY_NOTIF_MAGHRIB
            "isha" -> KEY_NOTIF_ISHA
            else -> return false
        }
        return prefs[key] ?: true
    }

    fun isNotificationEnabledFlow(prayerId: String): Flow<Boolean> {
        val key = when (prayerId.lowercase()) {
            "fajr" -> KEY_NOTIF_FAJR
            "dhuhr" -> KEY_NOTIF_DHUHR
            "asr" -> KEY_NOTIF_ASR
            "maghrib" -> KEY_NOTIF_MAGHRIB
            "isha" -> KEY_NOTIF_ISHA
            else -> KEY_NOTIF_FAJR
        }
        return context.dataStore.data.map { it[key] ?: true }
    }

    suspend fun setPrayerSound(prayerId: String, enabled: Boolean) {
        val key = when (prayerId.lowercase()) {
            "fajr" -> KEY_SOUND_FAJR
            "dhuhr" -> KEY_SOUND_DHUHR
            "asr" -> KEY_SOUND_ASR
            "maghrib" -> KEY_SOUND_MAGHRIB
            "isha" -> KEY_SOUND_ISHA
            else -> return
        }
        context.dataStore.edit { it[key] = enabled }
    }

    suspend fun isPrayerSoundEnabled(prayerId: String): Boolean {
        val prefs = context.dataStore.data.first()
        val key = when (prayerId.lowercase()) {
            "fajr" -> KEY_SOUND_FAJR
            "dhuhr" -> KEY_SOUND_DHUHR
            "asr" -> KEY_SOUND_ASR
            "maghrib" -> KEY_SOUND_MAGHRIB
            "isha" -> KEY_SOUND_ISHA
            else -> return true
        }
        return prefs[key] ?: true
    }

    fun isSoundEnabledFlow(prayerId: String): Flow<Boolean> {
        val key = when (prayerId.lowercase()) {
            "fajr" -> KEY_SOUND_FAJR
            "dhuhr" -> KEY_SOUND_DHUHR
            "asr" -> KEY_SOUND_ASR
            "maghrib" -> KEY_SOUND_MAGHRIB
            "isha" -> KEY_SOUND_ISHA
            else -> KEY_SOUND_FAJR
        }
        return context.dataStore.data.map { it[key] ?: true }
    }

    suspend fun setPrayerTrack(prayerId: String, trackId: String) {
        val key = when (prayerId.lowercase()) {
            "fajr" -> KEY_TRACK_FAJR
            "dhuhr" -> KEY_TRACK_DHUHR
            "asr" -> KEY_TRACK_ASR
            "maghrib" -> KEY_TRACK_MAGHRIB
            "isha" -> KEY_TRACK_ISHA
            else -> return
        }
        context.dataStore.edit { it[key] = trackId }
    }

    suspend fun getPrayerTrack(prayerId: String): String {
        val prefs = context.dataStore.data.first()
        val key = when (prayerId.lowercase()) {
            "fajr" -> KEY_TRACK_FAJR
            "dhuhr" -> KEY_TRACK_DHUHR
            "asr" -> KEY_TRACK_ASR
            "maghrib" -> KEY_TRACK_MAGHRIB
            "isha" -> KEY_TRACK_ISHA
            else -> return "azan_1"
        }
        return prefs[key] ?: "azan_1"
    }

    fun getTrackFlow(prayerId: String): Flow<String> {
        val key = when (prayerId.lowercase()) {
            "fajr" -> KEY_TRACK_FAJR
            "dhuhr" -> KEY_TRACK_DHUHR
            "asr" -> KEY_TRACK_ASR
            "maghrib" -> KEY_TRACK_MAGHRIB
            "isha" -> KEY_TRACK_ISHA
            else -> KEY_TRACK_FAJR
        }
        return context.dataStore.data.map { it[key] ?: "azan_1" }
    }

    suspend fun saveCachedFeed(xml: String, timestamp: Long) {
        context.dataStore.edit {
            it[KEY_CACHED_XML] = xml
            it[KEY_LAST_SYNC_TIME] = timestamp
        }
    }
}
