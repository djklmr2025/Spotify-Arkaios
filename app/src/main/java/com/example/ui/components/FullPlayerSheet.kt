package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Track
import com.example.data.repository.DownloadProgress
import com.example.player.PlaybackState
import com.example.player.RepeatMode
import com.example.ui.theme.ArkaiosGoldLight
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BorderSubtleCyan
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FullPlayerSheet(
    playbackState: PlaybackState,
    downloadProgress: DownloadProgress?,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDownloadClick: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onEqualizerPresetSelect: (String) -> Unit,
    onTrackFromQueueClick: (Track) -> Unit = {},
    onAddToPlaylistClick: (Track) -> Unit = {}
) {
    val track = playbackState.currentTrack ?: return

    var activePlayerTab by remember { mutableIntStateOf(0) } // 0 = Reproductor, 1 = Cola de Sesión
    var isUserSeeking by remember { mutableStateOf(false) }
    var seekFraction by remember { mutableFloatStateOf(0f) }
    var showEqDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    val eqPresets = listOf(
        "Arkaios Master HiFi",
        "Bass Boost Ultra",
        "Cyberpunk Electronic",
        "Acoustic & Vocals",
        "Club Dance",
        "Flat Reference"
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("full_player_screen"),
        color = BackgroundDark
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF101422),
                            Color(0xFF0C0E17),
                            BackgroundDark
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Header Bar with Tab Switcher (Player vs Queue)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse Player",
                            tint = TextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Mode Switcher Pills (Reproductor | Cola de Sesión)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDark)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (activePlayerTab == 0) Color(0x3322D3EE) else Color.Transparent)
                                .clickable { activePlayerTab = 0 }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Reproductor",
                                color = if (activePlayerTab == 0) CyanLight else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (activePlayerTab == 1) Color(0x3322D3EE) else Color.Transparent)
                                .clickable { activePlayerTab = 1 }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = null,
                                    tint = if (activePlayerTab == 1) CyanLight else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Cola (${playbackState.queue.size.coerceAtLeast(1)})",
                                    color = if (activePlayerTab == 1) CyanLight else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { showEqDialog = true },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Equalizer",
                            tint = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (activePlayerTab == 0) {
                    // MAIN PLAYER VIEW
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 4.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Artwork Cover Card
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .shadow(elevation = 28.dp, shape = RoundedCornerShape(32.dp), spotColor = CyanLight)
                                .clip(RoundedCornerShape(32.dp))
                                .background(SurfaceCard),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = track.coverUrl,
                                contentDescription = "Album Art",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )

                            // Hi-Res Audio Badge Overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xD908080C))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (track.audioFormat == "FLAC") "24-BIT MASTER" else "HI-FI AUDIO",
                                    color = if (track.audioFormat == "FLAC") CyanLight else ArkaiosGoldLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Real-time Sound Wave Equalizer Visualizer
                        EqualizerVisualizer(
                            amplitudes = playbackState.waveformAmplitudes,
                            isPlaying = playbackState.isPlaying,
                            modifier = Modifier.padding(horizontal = 12.dp),
                            maxHeight = 36.dp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Title, Artist, Like & Add to Playlist Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = track.artist,
                                        color = TextSecondary,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0x1A22D3EE))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = track.bitrate,
                                            color = CyanLight,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { onAddToPlaylistClick(track) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QueueMusic,
                                        contentDescription = "Añadir a Playlist",
                                        tint = CyanLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                IconButton(
                                    onClick = onFavoriteToggle,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (track.isFavorite) Color(0xFFEF4444) else TextPrimary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Seekbar Slider
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val currentPos = if (isUserSeeking) {
                                (seekFraction * playbackState.durationMs).toLong()
                            } else {
                                playbackState.currentPositionMs
                            }

                            Slider(
                                value = if (isUserSeeking) seekFraction else playbackState.progressFraction,
                                onValueChange = { frac ->
                                    isUserSeeking = true
                                    seekFraction = frac
                                },
                                onValueChangeFinished = {
                                    val targetMs = (seekFraction * playbackState.durationMs).toLong()
                                    onSeekTo(targetMs)
                                    isUserSeeking = false
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = CyanLight,
                                    inactiveTrackColor = Color(0xFF1E293B)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatMs(currentPos),
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = formatMs(playbackState.durationMs),
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Playback Control Buttons (Shuffle, Prev, Play/Pause, Next, Repeat)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(onClick = onToggleShuffle) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = if (playbackState.isShuffle) CyanLight else TextMuted,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            IconButton(
                                onClick = onPreviousTrack,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Previous Track",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            // Main Play/Pause Button
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .shadow(elevation = 16.dp, shape = CircleShape, spotColor = Color.White)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { onTogglePlayPause() }
                                    .testTag("full_player_play_pause"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (playbackState.isBuffering) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = Color(0xFF08080C),
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                        tint = Color(0xFF08080C),
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onNextTrack,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next Track",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            IconButton(onClick = onCycleRepeat) {
                                Icon(
                                    imageVector = if (playbackState.repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                    contentDescription = "Repeat Mode",
                                    tint = if (playbackState.repeatMode != RepeatMode.OFF) CyanLight else TextMuted,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bottom Utility Row (Download Offline Encrypted, Speed Selector)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onDownloadClick() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (downloadProgress?.isDownloading == true) {
                                    CircularProgressIndicator(
                                        progress = { (downloadProgress.progressPercent / 100f).coerceIn(0.1f, 1f) },
                                        modifier = Modifier.size(16.dp),
                                        color = EmeraldLight,
                                        strokeWidth = 2.dp
                                    )
                                    Text("Cifrando Vault (${downloadProgress.progressPercent}%)...", color = EmeraldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else if (track.isDownloaded) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Downloaded", tint = EmeraldLight, modifier = Modifier.size(16.dp))
                                    Text("🔒 .arkcache Protegido", color = EmeraldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Outlined.Download, contentDescription = "Download", tint = TextPrimary, modifier = Modifier.size(16.dp))
                                    Text("Descargar (${track.downloadSizeMb} MB)", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x2606B6D4))
                                    .clickable { showSpeedDialog = true }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${playbackState.playbackSpeed}x Velocidad",
                                    color = CyanLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    // QUEUE / SESIÓN ACTIVA VIEW
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Sesión Activa en Cola",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Reproducción continua de álbum / lista",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceDark)
                                    .border(1.dp, BorderSubtleCyan, RoundedCornerShape(12.dp))
                                    .clickable { onToggleShuffle() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Shuffle, contentDescription = null, tint = CyanLight, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = if (playbackState.isShuffle) "Aleatorio On" else "Mezclar",
                                        color = CyanLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        val activeQueue = playbackState.queue.ifEmpty { listOf(track) }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 90.dp, top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(activeQueue) { idx, qTrack ->
                                val isPlayingThis = playbackState.currentTrack?.id == qTrack.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isPlayingThis) Color(0x1F22D3EE) else SurfaceElevated)
                                        .border(1.dp, if (isPlayingThis) BorderSubtleCyan else BorderSubtle, RoundedCornerShape(10.dp))
                                        .clickable { onTrackFromQueueClick(qTrack) }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        if (isPlayingThis) {
                                            Icon(
                                                imageVector = Icons.Default.GraphicEq,
                                                contentDescription = null,
                                                tint = CyanLight,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "${idx + 1}",
                                                color = TextMuted,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.width(18.dp)
                                            )
                                        }

                                        AsyncImage(
                                            model = qTrack.coverUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                        )

                                        Column {
                                            Text(
                                                text = qTrack.title,
                                                color = if (isPlayingThis) CyanLight else TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = if (isPlayingThis) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${qTrack.artist} • ${qTrack.formattedDuration}",
                                                color = TextSecondary,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { onAddToPlaylistClick(qTrack) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QueueMusic,
                                            contentDescription = "Añadir a playlist",
                                            tint = CyanLight,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Equalizer Presets Dialog
    if (showEqDialog) {
        AlertDialog(
            onDismissRequest = { showEqDialog = false },
            title = { Text("Ecualizador & Audio Engine", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Configuración actual: ${playbackState.equalizerPreset}",
                        color = CyanLight,
                        fontSize = 13.sp
                    )
                    eqPresets.forEach { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (playbackState.equalizerPreset == preset) Color(0x2606B6D4) else Color.Transparent)
                                .clickable {
                                    onEqualizerPresetSelect(preset)
                                    showEqDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset,
                                color = if (playbackState.equalizerPreset == preset) CyanLight else TextPrimary,
                                fontWeight = if (playbackState.equalizerPreset == preset) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEqDialog = false }) {
                    Text("Cerrar", color = CyanLight)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Speed Selector Dialog
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("Velocidad de Reproducción", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (playbackState.playbackSpeed == speed) Color(0x2606B6D4) else Color.Transparent)
                                .clickable {
                                    onSpeedChange(speed)
                                    showSpeedDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${speed}x Normal",
                                color = if (playbackState.playbackSpeed == speed) CyanLight else TextPrimary,
                                fontWeight = if (playbackState.playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Listo", color = CyanLight)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
