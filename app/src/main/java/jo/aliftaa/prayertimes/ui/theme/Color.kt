package jo.aliftaa.prayertimes.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val EmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
fun <T> md3Tween() = tween<T>(durationMillis = 300, easing = EmphasizedDecelerateEasing)

@Immutable
data class CustomThemeTokens(
    val heroGradientStart: Color,
    val heroGradientEnd: Color,
    val heroAccentContainer: Color,
    val onHeroAccentContainer: Color,
    val activeCardOutline: Color,
    val countdownRingTrack: Color,
    val countdownRingProgress: Color
)

val LocalCustomThemeTokens = staticCompositionLocalOf {
    CustomThemeTokens(
        heroGradientStart = Color(0xFF065F46),
        heroGradientEnd = Color(0xFF134E4A),
        heroAccentContainer = Color(0x80064E3B),
        onHeroAccentContainer = Color(0xFFA7F3D0),
        activeCardOutline = Color(0xFF14B8A6),
        countdownRingTrack = Color(0x66064E3B),
        countdownRingProgress = Color(0xFF34D399)
    )
}

// 1. Emerald Original (dark)
val EmeraldOriginalColorScheme = darkColorScheme(
    surface = Color(0xFF14191C),
    surfaceContainer = Color(0xFF1D2427),
    onSurface = Color(0xFFF2F4F7),
    onSurfaceVariant = Color(0xFFB9C2C7),
    primary = Color(0xFF10B981),
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFFA7F3D0),
    background = Color(0xFF14191C),
    onBackground = Color(0xFFF2F4F7)
)
val EmeraldOriginalTokens = CustomThemeTokens(
    heroGradientStart = Color(0xFF065F46),
    heroGradientEnd = Color(0xFF134E4A),
    heroAccentContainer = Color(0x80064E3B),
    onHeroAccentContainer = Color(0xFFA7F3D0),
    activeCardOutline = Color(0xFF14B8A6),
    countdownRingTrack = Color(0x66064E3B),
    countdownRingProgress = Color(0xFF34D399)
)

// 2. Soft Terracotta (light)
val SoftTerracottaColorScheme = lightColorScheme(
    surface = Color(0xFFFCF9F5),
    surfaceContainer = Color(0xFFF3EDE2),
    onSurface = Color(0xFF2D160A),
    onSurfaceVariant = Color(0xFF6E584B),
    primary = Color(0xFFC27854),
    primaryContainer = Color(0xFFFDEAD9),
    onPrimaryContainer = Color(0xFF432818),
    secondaryContainer = Color(0xFFFFF3E6),
    onSecondaryContainer = Color(0xFF432818),
    background = Color(0xFFFCF9F5),
    onBackground = Color(0xFF2D160A)
)
val SoftTerracottaTokens = CustomThemeTokens(
    heroGradientStart = Color(0xFFC37D46),
    heroGradientEnd = Color(0xFFB06536),
    heroAccentContainer = Color(0xFFFDEAD9),
    onHeroAccentContainer = Color(0xFFBC6C25),
    activeCardOutline = Color(0xFFD4A373),
    countdownRingTrack = Color(0x4DFFEDD5),
    countdownRingProgress = Color(0xFFFED7AA)
)

// 3. Seafoam & Sand (light)
val SeafoamSandColorScheme = lightColorScheme(
    surface = Color(0xFFF4F8F9),
    surfaceContainer = Color(0xFFE4EFF1),
    onSurface = Color(0xFF12262B),
    onSurfaceVariant = Color(0xFF4A6066),
    primary = Color(0xFF205D6B),
    primaryContainer = Color(0xFFE0F2F1),
    onPrimaryContainer = Color(0xFF12262B),
    secondaryContainer = Color(0xFFE8F5F4),
    onSecondaryContainer = Color(0xFF12262B),
    background = Color(0xFFF4F8F9),
    onBackground = Color(0xFF12262B)
)
val SeafoamSandTokens = CustomThemeTokens(
    heroGradientStart = Color(0xFF457B9D),
    heroGradientEnd = Color(0xFF2A9D8F),
    heroAccentContainer = Color(0xFFE0F2F1),
    onHeroAccentContainer = Color(0xFF2A9D8F),
    activeCardOutline = Color(0xFF2A9D8F),
    countdownRingTrack = Color(0x40D0F4DE),
    countdownRingProgress = Color(0xFFA8DADC)
)

// 4. Muted Slate & Gold (dark)
val MutedSlateGoldColorScheme = darkColorScheme(
    surface = Color(0xFF141A1F),
    surfaceContainer = Color(0xFF1E262C),
    onSurface = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF94A3B8),
    primary = Color(0xFFD4AF37),
    primaryContainer = Color(0x33FFD700),
    onPrimaryContainer = Color(0xFFFFE57F),
    secondaryContainer = Color(0x4D37474F),
    onSecondaryContainer = Color(0xFFFFE57F),
    background = Color(0xFF141A1F),
    onBackground = Color(0xFFE2E8F0)
)
val MutedSlateGoldTokens = CustomThemeTokens(
    heroGradientStart = Color(0xFF37474F),
    heroGradientEnd = Color(0xFF263238),
    heroAccentContainer = Color(0x2BFFD700),
    onHeroAccentContainer = Color(0xFFFFD700),
    activeCardOutline = Color(0xFFFFD700),
    countdownRingTrack = Color(0x33FFD700),
    countdownRingProgress = Color(0xFFFFD700)
)

// 5. Deep Charcoal & Canary (dark)
val DeepCharcoalYellowColorScheme = darkColorScheme(
    surface = Color(0xFF121212),
    surfaceContainer = Color(0xFF1E1E1E),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFA3A3A3),
    primary = Color(0xFFEAB308),
    primaryContainer = Color(0x33FFEA00),
    onPrimaryContainer = Color(0xFFFFF566),
    secondaryContainer = Color(0x402D2D2D),
    onSecondaryContainer = Color(0xFFFFF566),
    background = Color(0xFF121212),
    onBackground = Color(0xFFFFFFFF)
)
val DeepCharcoalYellowTokens = CustomThemeTokens(
    heroGradientStart = Color(0xFF262626),
    heroGradientEnd = Color(0xFF1C1C1C),
    heroAccentContainer = Color(0x30FFEA00),
    onHeroAccentContainer = Color(0xFFFFEA00),
    activeCardOutline = Color(0xFFFFEA00),
    countdownRingTrack = Color(0x33FFEA00),
    countdownRingProgress = Color(0xFFFFEA00)
)

// 6. Simple Theme (Light and Dark variants)
val SimpleThemeLightColorScheme = lightColorScheme(
    surface = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF1F5F9),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569),
    primary = Color(0xFF2563EB),
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF0F172A),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A)
)

val SimpleThemeDarkColorScheme = darkColorScheme(
    surface = Color(0xFF18181B),
    surfaceContainer = Color(0xFF27272A),
    onSurface = Color(0xFFFAFAFA),
    onSurfaceVariant = Color(0xFFA1A1AA),
    primary = Color(0xFF3B82F6),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFBFDBFE),
    secondaryContainer = Color(0xFF27272A),
    onSecondaryContainer = Color(0xFFFAFAFA),
    background = Color(0xFF09090B),
    onBackground = Color(0xFFFAFAFA)
)

val SimpleThemeLightTokens = CustomThemeTokens(
    heroGradientStart = Color(0xFF2563EB),
    heroGradientEnd = Color(0xFF1D4ED8),
    heroAccentContainer = Color(0x33FFFFFF),
    onHeroAccentContainer = Color(0xFFFFFFFF),
    activeCardOutline = Color(0xFF2563EB),
    countdownRingTrack = Color(0x33FFFFFF),
    countdownRingProgress = Color(0xFFFFFFFF)
)

val SimpleThemeDarkTokens = CustomThemeTokens(
    heroGradientStart = Color(0xFF1E3A8A),
    heroGradientEnd = Color(0xFF172554),
    heroAccentContainer = Color(0x223B82F6),
    onHeroAccentContainer = Color(0xFF93C5FD),
    activeCardOutline = Color(0xFF3B82F6),
    countdownRingTrack = Color(0x333B82F6),
    countdownRingProgress = Color(0xFF60A5FA)
)

fun getThemeColorScheme(themeName: String, isDark: Boolean): ColorScheme {
    return when (themeName.lowercase()) {
        "simple" -> if (isDark) SimpleThemeDarkColorScheme else SimpleThemeLightColorScheme
        "soft_terracotta", "terracotta" -> SoftTerracottaColorScheme
        "seafoam_sand", "seafoam" -> SeafoamSandColorScheme
        "muted_slate_gold", "slate", "gold" -> MutedSlateGoldColorScheme
        "deep_charcoal_yellow", "charcoal", "yellow" -> DeepCharcoalYellowColorScheme
        else -> EmeraldOriginalColorScheme
    }
}

fun getCustomThemeTokens(themeName: String, isDark: Boolean = false): CustomThemeTokens {
    return when (themeName.lowercase()) {
        "simple" -> if (isDark) SimpleThemeDarkTokens else SimpleThemeLightTokens
        "soft_terracotta", "terracotta" -> SoftTerracottaTokens
        "seafoam_sand", "seafoam" -> SeafoamSandTokens
        "muted_slate_gold", "slate", "gold" -> MutedSlateGoldTokens
        "deep_charcoal_yellow", "charcoal", "yellow" -> DeepCharcoalYellowTokens
        else -> EmeraldOriginalTokens
    }
}
