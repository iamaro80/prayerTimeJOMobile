package jo.aliftaa.prayertimes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import jo.aliftaa.prayertimes.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val cairoFont = GoogleFont("Cairo")

val CairoFontFamily = FontFamily(
    Font(googleFont = cairoFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = cairoFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = cairoFont, fontProvider = provider, weight = FontWeight.Bold)
)

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)

fun getTypography(fontScale: String): Typography {
    val factor = when (fontScale.lowercase()) {
        "small" -> 0.88f
        "medium" -> 1.12f
        "large" -> 1.25f
        else -> 1.0f
    }
    if (factor == 1.0f) return AppTypography

    return Typography(
        headlineLarge = AppTypography.headlineLarge.copy(fontSize = (32 * factor).sp, lineHeight = (40 * factor).sp),
        headlineMedium = AppTypography.headlineMedium.copy(fontSize = (28 * factor).sp, lineHeight = (36 * factor).sp),
        headlineSmall = AppTypography.headlineSmall.copy(fontSize = (24 * factor).sp, lineHeight = (32 * factor).sp),
        titleLarge = AppTypography.titleLarge.copy(fontSize = (22 * factor).sp, lineHeight = (28 * factor).sp),
        titleMedium = AppTypography.titleMedium.copy(fontSize = (18 * factor).sp, lineHeight = (24 * factor).sp),
        titleSmall = AppTypography.titleSmall.copy(fontSize = (14 * factor).sp, lineHeight = (20 * factor).sp),
        bodyLarge = AppTypography.bodyLarge.copy(fontSize = (16 * factor).sp, lineHeight = (24 * factor).sp),
        bodyMedium = AppTypography.bodyMedium.copy(fontSize = (14 * factor).sp, lineHeight = (20 * factor).sp),
        bodySmall = AppTypography.bodySmall.copy(fontSize = (12 * factor).sp, lineHeight = (16 * factor).sp),
        labelLarge = AppTypography.labelLarge.copy(fontSize = (14 * factor).sp, lineHeight = (20 * factor).sp),
        labelMedium = AppTypography.labelMedium.copy(fontSize = (12 * factor).sp, lineHeight = (16 * factor).sp),
        labelSmall = AppTypography.labelSmall.copy(fontSize = (11 * factor).sp, lineHeight = (16 * factor).sp)
    )
}
