package jo.aliftaa.prayertimes.data.parser

import jo.aliftaa.prayertimes.data.model.Prayer
import jo.aliftaa.prayertimes.data.model.PrayerFeed
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrayerRssParser {

    /**
     * Parses the Atom XML feed returned by https://www.aliftaa.jo/PrayTimes.ashx
     *
     * Sample entry:
     * <entry><title> الفجر: 04:58</title></entry>
     * <entry><title> الشروق: 06:15</title></entry>
     * <entry><title> الظهر: 12:31</title></entry>
     * <entry><title> العصر: 04:02</title></entry>  <- Notice Asr is formatted as 12h or 24h
     * <entry><title> المغرب: 06:47</title></entry>
     * <entry><title> العشاء: 08:03</title></entry>
     */
    fun parse(xmlContent: String, hijriFormatted: String = ""): PrayerFeed {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlContent))

        var eventType = parser.eventType
        var currentTagName = ""
        var feedTitle = "دائرة الإفتاء - أوقات الصلاة"
        var subtitle = ""
        val rawTitles = mutableListOf<String>()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTagName = parser.name.lowercase()
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text?.trim() ?: ""
                    if (text.isNotEmpty()) {
                        when (currentTagName) {
                            "title" -> {
                                if (rawTitles.isEmpty() && !text.contains(":")) {
                                    feedTitle = text
                                } else {
                                    rawTitles.add(text)
                                }
                            }
                            "subtitle" -> subtitle = text
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    currentTagName = ""
                }
            }
            eventType = parser.next()
        }

        val prayers = processEntries(rawTitles)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        return PrayerFeed(
            title = feedTitle,
            dateGregorian = todayStr,
            hijriFormatted = hijriFormatted,
            prayers = prayers,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun processEntries(titles: List<String>): List<Prayer> {
        val result = mutableListOf<Prayer>()

        for (title in titles) {
            val parts = title.split(":")
            if (parts.size >= 2) {
                val namePart = parts[0].trim()
                // Time is hour and minute
                val hourStr = parts.getOrNull(1)?.trim() ?: ""
                val minStr = parts.getOrNull(2)?.trim() ?: ""

                val rawTime = if (minStr.isNotEmpty()) "$hourStr:$minStr" else hourStr
                val prayer = buildPrayer(namePart, rawTime)
                if (prayer != null) {
                    result.add(prayer)
                }
            }
        }

        return result
    }

    private fun buildPrayer(name: String, rawTime: String): Prayer? {
        val timeRegex = Regex("""(\d{1,2})[:.](\d{2})""")
        val match = timeRegex.find(rawTime) ?: return null
        var hour = match.groupValues[1].toInt()
        val minute = match.groupValues[2].toInt()

        val cleanName = name.replace(":", "").trim()

        return when {
            cleanName.contains("الفجر") -> {
                // Fajr is AM (e.g. 04:58)
                val time24 = formatTime(hour, minute)
                Prayer("fajr", "الفجر", "Fajr", time24, isPrayer = true)
            }
            cleanName.contains("الشروق") -> {
                // Shurooq is AM (e.g. 06:15) - NOT A PRAYER
                val time24 = formatTime(hour, minute)
                Prayer("shurooq", "الشروق", "Sunrise", time24, isPrayer = false)
            }
            cleanName.contains("الظهر") -> {
                // Dhuhr is noon (11:xx or 12:xx or 13:xx)
                val time24 = formatTime(if (hour < 10) hour + 12 else hour, minute)
                Prayer("dhuhr", "الظهر", "Dhuhr", time24, isPrayer = true)
            }
            cleanName.contains("العصر") -> {
                // Asr is afternoon, feed has 04:02 -> convert to 16:02 if < 12
                val h24 = if (hour < 12) hour + 12 else hour
                val time24 = formatTime(h24, minute)
                Prayer("asr", "العصر", "Asr", time24, isPrayer = true)
            }
            cleanName.contains("المغرب") -> {
                // Maghrib is sunset, feed has 06:47 -> convert to 18:47 if < 12
                val h24 = if (hour < 12) hour + 12 else hour
                val time24 = formatTime(h24, minute)
                Prayer("maghrib", "المغرب", "Maghrib", time24, isPrayer = true)
            }
            cleanName.contains("العشاء") -> {
                // Isha is night, feed has 08:03 -> convert to 20:03 if < 12
                val h24 = if (hour < 12) hour + 12 else hour
                val time24 = formatTime(h24, minute)
                Prayer("isha", "العشاء", "Isha", time24, isPrayer = true)
            }
            else -> null
        }
    }

    private fun formatTime(hour: Int, minute: Int): String {
        return String.format(Locale.US, "%02d:%02d", hour, minute)
    }
}
