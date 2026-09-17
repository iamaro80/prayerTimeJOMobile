package jo.aliftaa.prayertimes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jo.aliftaa.prayertimes.R
import jo.aliftaa.prayertimes.data.model.Prayer
import jo.aliftaa.prayertimes.ui.theme.LocalCustomThemeTokens
import jo.aliftaa.prayertimes.ui.theme.md3Tween
import jo.aliftaa.prayertimes.utils.DateUtils
import java.util.Locale

@Composable
fun CountdownTimerCard(
    nextPrayer: Prayer?,
    currentPrayer: Prayer?,
    remainingSeconds: Long,
    totalSecondsForProgress: Long = 3600L * 4,
    isArabic: Boolean
) {
    val prayerName = if (nextPrayer != null) {
        if (isArabic) nextPrayer.nameAr else nextPrayer.nameEn
    } else ""

    val currentName = if (currentPrayer != null) {
        if (isArabic) currentPrayer.nameAr else currentPrayer.nameEn
    } else ""

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60

    val customTokens = LocalCustomThemeTokens.current
    val heroGradient = Brush.linearGradient(
        colors = listOf(customTokens.heroGradientStart, customTokens.heroGradientEnd)
    )

    // Progress 0f..1f (remaining vs window)
    val progress = if (totalSecondsForProgress > 0) {
        (remainingSeconds.toFloat() / totalSecondsForProgress.toFloat()).coerceIn(0.05f, 1f)
    } else 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(heroGradient)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left column: Current Prayer (badge), Next Prayer Name, Countdown text
                Column(modifier = Modifier.weight(1f)) {
                    if (currentName.isNotEmpty()) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = customTokens.heroAccentContainer,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "${stringResource(R.string.current_prayer)}: $currentName",
                                style = MaterialTheme.typography.labelSmall,
                                color = customTokens.onHeroAccentContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "${stringResource(R.string.next_prayer)}: $prayerName",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // HH:MM:SS Countdown display (monospaced Latin digits)
                    val timeString = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
                    Text(
                        text = timeString,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

 Spacer(modifier = Modifier.width(16.dp))

 // Circular Progress Indicator Ring with glyph inside
 Box(
 contentAlignment = Alignment.Center,
 modifier = Modifier.size(90.dp)
 ) {
 CircularProgressIndicator(
 progress = { progress },
 modifier = Modifier.fillMaxSize(),
 color = customTokens.countdownRingProgress,
 trackColor = customTokens.countdownRingTrack,
 strokeWidth = 7.dp,
 strokeCap = StrokeCap.Round
 )
 val iconGlyph = when (nextPrayer?.id?.lowercase()) {
 "fajr" -> FaIcons.CloudMoon
 "shurooq" -> FaIcons.Sun
 "dhuhr" -> FaIcons.Sun
 "asr" -> FaIcons.CloudSun
 "maghrib" -> FaIcons.Moon
 "isha" -> FaIcons.StarAndCrescent
 else -> FaIcons.StarAndCrescent
 }
 FaIcon(
 glyph = iconGlyph,
 size = 50.dp,
 iconSize = 22.sp,
 tint = customTokens.countdownRingProgress,
 containerColor = Color.Transparent
 )
 }
 }
 }
 }
}

@Composable
fun PrayerItemCard(
 prayer: Prayer,
 isCurrent: Boolean,
 isNext: Boolean,
 isArabic: Boolean,
 is24Hour: Boolean,
 notifEnabled: Boolean,
 soundEnabled: Boolean
) {
 val displayName = if (isArabic) prayer.nameAr else prayer.nameEn
 val displayTime = DateUtils.formatTimeDisplay(prayer.time24, is24Hour, isArabic)
 val customTokens = LocalCustomThemeTokens.current

 val animBorderColor by animateColorAsState(
 targetValue = if (isNext) customTokens.activeCardOutline else Color.Transparent,
 animationSpec = md3Tween(),
 label = "borderColor"
 )

 val iconGlyph = when (prayer.id.lowercase()) {
 "fajr" -> FaIcons.CloudMoon
 "shurooq" -> FaIcons.Sun
 "dhuhr" -> FaIcons.Sun
 "asr" -> FaIcons.CloudSun
 "maghrib" -> FaIcons.Moon
 "isha" -> FaIcons.StarAndCrescent
 else -> FaIcons.StarAndCrescent
 }

 Card(
 modifier = Modifier
 .fillMaxWidth()
 .padding(vertical = 4.dp),
 shape = MaterialTheme.shapes.medium,
 colors = CardDefaults.cardColors(
 containerColor = MaterialTheme.colorScheme.surfaceContainer
 ),
 border = if (isNext) BorderStroke(2.dp, animBorderColor) else null,
 elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 3.dp else 0.dp)
 ) {
 Row(
 modifier = Modifier
 .fillMaxWidth()
 .padding(horizontal = 16.dp, vertical = 12.dp),
 verticalAlignment = Alignment.CenterVertically,
 horizontalArrangement = Arrangement.SpaceBetween
 ) {
 Row(verticalAlignment = Alignment.CenterVertically) {
 FaIcon(
 glyph = iconGlyph,
 size = 44.dp,
 iconSize = 18.sp,
 tint = customTokens.onHeroAccentContainer,
 containerColor = customTokens.heroAccentContainer,
 shape = MaterialTheme.shapes.small
 )
 Spacer(modifier = Modifier.width(12.dp))
 Column {
 Row(verticalAlignment = Alignment.CenterVertically) {
 Text(
 text = displayName,
 style = MaterialTheme.typography.titleMedium,
 fontWeight = if (isNext || isCurrent) FontWeight.Bold else FontWeight.SemiBold,
 color = MaterialTheme.colorScheme.onSurface
 )

 if (isNext) {
 Spacer(modifier = Modifier.width(6.dp))
 Surface(
 shape = MaterialTheme.shapes.small,
 color = MaterialTheme.colorScheme.primary
 ) {
 Text(
 text = stringResource(R.string.next_prayer),
 style = MaterialTheme.typography.labelSmall,
 modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
 color = MaterialTheme.colorScheme.onPrimary,
 fontWeight = FontWeight.Bold
 )
 }
 } else if (isCurrent) {
 Spacer(modifier = Modifier.width(6.dp))
 Surface(
 shape = MaterialTheme.shapes.small,
 color = customTokens.heroAccentContainer
 ) {
 Text(
 text = stringResource(R.string.current_prayer),
 style = MaterialTheme.typography.labelSmall,
 modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
 color = customTokens.onHeroAccentContainer,
 fontWeight = FontWeight.Bold
 )
 }
 }
 }
 }
 }

 Row(verticalAlignment = Alignment.CenterVertically) {
 if (prayer.isPrayer) {
 if (notifEnabled) {
 Icon(
 imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
 contentDescription = null,
 modifier = Modifier.size(18.dp),
 tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
 )
 } else {
 Icon(
 imageVector = Icons.Default.NotificationsOff,
 contentDescription = null,
 modifier = Modifier.size(18.dp),
 tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
 )
 }
 Spacer(modifier = Modifier.width(10.dp))
 }

 Text(
 text = displayTime,
 fontFamily = FontFamily.Monospace,
 style = MaterialTheme.typography.titleMedium,
 fontWeight = FontWeight.Bold,
 color = if (isNext) customTokens.activeCardOutline else MaterialTheme.colorScheme.onSurface
 )
 }
 }
 }
}

// 7. MD3 Warning Banners
@Composable
fun ExactAlarmWarningBanner(
 onClick: () -> Unit,
 modifier: Modifier = Modifier
) {
 Card(
 modifier = modifier
 .fillMaxWidth()
 .padding(vertical = 4.dp)
 .clickable(onClick = onClick),
 shape = MaterialTheme.shapes.medium,
 colors = CardDefaults.cardColors(
 containerColor = Color(0x66451A03) // amber-950/40
 ),
 border = BorderStroke(1.dp, Color(0x6692400E)) // amber-800/40
 ) {
 Row(
 modifier = Modifier
 .fillMaxWidth()
 .padding(14.dp),
 verticalAlignment = Alignment.CenterVertically
 ) {
 FaIcon(
 glyph = FaIcons.TriangleExclamation,
 size = 36.dp,
 iconSize = 16.sp,
 tint = Color(0xFFFDE68A), // amber-200
 containerColor = Color(0x6692400E),
 shape = MaterialTheme.shapes.small
 )
 Spacer(modifier = Modifier.width(12.dp))
 Text(
 text = stringResource(R.string.notif_permission_banner),
 style = MaterialTheme.typography.bodySmall,
 color = Color(0xFFFDE68A),
 modifier = Modifier.weight(1f)
 )
 }
 }
}

@Composable
fun BatteryOptimizationBanner(
 onClick: () -> Unit,
 modifier: Modifier = Modifier
) {
 Card(
 modifier = modifier
 .fillMaxWidth()
 .padding(vertical = 4.dp)
 .clickable(onClick = onClick),
 shape = MaterialTheme.shapes.medium,
 colors = CardDefaults.cardColors(
 containerColor = Color(0x66082F49) // sky-950/40
 ),
 border = BorderStroke(1.dp, Color(0x66075985)) // sky-800/40
 ) {
 Row(
 modifier = Modifier
 .fillMaxWidth()
 .padding(14.dp),
 verticalAlignment = Alignment.CenterVertically
 ) {
 FaIcon(
 glyph = FaIcons.BatteryHalf,
 size = 36.dp,
 iconSize = 16.sp,
 tint = Color(0xFFBAE6FD), // sky-200
 containerColor = Color(0x66075985),
 shape = MaterialTheme.shapes.small
 )
 Spacer(modifier = Modifier.width(12.dp))
 Text(
 text = stringResource(R.string.reminder_desc),
 style = MaterialTheme.typography.bodySmall,
 color = Color(0xFFBAE6FD),
 modifier = Modifier.weight(1f)
 )
 }
 }
}
