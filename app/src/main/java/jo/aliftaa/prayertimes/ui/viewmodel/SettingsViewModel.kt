package jo.aliftaa.prayertimes.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import jo.aliftaa.prayertimes.data.network.PrayerApiClient
import jo.aliftaa.prayertimes.data.repository.PreferencesRepository
import jo.aliftaa.prayertimes.data.repository.PrayerRepository
import jo.aliftaa.prayertimes.notification.PrayerNotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SettingsUiState(
    val language: String = "ar",
    val theme: String = "emerald_original",
    val darkMode: String = "system",
    val fontScale: String = "default",
    val simpleMode: String = "system",
    val is24Hour: Boolean = false,
    val reminderMinutes: Int = 15,
    val lastSyncFormatted: String = "",
    val isSyncing: Boolean = false,
    val syncSuccess: Boolean? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepo = PreferencesRepository(application)
    private val prayerRepo = PrayerRepository(application, PrayerApiClient(), preferencesRepo)
    private val notificationHelper = PrayerNotificationHelper(application)

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            language = PreferencesRepository.getLanguageSync(application),
            theme = PreferencesRepository.getThemeSync(application),
            darkMode = PreferencesRepository.getDarkModeSync(application),
            fontScale = PreferencesRepository.getFontScaleSync(application),
            simpleMode = PreferencesRepository.getSimpleModeSync(application)
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepo.languageFlow.collect { lang ->
                _uiState.value = _uiState.value.copy(language = lang)
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
            preferencesRepo.fontScaleFlow.collect { scale ->
                _uiState.value = _uiState.value.copy(fontScale = scale)
            }
        }
        viewModelScope.launch {
            preferencesRepo.simpleModeFlow.collect { mode ->
                _uiState.value = _uiState.value.copy(simpleMode = mode)
            }
        }
        viewModelScope.launch {
            preferencesRepo.is24HourFlow.collect { is24 ->
                _uiState.value = _uiState.value.copy(is24Hour = is24)
            }
        }
        viewModelScope.launch {
            preferencesRepo.reminderMinutesFlow.collect { mins ->
                _uiState.value = _uiState.value.copy(reminderMinutes = mins)
            }
        }
        viewModelScope.launch {
            preferencesRepo.lastSyncTimeFlow.collect { timestamp ->
                val formatted = if (timestamp > 0) {
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
                } else {
                    "-"
                }
                _uiState.value = _uiState.value.copy(lastSyncFormatted = formatted)
            }
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch { preferencesRepo.setLanguage(lang) }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch { preferencesRepo.setTheme(theme) }
    }

    fun setDarkMode(mode: String) {
        viewModelScope.launch { preferencesRepo.setDarkMode(mode) }
    }

    fun setFontScale(scale: String) {
        viewModelScope.launch { preferencesRepo.setFontScale(scale) }
    }

    fun setSimpleMode(mode: String) {
        viewModelScope.launch {
            preferencesRepo.setSimpleMode(mode)
            // When simple theme is active, setting its mode also updates darkMode
            if (_uiState.value.theme == "simple") {
                preferencesRepo.setDarkMode(mode)
            }
        }
    }

    fun set24HourFormat(is24Hour: Boolean) {
        viewModelScope.launch { preferencesRepo.set24HourFormat(is24Hour) }
    }

    fun setReminderMinutes(minutes: Int) {
        viewModelScope.launch {
            preferencesRepo.setReminderMinutes(minutes)
            rescheduleAlarms()
        }
    }

    fun setPrayerNotification(prayerId: String, enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepo.setPrayerNotification(prayerId, enabled)
            rescheduleAlarms()
        }
    }

    fun setPrayerSound(prayerId: String, enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepo.setPrayerSound(prayerId, enabled)
            rescheduleAlarms()
        }
    }

    fun setPrayerTrack(prayerId: String, trackId: String) {
        viewModelScope.launch {
            preferencesRepo.setPrayerTrack(prayerId, trackId)
            rescheduleAlarms()
        }
    }

    fun syncNow(onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, syncSuccess = null)
            val result = prayerRepo.getPrayerTimes(forceRefresh = true)
            val success = result.isSuccess
            _uiState.value = _uiState.value.copy(isSyncing = false, syncSuccess = success)
            if (success) {
                result.getOrNull()?.let { feed ->
                    notificationHelper.scheduleAlarms(feed.prayers, preferencesRepo)
                }
            }
            onResult(success)
        }
    }

    private suspend fun rescheduleAlarms() {
        val result = prayerRepo.getPrayerTimes(forceRefresh = false)
        result.getOrNull()?.let { feed ->
            notificationHelper.scheduleAlarms(feed.prayers, preferencesRepo)
        }
    }

    fun getNotificationFlow(prayerId: String) = preferencesRepo.isNotificationEnabledFlow(prayerId)
    fun getSoundFlow(prayerId: String) = preferencesRepo.isSoundEnabledFlow(prayerId)
    fun getTrackFlow(prayerId: String) = preferencesRepo.getTrackFlow(prayerId)
}
