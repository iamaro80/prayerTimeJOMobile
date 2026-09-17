package jo.aliftaa.prayertimes.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jo.aliftaa.prayertimes.R
import jo.aliftaa.prayertimes.notification.PrayerNotificationHelper
import jo.aliftaa.prayertimes.ui.components.AudioSelectorDialog
import jo.aliftaa.prayertimes.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPrayerForAudio by remember { mutableStateOf<Pair<String, String>?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. General & Language
        item {
            Text(
                text = stringResource(R.string.section_general),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.pref_language),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = uiState.language == "ar",
                            onClick = { viewModel.setLanguage("ar") },
                            label = { Text(stringResource(R.string.language_ar)) },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = uiState.language == "en",
                            onClick = { viewModel.setLanguage("en") },
                            label = { Text(stringResource(R.string.language_en)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = stringResource(R.string.pref_time_format),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = !uiState.is24Hour,
                            onClick = { viewModel.set24HourFormat(false) },
                            label = { Text(stringResource(R.string.format_12h)) },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = uiState.is24Hour,
                            onClick = { viewModel.set24HourFormat(true) },
                            label = { Text(stringResource(R.string.format_24h)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Appearance & Themes (Restyled as FilterChips)
        item {
            Text(
                text = stringResource(R.string.section_appearance),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.pref_theme_color),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val themeOptions = listOf(
                        "emerald_original" to stringResource(R.string.theme_emerald_original),
                        "soft_terracotta" to stringResource(R.string.theme_soft_terracotta),
                        "seafoam_sand" to stringResource(R.string.theme_seafoam_sand),
                        "muted_slate_gold" to stringResource(R.string.theme_muted_slate_gold),
                        "deep_charcoal_yellow" to stringResource(R.string.theme_deep_charcoal_yellow)
                    )

                    // Scrollable row of FilterChips for themes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        themeOptions.forEach { (key, name) ->
                            FilterChip(
                                selected = (uiState.theme == key) || (uiState.theme == "green" && key == "emerald_original") || (uiState.theme == "emerald" && key == "emerald_original"),
                                onClick = { viewModel.setTheme(key) },
                                label = { Text(name) }
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = stringResource(R.string.pref_dark_mode),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val modeOptions = listOf(
                        "system" to stringResource(R.string.mode_system),
                        "light" to stringResource(R.string.mode_light),
                        "dark" to stringResource(R.string.mode_dark)
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        modeOptions.forEach { (modeKey, modeName) ->
                            FilterChip(
                                selected = uiState.darkMode == modeKey,
                                onClick = { viewModel.setDarkMode(modeKey) },
                                label = { Text(modeName) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Pre-Prayer Reminder (Restyled as FilterChips)
        item {
            Text(
                text = stringResource(R.string.section_reminder),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.reminder_title),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.reminder_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val reminderOptions = listOf(
                        0 to stringResource(R.string.reminder_off),
                        5 to stringResource(R.string.reminder_5min),
                        10 to stringResource(R.string.reminder_10min),
                        15 to stringResource(R.string.reminder_15min),
                        45 to stringResource(R.string.reminder_45min),
                        60 to stringResource(R.string.reminder_60min)
                    )

                    // Two scrollable/spaced rows of FilterChips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        reminderOptions.forEach { (minutes, label) ->
                            FilterChip(
                                selected = uiState.reminderMinutes == minutes,
                                onClick = { viewModel.setReminderMinutes(minutes) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Synchronization
        item {
            Text(
                text = stringResource(R.string.section_sync),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.last_updated, uiState.lastSyncFormatted),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.syncNow() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSyncing
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.syncing))
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.sync_now))
                        }
                    }
                }
            }
        }

        // 5. Prayer Notification & Audio Track Settings
        item {
            Text(
                text = stringResource(R.string.section_notifications),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        val prayerList = listOf(
            "fajr" to R.string.prayer_fajr,
            "dhuhr" to R.string.prayer_dhuhr,
            "asr" to R.string.prayer_asr,
            "maghrib" to R.string.prayer_maghrib,
            "isha" to R.string.prayer_isha
        )

        items(prayerList.size) { idx ->
            val (prayerId, prayerNameRes) = prayerList[idx]
            PrayerNotificationSettingCard(
                prayerId = prayerId,
                prayerName = stringResource(prayerNameRes),
                viewModel = viewModel,
                onOpenAudioSelector = { id, name ->
                    selectedPrayerForAudio = id to name
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    selectedPrayerForAudio?.let { (prayerId, prayerName) ->
        val currentTrackFlow = remember(prayerId) { viewModel.getTrackFlow(prayerId) }
        val currentTrack by currentTrackFlow.collectAsState(initial = "azan_1")
        AudioSelectorDialog(
            prayerTitle = prayerName,
            currentTrackId = currentTrack,
            onTrackSelected = { newTrack ->
                viewModel.setPrayerTrack(prayerId, newTrack)
                selectedPrayerForAudio = null
            },
            onDismiss = { selectedPrayerForAudio = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        SettingsContent(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun PrayerNotificationSettingCard(
    prayerId: String,
    prayerName: String,
    viewModel: SettingsViewModel,
    onOpenAudioSelector: (String, String) -> Unit
) {
    val notifFlow = remember(prayerId) { viewModel.getNotificationFlow(prayerId) }
    val soundFlow = remember(prayerId) { viewModel.getSoundFlow(prayerId) }
    val trackFlow = remember(prayerId) { viewModel.getTrackFlow(prayerId) }

    val notifEnabled by notifFlow.collectAsState(initial = true)
    val soundEnabled by soundFlow.collectAsState(initial = true)
    val currentTrack by trackFlow.collectAsState(initial = "azan_1")

    val trackTitleRes = PrayerNotificationHelper.AVAILABLE_TRACKS.find { it.id == currentTrack }?.titleResId
        ?: R.string.track_1
    val trackTitle = stringResource(trackTitleRes)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = prayerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = notifEnabled,
                    onCheckedChange = { viewModel.setPrayerNotification(prayerId, it) }
                )
            }

            if (notifEnabled) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.sound_enabled),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { viewModel.setPrayerSound(prayerId, it) }
                    )
                }

                if (soundEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { onOpenAudioSelector(prayerId, prayerName) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${stringResource(R.string.sound_track)}: $trackTitle",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
