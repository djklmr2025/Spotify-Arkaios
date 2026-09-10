package com.example.data.repository

import android.util.Log
import com.example.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object ArkaiosNodeMusicProvider {
    private const val TAG = "ArkaiosNodeProvider"

    private val client = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // Endpoints candidatos del Servidor Real ARKAIOS Node
    private val NODE_ENDPOINTS = listOf(
        "http://192.168.101.106:8788",
        "http://10.0.2.2:8788",
        "http://localhost:8788"
    )

    private var activeEndpoint: String? = null

    suspend fun searchTracks(query: String, limit: Int = 50): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        val endpoints = if (activeEndpoint != null) listOf(activeEndpoint!!) + NODE_ENDPOINTS else NODE_ENDPOINTS

        for (base in endpoints.distinct()) {
            try {
                val encoded = URLEncoder.encode(query.trim(), "UTF-8")
                val url = "$base/api/search?q=$encoded"
                val req = Request.Builder().url(url).build()
                val resp = client.newCall(req).execute()

                if (resp.isSuccessful) {
                    val body = resp.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val arr = json.optJSONArray("results")
                        if (arr != null && arr.length() > 0) {
                            activeEndpoint = base
                            val count = Math.min(arr.length(), limit)
                            for (i in 0 until count) {
                                val item = arr.getJSONObject(i)
                                val id = item.optString("id", "klmr_$i")
                                val title = item.optString("title", "Pista DJ KLMR")
                                val artist = item.optString("artist", "DJ KLMR")
                                val album = item.optString("album", "DJ KLMR Private Vault")
                                val streamPath = item.optString("streamUrl", "/api/stream/$id")
                                val fullAudioUrl = if (streamPath.startsWith("http")) streamPath else "$base$streamPath"
                                val cover = item.optString("cover", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600")
                                val genre = item.optString("genre", "DJ Edit / Remix")
                                val format = item.optString("format", "MP3")

                                tracks.add(
                                    Track(
                                        id = id,
                                        title = title,
                                        artist = artist,
                                        album = album,
                                        durationMs = 225000L,
                                        audioUrl = fullAudioUrl,
                                        coverUrl = cover,
                                        genre = genre,
                                        bitrate = "320 kbps (HQ Lossless)",
                                        downloadSizeMb = 8.8,
                                        audioFormat = format
                                    )
                                )
                            }
                            Log.i(TAG, "Conectado a ARKAIOS Node: $base con ${tracks.size} pistas.")
                            return@withContext tracks
                        }
                    }
                }
            } catch (e: Exception) {
                // Siguiente endpoint
            }
        }
        tracks
    }

    suspend fun getInitialCatalog(limit: Int = 40): List<Track> = withContext(Dispatchers.IO) {
        searchTracks("", limit)
    }
}
