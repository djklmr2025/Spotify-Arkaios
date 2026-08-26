package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.PlaylistEntity
import com.example.data.local.TrackEntity
import com.example.data.model.Album
import com.example.data.model.GenreCategory
import com.example.data.model.Playlist
import com.example.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class MusicRepository(private val context: Context, private val db: AppDatabase) {

    private val trackDao = db.trackDao()
    private val playlistDao = db.playlistDao()

    val downloadedTracksFlow: Flow<List<Track>> = trackDao.getDownloadedTracks().map { list ->
        list.map { it.toTrack() }
    }

    val favoriteTracksFlow: Flow<List<Track>> = trackDao.getFavoriteTracks().map { list ->
        list.map { it.toTrack() }
    }

    val allCachedTracksFlow: Flow<List<Track>> = trackDao.getAllTracks().map { list ->
        list.map { it.toTrack() }
    }

    val customPlaylistsFlow: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    suspend fun getInitialCatalog(): List<Track> = withContext(Dispatchers.IO) {
        val defaultTracks = listOf(
            Track(
                id = "ark_01",
                title = "Cybernetic Horizon (FLAC Master)",
                artist = "Arkaios Sound Lab",
                album = "Nexus Echoes 2026",
                durationMs = 214000L,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
                genre = "Cyberpunk / Synth",
                bitrate = "9216 kbps (24-bit/192kHz Tidal)",
                tidalId = "tidal_track_99812",
                isFavorite = true,
                downloadSizeMb = 14.2,
                audioFormat = "FLAC"
            ),
            Track(
                id = "ark_02",
                title = "Midnight Neon Pulse",
                artist = "Sovereign Synthwave",
                album = "Future Grid Tokyo",
                durationMs = 198000L,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
                genre = "Synthwave",
                bitrate = "320 kbps (Hi-Fi)",
                tidalId = "tidal_track_99813",
                downloadSizeMb = 7.8,
                audioFormat = "MP3"
            ),
            Track(
                id = "ark_03",
                title = "Cosmic Lo-Fi Reverie",
                artist = "Puter Chill Station",
                album = "Cloud Orbit Vol. 1",
                durationMs = 184000L,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
                genre = "Lo-Fi Beats",
                bitrate = "320 kbps (Hi-Fi)",
                isFavorite = true,
                downloadSizeMb = 6.9,
                audioFormat = "M4A"
            ),
            Track(
                id = "ark_04",
                title = "Quantum Bass Resonance",
                artist = "Arkaios God Node",
                album = "Subatomic Frequencies",
                durationMs = 232000L,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                genre = "Future Bass",
                bitrate = "1411 kbps (Tidal HiFi)",
                downloadSizeMb = 11.5,
                audioFormat = "FLAC"
            ),
            Track(
                id = "ark_05",
                title = "Emerald Aurora Dreams",
                artist = "Aethelgard Ambient",
                album = "Nordic Solitude",
                durationMs = 260000L,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                coverUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
                genre = "Ambient / Drone",
                bitrate = "320 kbps (Hi-Fi)",
                downloadSizeMb = 8.6,
                audioFormat = "MP3"
            ),
            Track(
                id = "ark_06",
                title = "AMR Golden Velocity",
                artist = "Arkaios Pay Orchestra",
                album = "Token Symphony",
                durationMs = 210000L,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                genre = "Electronic / Orchestral",
                bitrate = "320 kbps (Hi-Fi)",
                downloadSizeMb = 9.2,
                audioFormat = "M4A"
            ),
            Track(
                id = "ark_07",
                title = "Analog Sunset in Kyoto",
                artist = "Retro Wave Master",
                album = "Sunset Cassette",
                durationMs = 195000L,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
                coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
                genre = "Synthwave",
                bitrate = "320 kbps (Hi-Fi)",
                downloadSizeMb = 7.4,
                audioFormat = "MP3"
            ),
            Track(
                id = "ark_08",
                title = "Deep Neural Odyssey",
                artist = "Gemini Core Beats",
                album = "Generative Audio 2026",
                durationMs = 245000L,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                coverUrl = "https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=600&auto=format&fit=crop&q=80",
                genre = "IDM / Glitch",
                bitrate = "1411 kbps (Tidal HiFi)",
                downloadSizeMb = 12.1,
                audioFormat = "FLAC"
            )
        )

        // Seed DB if empty
        val entities = defaultTracks.map { TrackEntity.fromTrack(it) }
        trackDao.insertOrUpdateTracks(entities)
        defaultTracks
    }

    fun getFeaturedAlbums(): List<Album> {
        return listOf(
            Album(
                id = "alb_01",
                title = "Nexus Echoes 2026",
                artist = "Arkaios Sound Lab",
                coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
                year = "2026",
                genre = "Cyberpunk / Synth",
                trackCount = 12
            ),
            Album(
                id = "alb_02",
                title = "Future Grid Tokyo",
                artist = "Sovereign Synthwave",
                coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
                year = "2025",
                genre = "Synthwave",
                trackCount = 10
            ),
            Album(
                id = "alb_03",
                title = "Cloud Orbit Vol. 1",
                artist = "Puter Chill Station",
                coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
                year = "2026",
                genre = "Lo-Fi Beats",
                trackCount = 14
            ),
            Album(
                id = "alb_04",
                title = "Token Symphony",
                artist = "Arkaios Pay Orchestra",
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                year = "2026",
                genre = "Electronic / Orchestral",
                trackCount = 8
            )
        )
    }

    fun getGenres(): List<GenreCategory> {
        return listOf(
            GenreCategory("gen_01", "Cyberpunk & Synth", "waves", 0xFF0D9488, 0xFF042F2E),
            GenreCategory("gen_02", "Lo-Fi & Study", "headphones", 0xFF854D0E, 0xFF451A03),
            GenreCategory("gen_03", "Tidal Master Hi-Fi", "disc", 0xFF0284C7, 0xFF0C4A6E),
            GenreCategory("gen_04", "Future Bass & Trap", "speaker", 0xFF9333EA, 0xFF581C87),
            GenreCategory("gen_05", "Arkaios AMR Hits", "coins", 0xFFD97706, 0xFF78350F),
            GenreCategory("gen_06", "Chill Ambient", "cloud", 0xFF059669, 0xFF064E3B),
            GenreCategory("gen_07", "Gaming & Chiptune", "gamepad", 0xFFDC2626, 0xFF7F1D1D),
            GenreCategory("gen_08", "Acoustic & Live", "guitar", 0xFFD97706, 0xFF92400E)
        )
    }

    suspend fun toggleFavorite(trackId: String, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
        trackDao.updateFavoriteStatus(trackId, !currentFavorite)
    }

    suspend fun createCustomPlaylist(title: String, description: String): String = withContext(Dispatchers.IO) {
        val id = "pl_" + System.currentTimeMillis()
        val playlist = PlaylistEntity(
            id = id,
            title = title,
            description = description,
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            author = "Arkaios User"
        )
        playlistDao.insertPlaylist(playlist)
        id
    }

    suspend fun scanDeviceLocalAudio(): List<Track> = withContext(Dispatchers.IO) {
        val localTracks = mutableListOf<Track>()
        val seenPathsOrUris = mutableSetOf<String>()

        // 1. Query MediaStore Audio External & Internal
        val urisToQuery = listOf(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Audio.Media.INTERNAL_CONTENT_URI
        )

        for (contentUriToScan in urisToQuery) {
            try {
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.DISPLAY_NAME
                )

                context.contentResolver.query(
                    contentUriToScan,
                    projection,
                    null, // Avoid restrictive filter so user songs in Download/Music/WhatsApp are found
                    null,
                    "${MediaStore.Audio.Media.TITLE} ASC"
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                    val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                    val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                    val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                    val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                    val sizeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                    val displayNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)

                    while (cursor.moveToNext()) {
                        val mediaId = if (idColumn != -1) cursor.getLong(idColumn) else System.currentTimeMillis()
                        val displayName = if (displayNameColumn != -1) cursor.getString(displayNameColumn) ?: "" else ""
                        var title = if (titleColumn != -1) cursor.getString(titleColumn) ?: "" else ""
                        if (title.isBlank()) {
                            title = displayName.substringBeforeLast(".")
                        }
                        if (title.isBlank()) title = "Pista de Audio Local"

                        val rawArtist = if (artistColumn != -1) cursor.getString(artistColumn) ?: "" else ""
                        val artist = if (rawArtist.isNotBlank() && rawArtist != "<unknown>") rawArtist else "Almacenamiento Local"
                        val album = if (albumColumn != -1) cursor.getString(albumColumn) ?: "Música Local" else "Música Local"
                        val duration = if (durationColumn != -1) cursor.getLong(durationColumn) else 180000L
                        val path = if (dataColumn != -1) cursor.getString(dataColumn) ?: "" else ""
                        val sizeBytes = if (sizeColumn != -1) cursor.getLong(sizeColumn) else 0L

                        val trackContentUri = ContentUris.withAppendedId(contentUriToScan, mediaId)
                        val uriStr = trackContentUri.toString()

                        if (seenPathsOrUris.contains(uriStr) || (path.isNotBlank() && seenPathsOrUris.contains(path))) {
                            continue
                        }
                        if (path.isNotBlank()) seenPathsOrUris.add(path)
                        seenPathsOrUris.add(uriStr)

                        val format = when {
                            path.endsWith(".mp3", ignoreCase = true) || displayName.endsWith(".mp3", ignoreCase = true) -> "MP3"
                            path.endsWith(".m4a", ignoreCase = true) || displayName.endsWith(".m4a", ignoreCase = true) -> "M4A"
                            path.endsWith(".flac", ignoreCase = true) || displayName.endsWith(".flac", ignoreCase = true) -> "FLAC"
                            path.endsWith(".wav", ignoreCase = true) || displayName.endsWith(".wav", ignoreCase = true) -> "WAV"
                            path.endsWith(".aac", ignoreCase = true) || displayName.endsWith(".aac", ignoreCase = true) -> "AAC"
                            path.endsWith(".ogg", ignoreCase = true) || displayName.endsWith(".ogg", ignoreCase = true) -> "OGG"
                            else -> "AUDIO"
                        }

                        // Filter out zero-length or system ringtones shorter than 4 seconds if duration is known
                        if (duration > 0 && duration < 4000) continue

                        localTracks.add(
                            Track(
                                id = "local_media_${mediaId}_${uriStr.hashCode()}",
                                title = title,
                                artist = artist,
                                album = album,
                                durationMs = if (duration > 0) duration else 210000L,
                                audioUrl = uriStr,
                                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                                genre = "Local / Personal",
                                bitrate = "Local $format (Directo)",
                                isDownloaded = true,
                                localFilePath = path,
                                downloadSizeMb = (sizeBytes / (1024.0 * 1024.0)).coerceAtLeast(1.0),
                                audioFormat = format
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w("MusicRepository", "MediaStore scan exception: ${e.message}")
            }
        }

        // 2. Direct File System Traversal (e.g. /storage/emulated/0/Music, /storage/emulated/0/Download)
        val musicDirs = listOfNotNull(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            File("/storage/emulated/0/Music"),
            File("/storage/emulated/0/Download"),
            File("/storage/emulated/0/Audio"),
            File("/sdcard/Music")
        ).distinctBy { it.absolutePath }

        for (dir in musicDirs) {
            if (dir.exists() && dir.isDirectory) {
                scanDirectoryRecursively(dir, localTracks, seenPathsOrUris)
            }
        }

        Log.d("MusicRepository", "Total local audio tracks scanned: ${localTracks.size}")
        localTracks
    }

    private fun scanDirectoryRecursively(dir: File, localTracks: MutableList<Track>, seen: MutableSet<String>) {
        try {
            val files = dir.listFiles() ?: return
            for (file in files) {
                if (file.isDirectory) {
                    if (!file.name.startsWith(".")) {
                        scanDirectoryRecursively(file, localTracks, seen)
                    }
                } else {
                    val name = file.name.lowercase()
                    if (name.endsWith(".mp3") || name.endsWith(".m4a") || name.endsWith(".flac") ||
                        name.endsWith(".wav") || name.endsWith(".aac") || name.endsWith(".ogg")) {
                        val path = file.absolutePath
                        if (!seen.contains(path)) {
                            seen.add(path)
                            val title = file.nameWithoutExtension
                            val sizeMb = (file.length() / (1024.0 * 1024.0)).coerceAtLeast(0.5)
                            val format = file.extension.uppercase()

                            localTracks.add(
                                Track(
                                    id = "local_file_${path.hashCode()}",
                                    title = title,
                                    artist = dir.name.ifEmpty { "Almacenamiento Local" },
                                    album = dir.name,
                                    durationMs = 210000L,
                                    audioUrl = Uri.fromFile(file).toString(),
                                    coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                                    genre = "Local / Personal",
                                    bitrate = "Direct File $format",
                                    isDownloaded = true,
                                    localFilePath = path,
                                    downloadSizeMb = sizeMb,
                                    audioFormat = format
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("MusicRepository", "Folder scan error on ${dir.absolutePath}: ${e.message}")
        }
    }

    suspend fun importTracksFromUris(uris: List<Uri>): List<Track> = withContext(Dispatchers.IO) {
        val imported = mutableListOf<Track>()
        val retriever = MediaMetadataRetriever()
        for (uri in uris) {
            try {
                var displayName = "Canción Propia"
                var sizeBytes = 0L

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) displayName = cursor.getString(nameIndex) ?: displayName
                        if (sizeIndex != -1) sizeBytes = cursor.getLong(sizeIndex)
                    }
                }

                var title = displayName.substringBeforeLast(".")
                var artist = "Artista Local"
                var album = "Música Propia"
                var duration = 180000L

                try {
                    retriever.setDataSource(context, uri)
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.let { if (it.isNotBlank()) title = it }
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.let { if (it.isNotBlank()) artist = it }
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.let { if (it.isNotBlank()) album = it }
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.let { if (it > 0) duration = it }
                } catch (e: Exception) {
                    // fallback to display name
                }

                val ext = displayName.substringAfterLast(".", "mp3").uppercase()
                val sizeMb = if (sizeBytes > 0) (sizeBytes / (1024.0 * 1024.0)).coerceAtLeast(0.5) else 5.0

                imported.add(
                    Track(
                        id = "imported_uri_${uri.toString().hashCode()}",
                        title = title,
                        artist = artist,
                        album = album,
                        durationMs = duration,
                        audioUrl = uri.toString(),
                        coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                        genre = "Importado / Local",
                        bitrate = "Direct File $ext",
                        isDownloaded = true,
                        localFilePath = uri.toString(),
                        downloadSizeMb = sizeMb,
                        audioFormat = ext
                    )
                )
            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to import uri: $uri", e)
            }
        }
        try {
            retriever.release()
        } catch (e: Exception) {}
        imported
    }
}
