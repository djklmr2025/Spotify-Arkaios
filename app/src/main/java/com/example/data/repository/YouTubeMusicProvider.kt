package com.example.data.repository

import android.util.Log
import com.example.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object YouTubeMusicProvider {
    private const val TAG = "YouTubeMusicProvider"

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    // Amplia lista de instancias Piped e Invidious actualizadas
    private val PIPED_INSTANCES = listOf(
        "https://pipedapi.kavin.rocks",
        "https://pipedapi.tokhmi.xyz",
        "https://api.piped.video",
        "https://pipedapi.adminforge.de",
        "https://pipedapi.palladium.sh",
        "https://piped-api.garudalinux.org",
        "https://saon.dev",
        "https://pipedapi.lunar.icu"
    )

    private val INVIDIOUS_INSTANCES = listOf(
        "https://inv.nadeko.net",
        "https://yewtu.be",
        "https://invidious.drgns.space",
        "https://invidious.nerdvpn.de",
        "https://inv.tux.pizza",
        "https://invidious.privacydev.net",
        "https://invidious.projectsegfau.lt"
    )

    suspend fun searchTracks(query: String, limit: Int = 25): List<Track> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val cleanQuery = query.trim()

        // 1. Intentar Piped APIs
        for (instance in PIPED_INSTANCES) {
            try {
                val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
                val url = "$instance/search?q=$encoded&filter=music_songs"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful && response.body != null) {
                    val jsonStr = response.body!!.string()
                    val json = JSONObject(jsonStr)
                    val items = json.optJSONArray("items") ?: JSONArray()
                    val resultList = mutableListOf<Track>()

                    for (i in 0 until items.length().coerceAtMost(limit)) {
                        val item = items.getJSONObject(i)
                        val videoId = item.optString("url", "").replace("/watch?v=", "").replace("/", "")
                        val title = item.optString("title", "").trim()
                        val uploader = item.optString("uploaderName", "YouTube Music").trim()
                        val thumbnail = item.optString("thumbnail", "")
                        val durationSec = item.optLong("duration", 210L)

                        if (videoId.isNotBlank() && title.isNotBlank()) {
                            resultList.add(
                                Track(
                                    id = "yt_$videoId",
                                    title = title,
                                    artist = if (uploader.isBlank()) "YouTube Artist" else uploader,
                                    album = "YouTube Music Single",
                                    durationMs = (if (durationSec > 0) durationSec else 210L) * 1000L,
                                    audioUrl = "https://www.youtube.com/watch?v=$videoId",
                                    coverUrl = if (thumbnail.isNotBlank()) thumbnail else "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                                    genre = "▶ YouTube Music (#1)",
                                    bitrate = "320 kbps (HQ Audio)",
                                    audioFormat = "MP3"
                                )
                            )
                        }
                    }

                    if (resultList.isNotEmpty()) {
                        return@withContext resultList
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Piped instance $instance falló: ${e.message}")
            }
        }

        // 2. Intentar Invidious APIs
        for (invInstance in INVIDIOUS_INSTANCES) {
            try {
                val encoded = URLEncoder.encode(cleanQuery + " audio", "UTF-8")
                val url = "$invInstance/api/v1/search?q=$encoded&type=video"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful && response.body != null) {
                    val jsonStr = response.body!!.string()
                    val items = JSONArray(jsonStr)
                    val resultList = mutableListOf<Track>()

                    for (i in 0 until items.length().coerceAtMost(limit)) {
                        val item = items.getJSONObject(i)
                        val videoId = item.optString("videoId", "")
                        val title = item.optString("title", "")
                        val author = item.optString("author", "YouTube Music")
                        val lengthSec = item.optLong("lengthSeconds", 210L)
                        val thumbnails = item.optJSONArray("videoThumbnails")
                        val thumbUrl = if (thumbnails != null && thumbnails.length() > 0) {
                            thumbnails.getJSONObject(0).optString("url", "")
                        } else ""

                        if (videoId.isNotBlank() && title.isNotBlank()) {
                            resultList.add(
                                Track(
                                    id = "yt_$videoId",
                                    title = title,
                                    artist = author,
                                    album = "YouTube Single",
                                    durationMs = lengthSec * 1000L,
                                    audioUrl = "https://www.youtube.com/watch?v=$videoId",
                                    coverUrl = if (thumbUrl.isNotBlank()) thumbUrl else "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                                    genre = "▶ YouTube Music (#1)",
                                    bitrate = "320 kbps",
                                    audioFormat = "MP3"
                                )
                            )
                        }
                    }

                    if (resultList.isNotEmpty()) {
                        return@withContext resultList
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Invidious instance $invInstance falló: ${e.message}")
            }
        }

        // 3. Direct YouTube Web Scrape Fallback
        try {
            val encoded = URLEncoder.encode(cleanQuery + " music audio", "UTF-8")
            val scrapeUrl = "https://www.youtube.com/results?search_query=$encoded"
            val request = Request.Builder()
                .url(scrapeUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .addHeader("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                val html = response.body!!.string()
                val pattern = Pattern.compile("\"videoId\":\"([^\"]{11})\".*?\"title\":\\{\"runs\":\\[\\{\"text\":\"([^\"]+)\"")
                val matcher = pattern.matcher(html)
                val resultList = mutableListOf<Track>()

                while (matcher.find() && resultList.size < limit) {
                    val vId = matcher.group(1) ?: ""
                    val title = matcher.group(2) ?: ""
                    if (vId.isNotBlank() && title.isNotBlank() && resultList.none { it.id == "yt_$vId" }) {
                        resultList.add(
                            Track(
                                id = "yt_$vId",
                                title = title,
                                artist = cleanQuery,
                                album = "YouTube Music Single",
                                durationMs = 210000L,
                                audioUrl = "https://www.youtube.com/watch?v=$vId",
                                coverUrl = "https://i.ytimg.com/vi/$vId/hqdefault.jpg",
                                genre = "▶ YouTube Music (#1)",
                                bitrate = "320 kbps (HQ Audio)",
                                audioFormat = "MP3"
                            )
                        )
                    }
                }

                if (resultList.isNotEmpty()) {
                    return@withContext resultList
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Scraper YouTube directo falló: ${e.message}")
        }

        return@withContext emptyList()
    }

    suspend fun resolveAudioStream(videoUrlOrId: String): String? = withContext(Dispatchers.IO) {
        val videoId = videoUrlOrId
            .replace("https://www.youtube.com/watch?v=", "")
            .replace("https://youtu.be/", "")
            .removePrefix("yt_")

        if (videoId.isBlank()) return@withContext null

        for (instance in PIPED_INSTANCES) {
            try {
                val url = "$instance/streams/$videoId"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful && response.body != null) {
                    val json = JSONObject(response.body!!.string())
                    val audioStreams = json.optJSONArray("audioStreams") ?: JSONArray()
                    var bestAudioUrl: String? = null
                    var maxBitrate = 0

                    for (i in 0 until audioStreams.length()) {
                        val stream = audioStreams.getJSONObject(i)
                        val streamUrl = stream.optString("url", "")
                        val bitrate = stream.optInt("bitrate", 0)
                        if (streamUrl.isNotBlank() && bitrate > maxBitrate) {
                            maxBitrate = bitrate
                            bestAudioUrl = streamUrl
                        }
                    }

                    if (bestAudioUrl != null) return@withContext bestAudioUrl
                }
            } catch (e: Exception) {}
        }

        for (invInstance in INVIDIOUS_INSTANCES) {
            try {
                val url = "$invInstance/api/v1/videos/$videoId"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful && response.body != null) {
                    val json = JSONObject(response.body!!.string())
                    val adaptiveFormats = json.optJSONArray("adaptiveFormats") ?: JSONArray()

                    for (i in 0 until adaptiveFormats.length()) {
                        val formatItem = adaptiveFormats.getJSONObject(i)
                        val type = formatItem.optString("type", "")
                        val streamUrl = formatItem.optString("url", "")
                        if (type.contains("audio", ignoreCase = true) && streamUrl.isNotBlank()) {
                            return@withContext streamUrl
                        }
                    }
                }
            } catch (e: Exception) {}
        }

        // Direct Stream proxy via inv.nadeko.net
        return@withContext "https://inv.nadeko.net/latest_version?id=$videoId&itag=140"
    }
}

