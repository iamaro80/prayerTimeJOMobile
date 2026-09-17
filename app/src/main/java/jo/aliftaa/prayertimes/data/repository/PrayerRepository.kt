package jo.aliftaa.prayertimes.data.repository

import android.content.Context
import android.util.Log
import jo.aliftaa.prayertimes.data.model.PrayerFeed
import jo.aliftaa.prayertimes.data.network.PrayerApiClient
import jo.aliftaa.prayertimes.data.parser.PrayerRssParser
import jo.aliftaa.prayertimes.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Locale

class PrayerRepository(
    private val context: Context,
    private val apiClient: PrayerApiClient,
    private val preferencesRepository: PreferencesRepository,
    private val parser: PrayerRssParser = PrayerRssParser()
) {
    companion object {
        private const val TAG = "PrayerRepository"
        const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L // 24 Hours
    }

    suspend fun getPrayerTimes(forceRefresh: Boolean = false): Result<PrayerFeed> = withContext(Dispatchers.IO) {
        val cachedXml = preferencesRepository.cachedXmlFlow.first()
        val lastSync = preferencesRepository.lastSyncTimeFlow.first()
        val isCacheValid = cachedXml != null && (System.currentTimeMillis() - lastSync < CACHE_TTL_MS)

        val lang = preferencesRepository.languageFlow.first()
        val locale = if (lang == "ar") Locale.forLanguageTag("ar-JO-u-nu-latn") else Locale(lang)
        val hijriDate = DateUtils.getFormattedHijriDate(locale)

        // If not forcing refresh and cache is valid, try returning cache first
        if (!forceRefresh && isCacheValid && cachedXml != null) {
            try {
                val feed = parser.parse(cachedXml, hijriDate)
                return@withContext Result.success(feed)
            } catch (e: Exception) {
                Log.w(TAG, "Cached XML parsing failed, will attempt network fetch", e)
            }
        }

        // Fetch from network
        try {
            val xml = apiClient.fetchPrayerFeed()
            val feed = parser.parse(xml, hijriDate)
            preferencesRepository.saveCachedFeed(xml, System.currentTimeMillis())
            Result.success(feed)
        } catch (netEx: Exception) {
            Log.e(TAG, "Network fetch failed: ${netEx.message}", netEx)
            // Fallback to cache even if older than 24h
            if (cachedXml != null) {
                try {
                    val feed = parser.parse(cachedXml, hijriDate)
                    return@withContext Result.success(feed)
                } catch (e: Exception) {
                    // ignore
                }
            }
            Result.failure(netEx)
        }
    }

    suspend fun isCacheStale(): Boolean {
        val lastSync = preferencesRepository.lastSyncTimeFlow.first()
        if (lastSync == 0L) return true
        return (System.currentTimeMillis() - lastSync) > CACHE_TTL_MS
    }
}
