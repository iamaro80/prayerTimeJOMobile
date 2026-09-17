package jo.aliftaa.prayertimes.data.model

data class Prayer(
    val id: String,          // "fajr", "shurooq", "dhuhr", "asr", "maghrib", "isha"
    val nameAr: String,      // الفجر, الشروق, الظهر, العصر, المغرب, العشاء
    val nameEn: String,      // Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha
    val time24: String,      // "HH:mm" (e.g. "04:58", "12:31", "16:02")
    val isPrayer: Boolean    // false for Shurooq (Sunrise)
) {
    val hour: Int
        get() = time24.split(":").getOrNull(0)?.toIntOrNull() ?: 0

    val minute: Int
        get() = time24.split(":").getOrNull(1)?.toIntOrNull() ?: 0
}

data class PrayerFeed(
    val title: String,
    val dateGregorian: String,
    val hijriFormatted: String,
    val prayers: List<Prayer>,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class AzanTrack(
    val id: String,          // "azan_1" .. "azan_12"
    val rawResId: Int,
    val titleResId: Int
)
