package jo.aliftaa.prayertimes.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import jo.aliftaa.prayertimes.data.model.Prayer
import jo.aliftaa.prayertimes.data.model.PrayerFeed
import jo.aliftaa.prayertimes.data.network.PrayerApiClient
import jo.aliftaa.prayertimes.data.repository.PreferencesRepository
import jo.aliftaa.prayertimes.data.repository.PrayerRepository
import jo.aliftaa.prayertimes.notification.PrayerNotificationHelper
import jo.aliftaa.prayertimes.notification.SyncScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

data class PrayerUiState(
    val isLoading: Boolean = true,
    val feed: PrayerFeed? = null,
    val errorMessage: String? = null,
    val isCacheStale: Boolean = false,
    val currentPrayer: Prayer? = null,
    val nextPrayer: Prayer? = null,
    val remainingSeconds: Long = 0L,
    val isArabic: Boolean = true,
    val is24Hour: Boolean = false,
    val theme: String = "green",
    val darkMode: String = "system"
)

class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepo = PreferencesRepository(application)
    private val prayerRepo = PrayerRepository(application, PrayerApiClient(), preferencesRepo)
    private val notificationHelper = PrayerNotificationHelper(application)
    private val syncScheduler = SyncScheduler(application)

    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    init {
        // Schedule daily background sync job at 8:00 AM Jordan
        syncScheduler.scheduleDailySync()

        // Observe settings flows
        viewModelScope.launch {
            preferencesRepo.languageFlow.collect { lang ->
                _uiState.value = _uiState.value.copy(isArabic = (lang == "ar"))
            }
        }
        viewModelScope.launch {
            preferencesRepo.themeFlow.collect { theme ->
                _uiState.value = _uiState.value.copy(theme = theme)
            }
        }
        viewModelScope.launch {
            preferencesRepo.darkModeFlow.collect { mode ->
                _uiState.value = _uiState.value.copy(darkMode = mode)
            }
        }
        viewModelScope.launch {
            preferencesRepo.is24HourFlow.collect { is24 ->
                _uiState.value = _uiState.value.copy(is24Hour = is24)
            }
        }

        // Start downcounter ticker loop (ticks every 1 second)
        viewModelScope.launch {
            while (isActive) {
                updatePrayerTimesState()
                delay(1000)
            }
        }

        // Initial fetch on app start
        loadPrayerTimes(forceRefresh = false)
    }

    fun loadPrayerTimes(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = prayerRepo.getPrayerTimes(forceRefresh)

            if (result.isSuccess) {
                val feed = result.getOrNull()
                val isStale = prayerRepo.isCacheStale()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    feed = feed,
                    isCacheStale = isStale,
                    errorMessage = null
                )
                // Schedule notifications
                feed?.let {
                    notificationHelper.scheduleAlarms(it.prayers, preferencesRepo)
                }
                updatePrayerTimesState()
            } else {
                val isStale = prayerRepo.isCacheStale()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isCacheStale = isStale,
                    errorMessage = result.exceptionOrNull()?.message ?: "Sync failed"
                )
                updatePrayerTimesState()
            }
        }
    }

    /**
     * Determines the current prayer and next prayer according to Islamic convention:
     * - Fajr: Fajr time until Shurooq time.
     * - (Between Shurooq and Dhuhr): No current prayer; next prayer is Dhuhr.
     * - Dhuhr: Dhuhr time until Asr time.
     * - Asr: Asr time until Maghrib time.
     * - Maghrib: Maghrib time until Isha time.
     * - Isha: Isha time until next Fajr (spanning midnight).
     *
     * Shurooq is explicitly excluded from being a prayer or a "next prayer" target for worship.
     */
    private fun updatePrayerTimesState() {
        val feed = _uiState.value.feed ?: return
        val prayers = feed.prayers
        if (prayers.isEmpty()) return

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val currentSeconds = currentMinutes * 60 + now.get(Calendar.SECOND)

        // Find prayers by id
        val fajr = prayers.find { it.id == "fajr" }
        val shurooq = prayers.find { it.id == "shurooq" }
        val dhuhr = prayers.find { it.id == "dhuhr" }
        val asr = prayers.find { it.id == "asr" }
        val maghrib = prayers.find { it.id == "maghrib" }
        val isha = prayers.find { it.id == "isha" }

        val fSec = (fajr?.let { it.hour * 3600 + it.minute * 60 }) ?: 0
        val sSec = (shurooq?.let { it.hour * 3600 + it.minute * 60 }) ?: 0
        val dSec = (dhuhr?.let { it.hour * 3600 + it.minute * 60 }) ?: 0
        val aSec = (asr?.let { it.hour * 3600 + it.minute * 60 }) ?: 0
        val mSec = (maghrib?.let { it.hour * 3600 + it.minute * 60 }) ?: 0
        val iSec = (isha?.let { it.hour * 3600 + it.minute * 60 }) ?: 0

        var currentP: Prayer? = null
        var nextP: Prayer? = null
        var remainingSec = 0L

        when {
            // 1. Before Fajr: Current is previous day's Isha, Next is Fajr
            currentSeconds < fSec -> {
                currentP = isha
                nextP = fajr
                remainingSec = (fSec - currentSeconds).toLong()
            }
            // 2. Fajr until Shurooq: Current is Fajr, Next is Dhuhr (NOT Shurooq!)
            currentSeconds in fSec until sSec -> {
                currentP = fajr
                nextP = dhuhr
                remainingSec = (dSec - currentSeconds).toLong()
            }
            // 3. Shurooq until Dhuhr: Current is null (post-sunrise period), Next is Dhuhr
            currentSeconds in sSec until dSec -> {
                currentP = null
                nextP = dhuhr
                remainingSec = (dSec - currentSeconds).toLong()
            }
            // 4. Dhuhr until Asr: Current is Dhuhr, Next is Asr
            currentSeconds in dSec until aSec -> {
                currentP = dhuhr
                nextP = asr
                remainingSec = (aSec - currentSeconds).toLong()
            }
            // 5. Asr until Maghrib: Current is Asr, Next is Maghrib
            currentSeconds in aSec until mSec -> {
                currentP = asr
                nextP = maghrib
                remainingSec = (mSec - currentSeconds).toLong()
            }
            // 6. Maghrib until Isha: Current is Maghrib, Next is Isha
            currentSeconds in mSec until iSec -> {
                currentP = maghrib
                nextP = isha
                remainingSec = (iSec - currentSeconds).toLong()
            }
            // 7. After Isha until midnight: Current is Isha, Next is tomorrow's Fajr
            else -> {
                currentP = isha
                nextP = fajr
                val secondsUntilMidnight = 86400 - currentSeconds
                remainingSec = (secondsUntilMidnight + fSec).toLong()
            }
        }

        _uiState.value = _uiState.value.copy(
            currentPrayer = currentP,
            nextPrayer = nextP,
            remainingSeconds = remainingSec
        )
    }

    fun getNotificationFlow(prayerId: String) = preferencesRepo.isNotificationEnabledFlow(prayerId)
    fun getSoundFlow(prayerId: String) = preferencesRepo.isSoundEnabledFlow(prayerId)
}
