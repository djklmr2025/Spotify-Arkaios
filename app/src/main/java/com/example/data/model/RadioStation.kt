package com.example.data.model

data class RadioStation(
    val id: String,
    val name: String,
    val genre: String,
    val streamUrl: String,
    val coverUrl: String,
    val description: String = "Estación de Radio en Vivo",
    val bitrate: String = "128 kbps AAC/MP3",
    val listenersCount: String = "15.4k oyentes",
    val isLive: Boolean = true
) {
    fun toTrack(): Track {
        return Track(
            id = "radio_$id",
            title = name,
            artist = "Jango Live • $genre",
            album = "Emisora en Vivo",
            durationMs = 0L, // 0L indicates live continuous stream
            audioUrl = streamUrl,
            coverUrl = coverUrl,
            genre = genre,
            bitrate = "Radio Stream $bitrate",
            audioFormat = "LIVE STREAM",
            isDownloaded = false
        )
    }
}
