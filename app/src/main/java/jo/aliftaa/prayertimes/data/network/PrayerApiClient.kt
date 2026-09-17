package jo.aliftaa.prayertimes.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class PrayerApiClient {
    private val feedUrl = "https://www.aliftaa.jo/PrayTimes.ashx"

    suspend fun fetchPrayerFeed(): String = withContext(Dispatchers.IO) {
        val url = URL(feedUrl)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 12000
            connection.readTimeout = 12000
            connection.setRequestProperty("User-Agent", "PrayerTimesJordan/1.0")
            connection.setRequestProperty("Accept", "application/xml, text/xml, */*")

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } else {
                throw Exception("HTTP error $responseCode: ${connection.responseMessage}")
            }
        } finally {
            connection.disconnect()
        }
    }
}
