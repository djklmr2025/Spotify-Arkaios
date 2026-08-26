package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
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
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.SIZE
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND (${MediaStore.Audio.Media.DATA} LIKE '%.mp3' OR ${MediaStore.Audio.Media.DATA} LIKE '%.m4a' OR ${MediaStore.Audio.Media.DATA} LIKE '%.flac' OR ${MediaStore.Audio.Media.DATA} LIKE '%.wav')"

            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

                while (cursor.moveToNext()) {
                    val mediaId = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Local Audio Track"
                    val artist = cursor.getString(artistColumn) ?: "Device Storage"
                    val album = cursor.getString(albumColumn) ?: "Local Music"
                    val duration = cursor.getLong(durationColumn)
                    val path = cursor.getString(dataColumn)
                    val sizeBytes = cursor.getLong(sizeColumn)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mediaId)

                    val format = when {
                        path.endsWith(".mp3", ignoreCase = true) -> "MP3"
                        path.endsWith(".m4a", ignoreCase = true) -> "M4A"
                        path.endsWith(".flac", ignoreCase = true) -> "FLAC"
                        else -> "AUDIO"
                    }

                    localTracks.add(
                        Track(
                            id = "local_$mediaId",
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = if (duration > 0) duration else 180000L,
                            audioUrl = contentUri.toString(),
                            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                            genre = "Local Storage",
                            bitrate = "Local $format (Original)",
                            isDownloaded = true,
                            localFilePath = path,
                            downloadSizeMb = (sizeBytes / (1024.0 * 1024.0)).coerceAtLeast(1.0),
                            audioFormat = format
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        localTracks
    }
}
