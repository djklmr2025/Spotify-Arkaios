package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import android.util.Log
import com.example.data.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPercentage: Int = 0,
    val repeatMode: RepeatMode = RepeatMode.ALL,
    val isShuffle: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val equalizerPreset: String = "Arkaios Master HiFi",
    val waveformAmplitudes: List<Float> = List(32) { 0.15f },
    val queue: List<Track> = emptyList(),
    val queueIndex: Int = 0
) {
    val progressFraction: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}

class AudioPlayerEngine(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var playlistQueue: List<Track> = emptyList()
    private var originalQueue: List<Track> = emptyList()
    private var currentQueueIndex: Int = 0

    init {
        initMediaPlayer()
    }

    private fun initMediaPlayer() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnPreparedListener { mp ->
                    _playbackState.value = _playbackState.value.copy(
                        isBuffering = false,
                        durationMs = mp.duration.toLong().coerceAtLeast(1000L)
                    )
                    mp.start()
                    _playbackState.value = _playbackState.value.copy(isPlaying = true)
                    startProgressTracker()
                }
                setOnBufferingUpdateListener { _, percent ->
                    _playbackState.value = _playbackState.value.copy(bufferedPercentage = percent)
                }
                setOnCompletionListener {
                    handleTrackCompletion()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayerEngine", "MediaPlayer error: what=$what, extra=$extra")
                    _playbackState.value = _playbackState.value.copy(isBuffering = false, isPlaying = false)
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerEngine", "Failed to init MediaPlayer", e)
        }
    }

    fun setQueue(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        originalQueue = tracks
        currentQueueIndex = startIndex.coerceIn(0, tracks.lastIndex)
        playlistQueue = if (_playbackState.value.isShuffle) {
            val current = tracks[currentQueueIndex]
            val rest = tracks.filterIndexed { i, _ -> i != currentQueueIndex }.shuffled()
            listOf(current) + rest
        } else {
            tracks
        }
        val targetTrack = playlistQueue[currentQueueIndex.coerceIn(0, playlistQueue.lastIndex)]
        _playbackState.value = _playbackState.value.copy(
            queue = playlistQueue,
            queueIndex = currentQueueIndex
        )
        playTrack(targetTrack)
    }

    fun playTrack(track: Track) {
        try {
            val qIdx = playlistQueue.indexOfFirst { it.id == track.id }.let { if (it >= 0) it else currentQueueIndex }
            currentQueueIndex = qIdx
            _playbackState.value = _playbackState.value.copy(
                currentTrack = track,
                isBuffering = true,
                currentPositionMs = 0L,
                durationMs = track.durationMs,
                queue = if (playlistQueue.isEmpty()) listOf(track) else playlistQueue,
                queueIndex = currentQueueIndex
            )

            mediaPlayer?.reset()

            val audioUri: Uri = if (track.isDownloaded && track.localFilePath != null && File(track.localFilePath).exists()) {
                val localFile = File(track.localFilePath)
                val playableFile = com.example.data.crypto.ArkaiosOfflineCryptoEngine.getDecryptedPlayableFile(context, localFile)
                if (playableFile != null && playableFile.exists()) {
                    Uri.fromFile(playableFile)
                } else {
                    Uri.fromFile(localFile)
                }
            } else {
                Uri.parse(track.audioUrl)
            }

            mediaPlayer?.setDataSource(context, audioUri)
            mediaPlayer?.prepareAsync()
        } catch (e: Exception) {
            Log.e("AudioPlayerEngine", "Error playing track ${track.title}", e)
            _playbackState.value = _playbackState.value.copy(isBuffering = false, isPlaying = false)
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _playbackState.value = _playbackState.value.copy(isPlaying = false)
            stopProgressTracker()
        } else {
            if (_playbackState.value.currentTrack != null) {
                player.start()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
                startProgressTracker()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val player = mediaPlayer ?: return
        val clamped = positionMs.coerceIn(0L, _playbackState.value.durationMs)
        player.seekTo(clamped.toInt())
        _playbackState.value = _playbackState.value.copy(currentPositionMs = clamped)
    }

    fun nextTrack() {
        if (playlistQueue.isEmpty()) return
        when (_playbackState.value.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0L)
                mediaPlayer?.start()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
            }
            else -> {
                if (currentQueueIndex < playlistQueue.lastIndex) {
                    currentQueueIndex++
                    playTrack(playlistQueue[currentQueueIndex])
                } else if (_playbackState.value.repeatMode == RepeatMode.ALL) {
                    currentQueueIndex = 0
                    playTrack(playlistQueue[0])
                }
            }
        }
    }

    fun previousTrack() {
        if (playlistQueue.isEmpty()) return
        if (_playbackState.value.currentPositionMs > 3000L) {
            seekTo(0L)
            return
        }
        if (currentQueueIndex > 0) {
            currentQueueIndex--
            playTrack(playlistQueue[currentQueueIndex])
        } else if (_playbackState.value.repeatMode == RepeatMode.ALL) {
            currentQueueIndex = playlistQueue.lastIndex
            playTrack(playlistQueue[currentQueueIndex])
        } else {
            seekTo(0L)
        }
    }

    fun toggleShuffle() {
        val newShuffle = !_playbackState.value.isShuffle
        _playbackState.value = _playbackState.value.copy(isShuffle = newShuffle)
        val currentTrack = _playbackState.value.currentTrack
        if (newShuffle) {
            val rest = originalQueue.filter { it.id != currentTrack?.id }.shuffled()
            playlistQueue = if (currentTrack != null) listOf(currentTrack) + rest else originalQueue.shuffled()
            currentQueueIndex = 0
        } else {
            playlistQueue = originalQueue
            currentQueueIndex = playlistQueue.indexOfFirst { it.id == currentTrack?.id }.coerceAtLeast(0)
        }
    }

    fun cycleRepeatMode() {
        val nextMode = when (_playbackState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playbackState.value = _playbackState.value.copy(repeatMode = nextMode)
    }

    fun setPlaybackSpeed(speed: Float) {
        try {
            mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(speed) ?: return
            _playbackState.value = _playbackState.value.copy(playbackSpeed = speed)
        } catch (e: Exception) {
            Log.w("AudioPlayerEngine", "Playback speed not supported", e)
        }
    }

    fun setEqualizerPreset(preset: String) {
        _playbackState.value = _playbackState.value.copy(equalizerPreset = preset)
    }

    private fun handleTrackCompletion() {
        when (_playbackState.value.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0L)
                mediaPlayer?.start()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
            }
            RepeatMode.ALL -> {
                nextTrack()
            }
            RepeatMode.OFF -> {
                if (currentQueueIndex < playlistQueue.lastIndex) {
                    nextTrack()
                } else {
                    _playbackState.value = _playbackState.value.copy(isPlaying = false, currentPositionMs = 0L)
                    stopProgressTracker()
                }
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val player = mediaPlayer
                if (player != null && player.isPlaying) {
                    val currentPos = player.currentPosition.toLong()
                    val duration = player.duration.toLong().coerceAtLeast(1000L)
                    
                    // Generate fluid dynamic waveform bars
                    val newAmplitudes = List(32) { i ->
                        val base = 0.2f + 0.7f * (0.5f + 0.5f * kotlin.math.sin((currentPos / 180.0 + i * 0.4).toFloat()))
                        val jitter = Random.nextFloat() * 0.15f
                        (base + jitter).coerceIn(0.08f, 1.0f)
                    }

                    _playbackState.value = _playbackState.value.copy(
                        currentPositionMs = currentPos,
                        durationMs = duration,
                        waveformAmplitudes = newAmplitudes
                    )
                }
                delay(100)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressTracker()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
