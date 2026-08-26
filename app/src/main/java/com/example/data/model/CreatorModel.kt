package com.example.data.model

data class CreatorTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val durationSeconds: Int,
    val fileSizeMb: Double,
    val streamsCount: Long = 0,
    val amrEarned: Double = 0.0,
    val uploadDate: String,
    val gdriveFileId: String,
    val gdriveShareUrl: String,
    val coverUrl: String,
    val audioFormat: String = "FLAC 24-bit"
) {
    fun toCatalogTrack(): Track {
        return Track(
            id = id,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationSeconds * 1000L,
            audioUrl = gdriveShareUrl,
            coverUrl = coverUrl,
            genre = genre,
            bitrate = "FLAC Master 24-bit (Google Drive 5TB)",
            audioFormat = audioFormat,
            downloadSizeMb = fileSizeMb
        )
    }
}

data class CreatorCloudStats(
    val allocatedStorageGb: Double = 50.0,
    val usedStorageGb: Double = 1.84,
    val gdriveMasterFolderId: String = "14EVvOGfhLuhknCEz3uWqKkrNGPbukuWZ",
    val gdriveMasterFolderUrl: String = "https://drive.google.com/drive/folders/14EVvOGfhLuhknCEz3uWqKkrNGPbukuWZ?usp=sharing",
    val totalTracksUploaded: Int = 3,
    val totalGlobalStreams: Long = 1420,
    val totalAmrEarned: Double = 7.10,
    val royaltyRatePerStreamAmr: Double = 0.005,
    val isCreatorNodeActive: Boolean = true
)
