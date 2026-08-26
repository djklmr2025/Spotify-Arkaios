package com.example.data.m3u

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object M3uParser {

    /**
     * Parse M3U/M3U8 content string into a list of Track models.
     * Supports #EXTINF:duration,Artist - Title and raw URL lines.
     */
    fun parseM3uContent(content: String, defaultCategory: String = "M3U Playlist"): List<Track> {
        val tracks = mutableListOf<Track>()
        val lines = content.lines()

        var currentTitle = ""
        var currentArtist = "M3U Audio"
        var currentDurationSec = 0L
        var currentCover = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80"
        var currentGenre = defaultCategory

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isBlank() || line.startsWith("#EXTM3U")) continue

            if (line.startsWith("#EXTINF:", ignoreCase = true)) {
                // Example: #EXTINF:240 tvg-logo="http..." group-title="Rock",Queen - Bohemian Rhapsody
                try {
                    val infoPart = line.substringAfter("#EXTINF:")
                    val durationStr = infoPart.substringBefore(",").substringBefore(" ").trim()
                    currentDurationSec = durationStr.toLongOrNull()?.coerceAtLeast(0L) ?: 0L

                    // Extract tvg-logo if present
                    if (infoPart.contains("tvg-logo=\"")) {
                        val logo = infoPart.substringAfter("tvg-logo=\"").substringBefore("\"")
                        if (logo.isNotBlank()) currentCover = logo
                    }

                    // Extract group-title as genre if present
                    if (infoPart.contains("group-title=\"")) {
                        val group = infoPart.substringAfter("group-title=\"").substringBefore("\"")
                        if (group.isNotBlank()) currentGenre = group
                    }

                    val titlePart = infoPart.substringAfter(",", "")
                    if (titlePart.contains(" - ")) {
                        currentArtist = titlePart.substringBefore(" - ").trim()
                        currentTitle = titlePart.substringAfter(" - ").trim()
                    } else if (titlePart.isNotBlank()) {
                        currentTitle = titlePart.trim()
                        currentArtist = "Stream"
                    }
                } catch (e: Exception) {
                    Log.w("M3uParser", "Error parsing EXTINF: $line", e)
                }
            } else if (!line.startsWith("#")) {
                // This is the media URL or file path
                val url = line
                val title = if (currentTitle.isNotBlank()) currentTitle else url.substringAfterLast("/").substringBeforeLast(".")
                val artist = if (currentArtist.isNotBlank()) currentArtist else "M3U Stream"
                val durationMs = if (currentDurationSec > 0) currentDurationSec * 1000L else 0L

                val isLive = durationMs == 0L || url.contains(".m3u8") || url.contains(":80") || url.contains("/stream")

                tracks.add(
                    Track(
                        id = "m3u_${url.hashCode()}_${System.currentTimeMillis() % 10000}",
                        title = title.ifBlank { "M3U Audio Track" },
                        artist = artist,
                        album = if (isLive) "Radio M3U" else "Lista M3U",
                        durationMs = durationMs,
                        audioUrl = url,
                        coverUrl = currentCover,
                        genre = currentGenre,
                        bitrate = if (isLive) "M3U Live Stream" else "M3U Audio Link",
                        audioFormat = if (url.contains(".m3u8")) "HLS/M3U8" else "M3U/MP3",
                        isDownloaded = false
                    )
                )

                // Reset per-track state
                currentTitle = ""
                currentArtist = "M3U Audio"
                currentDurationSec = 0L
                currentCover = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80"
                currentGenre = defaultCategory
            }
        }

        return tracks
    }

    /**
     * Reads M3U / M3U8 from Android Content Uri
     */
    suspend fun parseFromUri(context: Context, uri: Uri): List<Track> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val reader = BufferedReader(InputStreamReader(stream))
                val content = reader.readText()
                parseM3uContent(content, "Lista M3U Importada")
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("M3uParser", "Failed to parse M3U from Uri: $uri", e)
            emptyList()
        }
    }

    /**
     * Downloads and parses M3U / M3U8 from a web URL
     */
    suspend fun parseFromUrl(urlString: String): List<Track> = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString.trim())
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "ArkaiosTify/1.0")

            if (connection.responseCode in 200..299) {
                val content = connection.inputStream.bufferedReader().use { it.readText() }
                parseM3uContent(content, "Radio M3U Online")
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("M3uParser", "Failed to load M3U from URL: $urlString", e)
            emptyList()
        }
    }

    /**
     * Exports a list of tracks to standard #EXTM3U format
     */
    fun exportToM3u(playlistName: String, tracks: List<Track>): String {
        val builder = java.lang.StringBuilder()
        builder.append("#EXTM3U\n")
        builder.append("#PLAYLIST:$playlistName\n\n")

        for (track in tracks) {
            val durationSec = (track.durationMs / 1000).coerceAtLeast(-1)
            builder.append("#EXTINF:$durationSec tvg-logo=\"${track.coverUrl}\" group-title=\"${track.genre}\",${track.artist} - ${track.title}\n")
            builder.append("${track.audioUrl}\n\n")
        }

        return builder.toString()
    }
}
