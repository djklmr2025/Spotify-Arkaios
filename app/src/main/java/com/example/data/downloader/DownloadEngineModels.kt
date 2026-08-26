package com.example.data.downloader

enum class DownloadEngineSource(val displayName: String, val endpointUrl: String, val description: String) {
    AUTO_FALLBACK(
        "Auto Fallback Inteligente",
        "https://arkaios.qzz.io/w4ltmI1",
        "Prueba Arkaios Gateway -> SpotiDownloader -> yt-dlp releases"
    ),
    ARKAIOS_GATEWAY(
        "Arkaios Shortener Gateway",
        "https://arkaios.qzz.io/w4ltmI1",
        "Puente acortador Arkaios Cloud Stream"
    ),
    SPOTI_DOWNLOADER(
        "SpotiDownloader Engine",
        "https://spotidownloader.com/en19",
        "Extractor directo de metadatos y audio Hi-Fi"
    ),
    YT_DLP_ENGINE(
        "yt-dlp Release Core Engine",
        "https://github.com/yt-dlp/yt-dlp/releases",
        "Motor universal de extracción de streams de audio"
    )
}

enum class DownloadTaskStatus {
    QUEUED,
    RESOLVING_STREAM,
    EXTRACTING_METADATA,
    DOWNLOADING,
    PROCESSING_AUDIO,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class BackgroundDownloadTask(
    val id: String,
    val trackId: String,
    val trackTitle: String,
    val artist: String,
    val album: String = "Arkaios Sound Lab",
    val coverUrl: String,
    val originalSourceUrl: String,
    val resolvedStreamUrl: String? = null,
    val format: String = "MP3",
    val quality: String = "320 kbps (Hi-Fi)",
    val progressPercent: Int = 0,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val speedKbps: Double = 0.0,
    val status: DownloadTaskStatus = DownloadTaskStatus.QUEUED,
    val statusMessage: String = "En cola de descarga en 2do plano...",
    val activeEngine: DownloadEngineSource = DownloadEngineSource.AUTO_FALLBACK,
    val localFilePath: String? = null,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val completedTimestamp: Long? = null,
    val errorMessage: String? = null
) {
    val formattedDownloadedSize: String
        get() {
            val mb = downloadedBytes / (1024.0 * 1024.0)
            val totalMb = if (totalBytes > 0) totalBytes / (1024.0 * 1024.0) else 0.0
            return if (totalMb > 0) "%.1f / %.1f MB".format(mb, totalMb) else "%.1f MB".format(mb)
        }

    val formattedSpeed: String
        get() {
            return if (speedKbps >= 1024) {
                "%.1f MB/s".format(speedKbps / 1024.0)
            } else {
                "%.0f KB/s".format(speedKbps)
            }
        }
}

data class BackgroundEngineStats(
    val isRunning: Boolean = true,
    val activeDownloadsCount: Int = 0,
    val queuedCount: Int = 0,
    val completedCount: Int = 0,
    val totalSpeedKbps: Double = 0.0,
    val currentEngine: DownloadEngineSource = DownloadEngineSource.AUTO_FALLBACK,
    val lastExtractedUrl: String? = null
)

data class ExtractedStreamResult(
    val success: Boolean,
    val streamUrl: String,
    val title: String,
    val artist: String,
    val album: String,
    val coverUrl: String,
    val durationMs: Long,
    val format: String,
    val bitrate: String,
    val usedEngine: DownloadEngineSource,
    val engineLog: String
)
