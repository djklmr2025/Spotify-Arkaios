package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Track

@Entity(tableName = "cached_tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val audioUrl: String,
    val coverUrl: String,
    val genre: String,
    val isExplicit: Boolean,
    val bitrate: String,
    val isDownloaded: Boolean,
    val localFilePath: String?,
    val tidalId: String?,
    val isFavorite: Boolean,
    val downloadSizeMb: Double,
    val audioFormat: String,
    val addedTimestamp: Long = System.currentTimeMillis()
) {
    fun toTrack(): Track {
        return Track(
            id = id,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            audioUrl = audioUrl,
            coverUrl = coverUrl,
            genre = genre,
            isExplicit = isExplicit,
            bitrate = bitrate,
            isDownloaded = isDownloaded,
            localFilePath = localFilePath,
            tidalId = tidalId,
            isFavorite = isFavorite,
            downloadSizeMb = downloadSizeMb,
            audioFormat = audioFormat
        )
    }

    companion object {
        fun fromTrack(track: Track): TrackEntity {
            return TrackEntity(
                id = track.id,
                title = track.title,
                artist = track.artist,
                album = track.album,
                durationMs = track.durationMs,
                audioUrl = track.audioUrl,
                coverUrl = track.coverUrl,
                genre = track.genre,
                isExplicit = track.isExplicit,
                bitrate = track.bitrate,
                isDownloaded = track.isDownloaded,
                localFilePath = track.localFilePath,
                tidalId = track.tidalId,
                isFavorite = track.isFavorite,
                downloadSizeMb = track.downloadSizeMb,
                audioFormat = track.audioFormat
            )
        }
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val author: String,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackId"]
)
data class PlaylistTrackCrossRefEntity(
    val playlistId: String,
    val trackId: String,
    val addedTimestamp: Long = System.currentTimeMillis()
)

