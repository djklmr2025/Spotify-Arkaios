package com.example.data.tidal

enum class TidalAudioQuality(val code: String, val displayName: String, val bitDepth: String) {
    LOW("LOW", "96 kbps (AAC)", "16-bit"),
    HIGH("HIGH", "320 kbps (AAC Hi-Fi)", "16-bit"),
    LOSSLESS("LOSSLESS", "1411 kbps (FLAC HiFi)", "16-bit / 44.1kHz"),
    HI_RES("HI_RES", "Up to 9216 kbps (Master FLAC)", "24-bit / 192kHz")
}

enum class AudioServerProvider(
    val id: String,
    val displayName: String,
    val defaultEndpoint: String,
    val description: String
) {
    TIDAL_OFFICIAL(
        "tidal_official",
        "TIDAL Hi-Fi API Direct",
        "https://api.tidal.com/v1",
        "Servidores directos de TIDAL con token nativo y calidad Master FLAC"
    ),
    NAVIDROME_SUBSONIC(
        "navidrome_subsonic",
        "Navidrome / Subsonic Server",
        "https://demo.navidrome.org/rest",
        "Servidor de música autohospedado compatible con API Subsonic"
    ),
    AMPACHE_SERVER(
        "ampache_server",
        "Ampache Cloud Server",
        "https://play.ampache.org/api",
        "Servidor libre de streaming de audio y biblioteca remota"
    ),
    KOEL_SERVER(
        "koel_server",
        "Koel Music Server",
        "https://music.koel.dev/api",
        "Plataforma moderna de streaming personal para colecciones privadas"
    ),
    SWING_MUSIC(
        "swing_music",
        "SwingMusic Server",
        "https://swingmusic.org/api",
        "Servidor ligero de música en la nube de alto rendimiento"
    )
}

data class TidalConfig(
    val clientToken: String = "kgsOOmYk3zShYrNP",
    val accessToken: String = "",
    val refreshToken: String = "",
    val countryCode: String = "US",
    val audioQuality: TidalAudioQuality = TidalAudioQuality.LOSSLESS,
    val selectedProvider: AudioServerProvider = AudioServerProvider.TIDAL_OFFICIAL,
    val customServerUrl: String = "",
    val serverUsername: String = "",
    val isConnected: Boolean = true
)

data class TidalPlaybackStreamInfo(
    val trackId: String,
    val streamUrl: String,
    val audioQuality: String,
    val audioMode: String,
    val bitDepth: Int = 16,
    val sampleRate: Int = 44100,
    val codec: String = "FLAC",
    val expiresAt: Long = System.currentTimeMillis() + (3600 * 1000L),
    val serverResponseTimeMs: Long = 120L
)

data class TidalSearchResult(
    val tracks: List<com.example.data.model.Track> = emptyList(),
    val totalCount: Int = 0,
    val queryTimeMs: Long = 0L,
    val usedToken: String = "kgsOOmYk3zShYrNP"
)
