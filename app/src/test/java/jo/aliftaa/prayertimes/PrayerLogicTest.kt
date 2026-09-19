package jo.aliftaa.prayertimes

import jo.aliftaa.prayertimes.data.parser.PrayerRssParser
import jo.aliftaa.prayertimes.utils.DateUtils
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class PrayerLogicTest {

    private val sampleAtomFeed = """
        <?xml version="1.0" encoding="utf-8" ?>
        <feed xmlns="http://www.w3.org/2005/Atom" xml:lang="en">
            <title>دائرة الإفتاء -  أوقات الصلاة</title>
            <link rel="alternate" href="http://aliftaa.jo/"></link>
            <id>http://aliftaa.jo/</id>
            <updated>16/09/2026 06:31:17 ص</updated>
            <subtitle> أوقات الصلاة حسب التوقيت المحلي لمدينة عمان 16-09-2026 </subtitle>
            <entry><title> الفجر: 04:58</title></entry>
            <entry><title> الشروق: 06:15</title></entry>
            <entry><title> الظهر: 12:31</title></entry>
            <entry><title> العصر: 04:02</title></entry>
            <entry><title> المغرب: 06:47</title></entry>
            <entry><title> العشاء: 08:03</title></entry>
        </feed>
    """.trimIndent()

    @Test
    fun testRssParsing() {
        val parser = PrayerRssParser()
        val feed = parser.parse(sampleAtomFeed)

        assertEquals(6, feed.prayers.size)

        // 1. Fajr
        val fajr = feed.prayers.find { it.id == "fajr" }
        assertNotNull(fajr)
        assertEquals("04:58", fajr!!.time24)
        assertTrue(fajr.isPrayer)

        // 2. Shurooq (CRITICAL: isPrayer MUST BE FALSE)
        val shurooq = feed.prayers.find { it.id == "shurooq" }
        assertNotNull(shurooq)
        assertEquals("06:15", shurooq!!.time24)
        assertFalse("Shurooq must NOT be considered a prayer!", shurooq.isPrayer)

        // 3. Dhuhr
        val dhuhr = feed.prayers.find { it.id == "dhuhr" }
        assertNotNull(dhuhr)
        assertEquals("12:31", dhuhr!!.time24)
        assertTrue(dhuhr.isPrayer)

        // 4. Asr (Converted to 24-hr: 04:02 -> 16:02)
        val asr = feed.prayers.find { it.id == "asr" }
        assertNotNull(asr)
        assertEquals("16:02", asr!!.time24)
        assertTrue(asr.isPrayer)

        // 5. Maghrib (06:47 -> 18:47)
        val maghrib = feed.prayers.find { it.id == "maghrib" }
        assertNotNull(maghrib)
        assertEquals("18:47", maghrib!!.time24)
        assertTrue(maghrib.isPrayer)

        // 6. Isha (08:03 -> 20:03)
        val isha = feed.prayers.find { it.id == "isha" }
        assertNotNull(isha)
        assertEquals("20:03", isha!!.time24)
        assertTrue(isha.isPrayer)
    }

    @Test
    fun testTimeDisplayFormatting() {
        // 12-hour format test
        val time12Ar = DateUtils.formatTimeDisplay("16:02", is24Hour = false, isArabic = true)
        assertEquals("04:02 م", time12Ar)

        val time12En = DateUtils.formatTimeDisplay("04:58", is24Hour = false, isArabic = false)
        assertEquals("04:58 AM", time12En)

        // 24-hour format test
        val time24 = DateUtils.formatTimeDisplay("16:02", is24Hour = true, isArabic = true)
        assertEquals("16:02", time24)
    }

    @Test
    fun testQiblaBearingsForJordanianCities() {
        // Amman: expected ~160.7° (~161°)
        val ammanBearing = jo.aliftaa.prayertimes.qibla.QiblaSensorManager.calculateQiblaBearing(31.9539, 35.9106)
        assertEquals(160.71f, ammanBearing, 0.5f)

        // Aqaba: expected ~150.7° (within Jordan's southern region)
        val aqabaBearing = jo.aliftaa.prayertimes.qibla.QiblaSensorManager.calculateQiblaBearing(29.5321, 35.0063)
        assertEquals(150.71f, aqabaBearing, 0.5f)

        // Irbid: expected ~161.4° (within Jordan's northern region)
        val irbidBearing = jo.aliftaa.prayertimes.qibla.QiblaSensorManager.calculateQiblaBearing(32.5568, 35.8469)
        assertEquals(161.39f, irbidBearing, 0.5f)
    }
}
