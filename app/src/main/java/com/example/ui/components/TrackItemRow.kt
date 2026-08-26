package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.ArkaiosGold
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TidalCyan

@Composable
fun TrackItemRow(
    track: Track,
    isCurrentTrack: Boolean,
    isPlaying: Boolean,
    downloadProgress: DownloadProgress?,
    onTrackClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDownloadClick: () -> Unit,
    onRemoveDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isCurrentTrack) Color(0x2606B6D4) else Color.Transparent)
            .clickable { onTrackClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("track_row_${track.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Cover with playing indicator overlay
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = track.coverUrl,
                    contentDescription = "Cover for ${track.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )

                if (isCurrentTrack) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color(0x9908080C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Currently Playing",
                            tint = if (isPlaying) CyanLight else TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title, Artist, and Badges
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    color = if (isCurrentTrack) CyanLight else TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = if (isCurrentTrack) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Platform Source Badge
                    val sourceText = when {
                        track.id.startsWith("yt_") -> "▶ YT Music"
                        track.id.startsWith("audius_") -> "☁ Audius"
                        track.id.startsWith("jamendo_") -> "🎵 Jamendo"
                        track.id.startsWith("archive_") -> "📁 Archive"
                        track.id.startsWith("tidal_") -> "⚡ TIDAL"
                        track.id.startsWith("local_") || track.localFilePath != null -> "💾 Local"
                        else -> "🌐 Cloud"
                    }
                    val sourceBgColor = when {
                        track.id.startsWith("yt_") -> Color(0x33EF4444)
                        track.id.startsWith("audius_") -> Color(0x33A855F7)
                        track.id.startsWith("jamendo_") -> Color(0x33F59E0B)
                        track.id.startsWith("archive_") -> Color(0x333B82F6)
                        track.id.startsWith("tidal_") -> Color(0x3306B6D4)
                        track.id.startsWith("local_") || track.localFilePath != null -> Color(0x3310B981)
                        else -> Color(0x3364748B)
                    }
                    val sourceTextColor = when {
                        track.id.startsWith("yt_") -> Color(0xFFFCA5A5)
                        track.id.startsWith("audius_") -> Color(0xFFE9D5FF)
                        track.id.startsWith("jamendo_") -> Color(0xFFFDE68A)
                        track.id.startsWith("archive_") -> Color(0xFF93C5FD)
                        track.id.startsWith("tidal_") -> CyanLight
                        track.id.startsWith("local_") || track.localFilePath != null -> EmeraldLight
                        else -> TextSecondary
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(sourceBgColor)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = sourceText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = sourceTextColor
                        )
                    }

                    // Audio format badge (FLAC / MP3 / M4A)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (track.audioFormat == "FLAC") Color(0x3306B6D4)
                                else if (track.audioFormat == "M4A") Color(0x3310B981)
                                else Color(0x2294A3B8)
                            )
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = track.audioFormat,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (track.audioFormat == "FLAC") CyanLight else if (track.audioFormat == "M4A") EmeraldLight else TextSecondary
                        )
                    }

                    Text(
                        text = "${track.artist} • ${track.album}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Action Buttons: Favorite, Download, Menu
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Favorite Button
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (track.isFavorite) Color(0xFFEF4444) else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Download Status Indicator / Button
            if (downloadProgress?.isDownloading == true) {
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { (downloadProgress.progressPercent / 100f).coerceIn(0.1f, 1f) },
                        modifier = Modifier.size(18.dp),
                        color = EmeraldLight,
                        strokeWidth = 2.dp
                    )
                }
            } else if (track.isDownloaded) {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Downloaded Offline",
                        tint = EmeraldLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onDownloadClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = "Download to Device",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Context Menu Dropdown
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Track Options",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(SurfaceDark)
                ) {
                    DropdownMenuItem(
                        text = { Text("Reproducir ahora", color = TextPrimary) },
                        onClick = {
                            showMenu = false
                            onTrackClick()
                        }
                    )
                    if (track.isDownloaded) {
                        DropdownMenuItem(
                            text = { Text("Eliminar de Descargas", color = Color(0xFFF87171)) },
                            onClick = {
                                showMenu = false
                                onRemoveDownloadClick()
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Descargar pista (${track.downloadSizeMb} MB)", color = EmeraldLight) },
                            onClick = {
                                showMenu = false
                                onDownloadClick()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(if (track.isFavorite) "Quitar de Favoritos" else "Añadir a Favoritos", color = TextPrimary) },
                        onClick = {
                            showMenu = false
                            onFavoriteToggle()
                        }
                    )
                }
            }
        }
    }
}
