package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.Album
import com.example.data.model.Track
import com.example.data.repository.DownloadProgress
import com.example.player.PlaybackState
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
fun AlbumDetailModal(
    album: Album,
    albumTracks: List<Track>,
    playbackState: PlaybackState,
    downloadStatus: Map<String, DownloadProgress>,
    onDismiss: () -> Unit,
    onPlayAll: (List<Track>, Boolean) -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onDownloadAlbum: (List<Track>) -> Unit,
    onFavoriteToggle: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit
) {
    val totalAlbumDurationMs = albumTracks.sumOf { it.durationMs }
    val totalMinutes = totalAlbumDurationMs / 60000
    val allDownloaded = albumTracks.isNotEmpty() && albumTracks.all { it.isDownloaded }
    val isAnyDownloading = albumTracks.any { downloadStatus[it.id]?.isDownloading == true }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("album_detail_modal"),
        color = BackgroundDark
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF13182B),
                            Color(0xFF0C0F1D),
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
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextPrimary
                        )
                    }

                    Text(
                        text = "ÁLBUM COMPLETO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanLight,
                        letterSpacing = 2.sp
                    )

                    IconButton(onClick = { onDownloadAlbum(albumTracks) }, modifier = Modifier.size(40.dp)) {
                        if (isAnyDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = EmeraldLight,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (allDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                                contentDescription = "Descargar Álbum",
                                tint = if (allDownloaded) EmeraldLight else CyanLight
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp, start = 16.dp, end = 16.dp, top = 8.dp)
                ) {
                    // Album Artwork & Metadata Card
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(24.dp), spotColor = CyanLight)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(SurfaceCard),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = album.coverUrl,
                                    contentDescription = album.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xD908080C))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "FLAC 24-BIT",
                                        color = CyanLight,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = album.title,
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = album.artist,
                                color = CyanLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "${album.year} • ${album.genre} • ${albumTracks.size} canciones (${totalMinutes} min)",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action Buttons (Play Album, Shuffle Album, Download Full Album)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { onPlayAll(albumTracks, false) },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF08080C)),
                                    shape = RoundedCornerShape(22.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Text("Reproducir", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }

                                Button(
                                    onClick = { onPlayAll(albumTracks, true) },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = TextPrimary),
                                    shape = RoundedCornerShape(22.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Shuffle, contentDescription = null, tint = CyanLight, modifier = Modifier.size(18.dp))
                                        Text("Aleatorio", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }

                            // Download Full Album Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x1F10B981))
                                    .border(1.dp, Color(0x3310B981), RoundedCornerShape(12.dp))
                                    .clickable { onDownloadAlbum(albumTracks) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(
                                            imageVector = if (allDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                                            contentDescription = null,
                                            tint = EmeraldLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column {
                                            Text(
                                                text = if (allDownloaded) "Álbum completo en Caché Offline (.arkcache)" else "Descargar Álbum Completo en Caché (.arkcache)",
                                                color = EmeraldLight,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Cifrado con llave Vault DRM Arkaios para reproducción local segura",
                                                color = TextSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Track List
                    item {
                        Text(
                            text = "Pistas del Álbum:",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    itemsIndexed(albumTracks) { index, track ->
                        val isCurrent = playbackState.currentTrack?.id == track.id
                        val isPlaying = isCurrent && playbackState.isPlaying

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCurrent) Color(0x1F22D3EE) else SurfaceElevated)
                                .border(1.dp, if (isCurrent) BorderSubtleCyan else BorderSubtle, RoundedCornerShape(12.dp))
                                .clickable { onTrackClick(track, albumTracks) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = if (isCurrent) CyanLight else TextMuted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(20.dp)
                                )

                                Column {
                                    Text(
                                        text = track.title,
                                        color = if (isCurrent) CyanLight else TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = track.artist,
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "• ${track.formattedDuration}",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                        if (track.isDownloaded) {
                                            Text(
                                                text = "• 🔒 .arkcache",
                                                color = EmeraldLight,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { onAddToPlaylist(track) }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.QueueMusic,
                                        contentDescription = "Añadir a Playlist",
                                        tint = CyanLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(onClick = { onFavoriteToggle(track) }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Favorito",
                                        tint = if (track.isFavorite) Color(0xFFEF4444) else TextSecondary,
                                        modifier = Modifier.size(18.dp)
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
