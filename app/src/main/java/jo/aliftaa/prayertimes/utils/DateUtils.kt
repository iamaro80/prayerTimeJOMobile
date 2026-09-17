package jo.aliftaa.prayertimes.utils

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.util.Date
import java.util.Locale

object DateUtils {

    fun getFormattedHijriDate(locale: Locale): String {
        return try {
            val hijrahDate = HijrahDate.now()
            val year = hijrahDate.get(java.time.temporal.ChronoField.YEAR)
            val month = hijrahDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
            val day = hijrahDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)

            val monthNamesAr = arrayOf(
                "", "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
                "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
                "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
            )

            val monthNamesEn = arrayOf(
                "", "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
                "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
                "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
            )

            val isArabic = locale.language == "ar"
            val monthName = if (isArabic) {
                monthNamesAr.getOrElse(month) { "" }
            } else {
                monthNamesEn.getOrElse(month) { "" }
            }

            if (isArabic) {
                "$day $monthName $year هـ"
            } else {
                "$day $monthName $year AH"
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getFormattedGregorianDate(locale: Locale): String {
        val pattern = if (locale.language == "ar") "EEEE، d MMMM yyyy" else "EEEE, d MMMM yyyy"
        return SimpleDateFormat(pattern, locale).format(Date())
    }

    fun formatTimeDisplay(time24: String, is24Hour: Boolean, isArabic: Boolean): String {
        val parts = time24.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        if (is24Hour) {
            return String.format(Locale.US, "%02d:%02d", hour, minute)
        }

        val periodAr = if (hour < 12) "ص" else "م"
        val periodEn = if (hour < 12) "AM" else "PM"
        val period = if (isArabic) periodAr else periodEn

        val hour12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }

        return String.format(Locale.US, "%02d:%02d %s", hour12, minute, period)
    }
}
