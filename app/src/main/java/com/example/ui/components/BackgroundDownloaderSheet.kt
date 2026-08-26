package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.downloader.BackgroundDownloadTask
import com.example.data.downloader.BackgroundEngineStats
import com.example.data.downloader.DownloadEngineSource
import com.example.data.downloader.DownloadTaskStatus
import com.example.data.model.Track
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BorderSubtleCyan
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.ErrorColor
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BackgroundDownloaderSheet(
    tasks: List<BackgroundDownloadTask>,
    engineStats: BackgroundEngineStats,
    onEnqueueUrl: (url: String, format: String, engine: DownloadEngineSource) -> Unit,
    onCancelTask: (taskId: String) -> Unit,
    onRetryTask: (taskId: String) -> Unit,
    onClearCompleted: () -> Unit,
    onPlayTrack: (Track) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var inputUrl by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf("MP3") }
    var selectedEngine by remember { mutableStateOf(DownloadEngineSource.AUTO_FALLBACK) }

    val formats = listOf("MP3", "FLAC", "M4A")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDA08080C))
            .padding(top = 28.dp, start = 12.dp, end = 12.dp, bottom = 12.dp)
            .testTag("background_downloader_modal"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .shadow(24.dp, shape = RoundedCornerShape(24.dp), spotColor = CyanPrimary)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, BorderSubtleCyan, RoundedCornerShape(24.dp)),
            color = SurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x2606B6D4))
                                .border(1.dp, BorderSubtleCyan, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Downloader",
                                tint = CyanLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Motor de Descargas en 2do Plano",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x2610B981))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Activo", color = EmeraldLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = "yt-dlp Core • Arkaios Gateway • SpotiDownloader",
                                color = CyanLight,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Real-time Engine Metrics & Web Sources Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = CyanLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (engineStats.totalSpeedKbps > 0) "%.1f MB/s".format(engineStats.totalSpeedKbps / 1024.0) else "0.0 KB/s",
                                color = CyanLight,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• ${engineStats.activeDownloadsCount} activas • ${engineStats.completedCount} listas",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        if (tasks.any { it.status == DownloadTaskStatus.COMPLETED }) {
                            Text(
                                text = "Limpiar listas",
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable { onClearCompleted() }
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // External Source Quick Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EngineSourceChip(
                        label = "arkaios.qzz.io",
                        url = "https://arkaios.qzz.io/w4ltmI1",
                        badge = "Shortener",
                        modifier = Modifier.weight(1f)
                    ) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://arkaios.qzz.io/w4ltmI1"))
                        context.startActivity(intent)
                    }

                    EngineSourceChip(
                        label = "spotidownloader",
                        url = "https://spotidownloader.com/en19",
                        badge = "Web API",
                        modifier = Modifier.weight(1f)
                    ) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://spotidownloader.com/en19"))
                        context.startActivity(intent)
                    }

                    EngineSourceChip(
                        label = "yt-dlp releases",
                        url = "https://github.com/yt-dlp/yt-dlp/releases",
                        badge = "Core Engine",
                        modifier = Modifier.weight(1f)
                    ) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yt-dlp/yt-dlp/releases"))
                        context.startActivity(intent)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input URL & Engine Selection
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Descargar desde enlace o pista reproducible:",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            placeholder = { Text("Pega enlace (Spotify, Arkaios, yt-dlp, web audio)...", color = TextMuted, fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("downloader_url_input"),
                            leadingIcon = {
                                Icon(Icons.Default.Link, contentDescription = null, tint = CyanLight, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (inputUrl.isNotEmpty()) {
                                    IconButton(onClick = { inputUrl = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanLight,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark
                            ),
                            singleLine = true
                        )

                        // Format & Engine selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                formats.forEach { fmt ->
                                    val isSelected = selectedFormat == fmt
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) CyanPrimary else SurfaceDark)
                                            .border(1.dp, if (isSelected) CyanLight else BorderSubtle, RoundedCornerShape(8.dp))
                                            .clickable { selectedFormat = fmt }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = fmt,
                                            color = if (isSelected) Color(0xFF08080C) else TextSecondary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (inputUrl.isNotBlank()) {
                                        onEnqueueUrl(inputUrl.trim(), selectedFormat, selectedEngine)
                                        inputUrl = ""
                                    }
                                },
                                enabled = inputUrl.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyanPrimary,
                                    contentColor = Color(0xFF08080C),
                                    disabledContainerColor = Color(0x2606B6D4),
                                    disabledContentColor = TextMuted
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("start_background_download_button")
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Descargar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tasks List Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cola de Tareas en 2do Plano (${tasks.size})",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Sin descargas activas",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Pulsa 'Descargar' en cualquier pista reproducible o pega un enlace arriba para descargar en segundo plano.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tasks.reversed(), key = { it.id }) { task ->
                            DownloadTaskItemRow(
                                task = task,
                                onCancel = { onCancelTask(task.id) },
                                onRetry = { onRetryTask(task.id) },
                                onPlay = {
                                    val track = Track(
                                        id = task.trackId,
                                        title = task.trackTitle,
                                        artist = task.artist,
                                        album = task.album,
                                        durationMs = 210000L,
                                        audioUrl = task.resolvedStreamUrl ?: task.originalSourceUrl,
                                        coverUrl = task.coverUrl,
                                        genre = "Descarga",
                                        isDownloaded = true,
                                        localFilePath = task.localFilePath,
                                        audioFormat = task.format
                                    )
                                    onPlayTrack(track)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadTaskItemRow(
    task: BackgroundDownloadTask,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onPlay: () -> Unit
) {
    val isRunning = task.status == DownloadTaskStatus.DOWNLOADING ||
            task.status == DownloadTaskStatus.RESOLVING_STREAM ||
            task.status == DownloadTaskStatus.PROCESSING_AUDIO

    val isCompleted = task.status == DownloadTaskStatus.COMPLETED
    val isFailed = task.status == DownloadTaskStatus.FAILED || task.status == DownloadTaskStatus.CANCELLED

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(
                1.dp,
                if (isRunning) BorderSubtleCyan else if (isCompleted) Color(0x3310B981) else BorderSubtle,
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cover
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceDark)
                ) {
                    AsyncImage(
                        model = task.coverUrl,
                        contentDescription = task.trackTitle,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.trackTitle,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${task.artist} • ${task.format} ${task.quality}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = task.statusMessage,
                        color = if (isRunning) CyanLight else if (isCompleted) EmeraldLight else TextMuted,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status Actions
                when {
                    isCompleted -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x2610B981))
                                .clickable { onPlay() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir", tint = EmeraldLight, modifier = Modifier.size(14.dp))
                                Text("Oír", color = EmeraldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    isRunning -> {
                        IconButton(
                            onClick = onCancel,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                    isFailed -> {
                        IconButton(
                            onClick = onRetry,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reintentar", tint = CyanLight, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Progress bar and stats if running
            if (isRunning) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { task.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CyanPrimary,
                        trackColor = Color(0x3306B6D4)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${task.progressPercent}% • ${task.formattedDownloadedSize}",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "⚡ ${task.formattedSpeed}",
                            color = CyanLight,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EngineSourceChip(
    label: String,
    url: String,
    badge: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = CyanLight,
                    modifier = Modifier.size(10.dp)
                )
            }
            Text(
                text = badge,
                color = CyanLight,
                fontSize = 9.sp
            )
        }
    }
}
