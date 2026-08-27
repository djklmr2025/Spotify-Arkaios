package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Album
import com.example.data.model.AmrWallet
import com.example.data.model.GenreCategory
import com.example.data.model.Track
import com.example.data.repository.DownloadProgress
import com.example.player.PlaybackState
import com.example.ui.components.TrackItemRow
import com.example.ui.theme.ArkaiosGold
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BorderSubtleCyan
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TidalCyan

@Composable
fun HomeScreen(
    tracks: List<Track>,
    featuredAlbums: List<Album>,
    genres: List<GenreCategory>,
    wallet: AmrWallet,
    playbackState: PlaybackState,
    downloadStatus: Map<String, DownloadProgress>,
    radioStations: List<com.example.data.model.RadioStation> = emptyList(),
    onTrackClick: (Track, List<Track>?) -> Unit = { _, _ -> },
    onAlbumClick: (Album) -> Unit = {},
    onRadioClick: (com.example.data.model.RadioStation) -> Unit = {},
    onFavoriteToggle: (Track) -> Unit,
    onDownloadClick: (Track) -> Unit,
    onRemoveDownloadClick: (Track) -> Unit,
    onNavigateToAmrStore: () -> Unit,
    onGenreSelect: (String) -> Unit,
    onOpenTidalModal: () -> Unit = {},
    onOpenCreatorStudio: () -> Unit = {},
    onOpenCommunityVoting: () -> Unit = {},
    onOpenVipCreators: () -> Unit = {},
    onOpenKaraokeStudio: () -> Unit = {},
    onOpenAuthModal: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header with User Profile, Creator Studio, TIDAL API pill, and AMR Balance Badge
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .clickable { onOpenAuthModal() }
                        .testTag("home_user_profile_header")
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                        contentDescription = "Perfil Usuario",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, CyanLight, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = wallet.userName.ifEmpty { "Arkaios Master" },
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (wallet.isGodOwnerLicensed) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "God Owner",
                                    tint = CyanLight,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = if (wallet.isGodOwnerLicensed) "⚡ Membresía Anual 100GB VIP" else "100GB Nube • Google Drive",
                            color = if (wallet.isGodOwnerLicensed) CyanLight else EmeraldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    // VIP Creators Directory Pill Button (Karaoplay Style)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x26F59E0B))
                            .border(1.dp, Color(0x66F59E0B), RoundedCornerShape(20.dp))
                            .clickable { onOpenVipCreators() }
                            .padding(horizontal = 7.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("👥", fontSize = 10.sp)
                            Text(
                                text = "VIP Creadores",
                                color = ArkaiosGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Karaoke Studio Pill Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x26EC4899))
                            .border(1.dp, Color(0x66EC4899), RoundedCornerShape(20.dp))
                            .clickable { onOpenKaraokeStudio() }
                            .padding(horizontal = 7.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("🎤", fontSize = 10.sp)
                            Text(
                                text = "Karaoke",
                                color = Color(0xFFF472B6),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Community Voting Pill Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x2610B981))
                            .border(1.dp, Color(0x6610B981), RoundedCornerShape(20.dp))
                            .clickable { onOpenCommunityVoting() }
                            .padding(horizontal = 7.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("🌟", fontSize = 10.sp)
                            Text(
                                text = "Votos",
                                color = EmeraldLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Creator Studio Pill Button (Google Drive 100GB)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x26F59E0B))
                            .border(1.dp, Color(0x66F59E0B), RoundedCornerShape(20.dp))
                            .clickable { onOpenCreatorStudio() }
                            .padding(horizontal = 7.dp, vertical = 5.dp)
                            .testTag("home_creator_studio_pill_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "100GB Studio",
                                color = ArkaiosGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // TIDAL API Pill Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDark)
                            .border(1.dp, BorderSubtleCyan, RoundedCornerShape(20.dp))
                            .clickable { onOpenTidalModal() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("home_tidal_pill_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🌊", fontSize = 11.sp)
                            Text(
                                text = "TIDAL",
                                color = CyanLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // AMR Balance Quick Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDark)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                            .clickable { onNavigateToAmrStore() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("home_amr_pill_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🪙", fontSize = 11.sp)
                            Text(
                                text = "${"%.2f".format(wallet.balance)} AMR",
                                color = CyanLight,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Hero Featured Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF0E1A2E), Color(0xFF131726), Color(0xFF1B1832))
                        )
                    )
                    .border(1.dp, BorderSubtleCyan, RoundedCornerShape(24.dp))
                    .clickable {
                        tracks.firstOrNull()?.let { onTrackClick(it, tracks) }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x2606B6D4))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "LANZAMIENTO DESTACADO",
                                color = CyanLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Cybernetic Horizon (Master FLAC)",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Arkaios Sound Lab • Audio 24-Bit / 192kHz",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(elevation = 8.dp, shape = CircleShape, spotColor = Color.White)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Hero",
                            tint = Color(0xFF08080C),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Horizontal Carousel: Álbumes Destacados & Tidal Master
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "Álbumes en Tendencia • TIDAL Master",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(featuredAlbums) { album ->
                        Column(
                            modifier = Modifier
                                .width(144.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                                .clickable {
                                    onAlbumClick(album)
                                }
                                .padding(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(124.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceCard)
                            ) {
                                AsyncImage(
                                    model = album.coverUrl,
                                    contentDescription = album.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = album.title,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "${album.artist} • ${album.year}",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Jango Live & Trending Radio Stations (24/7 Unlimited Streaming)
        item {
            Column(modifier = Modifier.padding(top = 22.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📻 Jango Live • Radios en Vivo",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x26EF4444))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("EN VIVO", color = Color(0xFFEF4444), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "Sin Anuncios",
                        color = CyanLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(radioStations) { station ->
                        Column(
                            modifier = Modifier
                                .width(150.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .border(1.dp, BorderSubtleCyan, RoundedCornerShape(16.dp))
                                .clickable { onRadioClick(station) }
                                .padding(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceCard)
                            ) {
                                AsyncImage(
                                    model = station.coverUrl,
                                    contentDescription = station.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xCC000000))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("🔴 LIVE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(CyanPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Radio",
                                        tint = Color(0xFF08080C),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = station.name,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = station.genre,
                                color = CyanLight,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = station.listenersCount,
                                color = TextMuted,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Genre Pills Carousel
        item {
            Column(modifier = Modifier.padding(top = 22.dp)) {
                Text(
                    text = "Explorar por Géneros",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(genres) { genre ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(genre.gradientStartHex), Color(genre.gradientEndHex))
                                    )
                                )
                                .clickable { onGenreSelect(genre.name) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = genre.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section: "Pistas Recomendadas para Ti"
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pistas para Ti (Descargables)",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${tracks.size} canciones",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Track items list
        items(tracks) { track ->
            val isCurrent = playbackState.currentTrack?.id == track.id
            val isPlaying = isCurrent && playbackState.isPlaying
            val progress = downloadStatus[track.id]

            TrackItemRow(
                track = track,
                isCurrentTrack = isCurrent,
                isPlaying = isPlaying,
                downloadProgress = progress,
                onTrackClick = { onTrackClick(track, tracks) },
                onFavoriteToggle = { onFavoriteToggle(track) },
                onDownloadClick = { onDownloadClick(track) },
                onRemoveDownloadClick = { onRemoveDownloadClick(track) },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

