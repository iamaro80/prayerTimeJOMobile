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
                    Spacer(modifier = Modifier.height(12.dp))

                    val themeList = listOf(
                        Triple("emerald_original", stringResource(R.string.theme_emerald_original), androidx.compose.ui.graphics.Color(0xFF10B981)),
                        Triple("soft_terracotta", stringResource(R.string.theme_soft_terracotta), androidx.compose.ui.graphics.Color(0xFFC27854)),
                        Triple("seafoam_sand", stringResource(R.string.theme_seafoam_sand), androidx.compose.ui.graphics.Color(0xFF205D6B)),
                        Triple("muted_slate_gold", stringResource(R.string.theme_muted_slate_gold), androidx.compose.ui.graphics.Color(0xFFD4AF37)),
                        Triple("deep_charcoal_yellow", stringResource(R.string.theme_deep_charcoal_yellow), androidx.compose.ui.graphics.Color(0xFFEAB308)),
                        Triple("simple", stringResource(R.string.theme_simple), androidx.compose.ui.graphics.Color(0xFF2563EB))
                    )

                    // Vertical list with color swatch, title, and radio button
                    themeList.forEach { (key, name, swatchColor) ->
                        val isSelected = (uiState.theme == key) ||
                                (key == "emerald_original" && (uiState.theme == "green" || uiState.theme == "emerald"))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setTheme(key)
                                    if (key == "simple") {
                                        viewModel.setDarkMode(uiState.simpleMode)
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Swatch dot
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier.size(20.dp)
                            ) {
                                drawCircle(color = swatchColor)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setTheme(key)
                                    if (key == "simple") {
                                        viewModel.setDarkMode(uiState.simpleMode)
                                    }
                                }
                            )
                        }
                    }

                    // Display Mode (Follow System, Light, Dark) - only for Simple theme
                    if (uiState.theme == "simple") {
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
                                    selected = uiState.simpleMode == modeKey,
                                    onClick = { viewModel.setSimpleMode(modeKey) },
                                    label = { Text(modeName) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Font Size scaling control
                    Text(
                        text = stringResource(R.string.pref_font_size),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val fontSizes = listOf(
                        "small" to stringResource(R.string.font_size_small),
                        "default" to stringResource(R.string.font_size_default),
                        "medium" to stringResource(R.string.font_size_medium),
                        "large" to stringResource(R.string.font_size_large)
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        fontSizes.forEach { (scaleKey, scaleName) ->
                            FilterChip(
                                selected = uiState.fontScale == scaleKey,
                                onClick = { viewModel.setFontScale(scaleKey) },
                                label = { Text(scaleName) },
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
