package com.example.data.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val audioUrl: String,
    val coverUrl: String,
    val genre: String,
    val isExplicit: Boolean = false,
    val bitrate: String = "320 kbps (Hi-Fi)",
    val isDownloaded: Boolean = false,
    val localFilePath: String? = null,
    val tidalId: String? = null,
    val isFavorite: Boolean = false,
    val downloadSizeMb: Double = 8.4,
    val audioFormat: String = "MP3"
) {
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val coverUrl: String,
    val year: String,
    val genre: String,
    val trackCount: Int
)

data class GenreCategory(
    val id: String,
    val name: String,
    val iconName: String,
    val gradientStartHex: Long,
    val gradientEndHex: Long
)
