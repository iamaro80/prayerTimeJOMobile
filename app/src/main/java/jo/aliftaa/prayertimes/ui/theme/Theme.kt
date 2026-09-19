package jo.aliftaa.prayertimes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun PrayerTimeTheme(
    themeName: String = "emerald_original",
    darkMode: String = "system",
    language: String = "ar",
    fontScale: String = "default",
    content: @Composable () -> Unit
) {
    val isDark = when (darkMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = getThemeColorScheme(themeName, isDark)
    val customTokens = getCustomThemeTokens(themeName, isDark)
    val layoutDirection = if (language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    val locale = if (language == "ar") {
        java.util.Locale.forLanguageTag("ar-JO-u-nu-latn")
    } else {
        java.util.Locale(language)
    }
    val baseContext = androidx.compose.ui.platform.LocalContext.current
    val localizedConfiguration = android.content.res.Configuration(androidx.compose.ui.platform.LocalConfiguration.current).apply {
        setLocale(locale)
        setLayoutDirection(locale)
    }
    val localizedContext = baseContext.createConfigurationContext(localizedConfiguration)

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        androidx.compose.ui.platform.LocalConfiguration provides localizedConfiguration,
        androidx.compose.ui.platform.LocalContext provides localizedContext,
        LocalCustomThemeTokens provides customTokens
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = getTypography(fontScale),
            shapes = AppShapes,
            content = content
        )
    }
}
