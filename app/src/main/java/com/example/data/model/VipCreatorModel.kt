package com.example.data.model

data class VipCreator(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val followerCount: Int,
    val isFollowing: Boolean = false,
    val bio: String,
    val storageUsageGb: Double,
    val publishedTracksCount: Int,
    val topTrackTitle: String,
    val topTrackVotes: Int,
    val isVipAnnualMember: Boolean = true
)

data class KaraokeTrack(
    val id: String,
    val title: String,
    val artist: String,
    val sourcePlatform: String = "Karaoplay", // Karaoplay, YouTube Karaoke, Arkaios Cloud
    val sourceUrl: String,
    val coverUrl: String,
    val durationSeconds: Int,
    val hasSyncedLyrics: Boolean = true,
    val viewsCount: Long = 0
) {
    fun toTrack(): Track {
        return Track(
            id = id,
            title = "$title [Karaoke Version]",
            artist = artist,
            album = "Karaoke Studio 2026",
            durationMs = durationSeconds * 1000L,
            audioUrl = sourceUrl,
            coverUrl = coverUrl,
            genre = "Karaoke",
            bitrate = "Karaoplay HD Audio",
            audioFormat = "MP3 / FLAC",
            downloadSizeMb = 12.5
        )
    }
}
