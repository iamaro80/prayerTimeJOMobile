package jo.aliftaa.prayertimes.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jo.aliftaa.prayertimes.R
import jo.aliftaa.prayertimes.ui.components.CountdownTimerCard
import jo.aliftaa.prayertimes.ui.components.PrayerItemCard
import jo.aliftaa.prayertimes.ui.viewmodel.PrayerViewModel
import jo.aliftaa.prayertimes.utils.DateUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PrayerViewModel,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val locale = if (uiState.isArabic) Locale.forLanguageTag("ar-JO-u-nu-latn") else Locale.ENGLISH

    val gregorianDate = remember(uiState.isArabic) { DateUtils.getFormattedGregorianDate(locale) }
    val hijriDate = uiState.feed?.hijriFormatted.takeIf { !it.isNullOrBlank() }
        ?: remember(uiState.isArabic) { DateUtils.getFormattedHijriDate(locale) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.location_title),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadPrayerTimes(forceRefresh = true) }) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.sync_now)
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Permission warning banner if permission is missing on Android 13+
            if (!hasNotificationPermission) {
                item {
                    jo.aliftaa.prayertimes.ui.components.ExactAlarmWarningBanner(
                        onClick = onRequestNotificationPermission
                    )
                }
            }

            // Stale cache indicator banner
            if (uiState.isCacheStale) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.loadPrayerTimes(forceRefresh = true) }
                    ) {
                        Text(
                            text = stringResource(R.string.stale_cache_warning),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // Date Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = gregorianDate,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (hijriDate.isNotEmpty()) {
                            Text(
                                text = hijriDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Real-Time Countdown Downcounter Card
            item {
                CountdownTimerCard(
                    nextPrayer = uiState.nextPrayer,
                    currentPrayer = uiState.currentPrayer,
                    remainingSeconds = uiState.remainingSeconds,
                    isArabic = uiState.isArabic
                )
            }

            // List of prayer times
            val prayers = uiState.feed?.prayers ?: emptyList()
            items(prayers) { prayer ->
                val notifEnabled by viewModel.getNotificationFlow(prayer.id).collectAsState(initial = true)
                val soundEnabled by viewModel.getSoundFlow(prayer.id).collectAsState(initial = true)

                PrayerItemCard(
                    prayer = prayer,
                    isCurrent = (uiState.currentPrayer?.id == prayer.id),
                    isNext = (uiState.nextPrayer?.id == prayer.id),
                    isArabic = uiState.isArabic,
                    is24Hour = uiState.is24Hour,
                    notifEnabled = notifEnabled,
                    soundEnabled = soundEnabled
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
