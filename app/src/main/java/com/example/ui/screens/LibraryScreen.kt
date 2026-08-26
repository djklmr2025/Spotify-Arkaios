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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.PlaylistEntity
import com.example.data.model.Track
import com.example.data.repository.DownloadProgress
import com.example.player.PlaybackState
import com.example.ui.components.TrackItemRow
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
fun LibraryScreen(
    playlists: List<PlaylistEntity>,
    playlistTracksMap: Map<String, List<Track>>,
    downloadedTracks: List<Track>,
    favoriteTracks: List<Track>,
    localDeviceTracks: List<Track>,
    playbackState: PlaybackState,
    downloadStatus: Map<String, DownloadProgress>,
    onTrackClick: (Track) -> Unit,
    onPlayPlaylist: (List<Track>, Boolean) -> Unit,
    onFavoriteToggle: (Track) -> Unit,
    onDownloadClick: (Track) -> Unit,
    onRemoveDownloadClick: (Track) -> Unit,
    onScanDeviceAudio: () -> Unit,
    onCreatePlaylist: (String, String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onOpenDownloaderSheet: () -> Unit = {},
    onOpenAuthModal: () -> Unit = {},
    onOpenCreatorStudio: () -> Unit = {}
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var selectedPlaylistForDetail by remember { mutableStateOf<PlaylistEntity?>(null) }
    var playlistTitle by remember { mutableStateOf("") }
    var playlistDesc by remember { mutableStateOf("") }

    val tabs = listOf("Playlists (${playlists.size})", "Descargas Offline / Vault", "Favoritos", "Archivos Locales")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("library_screen")
    ) {
        // Library Header with Create Playlist Action & Motor Downloader & Auth Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tu Biblioteca",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                // Creator Studio 50GB Google Drive Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x2638BDF8))
                        .border(1.dp, Color(0x6638BDF8), RoundedCornerShape(20.dp))
                        .clickable { onOpenCreatorStudio() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("☁️", fontSize = 11.sp)
                        Text(
                            text = "50GB Drive",
                            color = CyanLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Auth Account Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x26F59E0B))
                        .border(1.dp, Color(0x66F59E0B), RoundedCornerShape(20.dp))
                        .clickable { onOpenAuthModal() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = "Cuenta Arkaios", tint = ArkaiosGoldLight, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Cuenta",
                            color = ArkaiosGoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceDark)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .clickable { showCreatePlaylistDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Playlist",
                            tint = CyanLight,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Playlist",
                            color = CyanLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Playlist Detail View (if user opened a specific playlist)
        if (selectedPlaylistForDetail != null) {
            val pl = selectedPlaylistForDetail!!
            val tracksInPl = playlistTracksMap[pl.id] ?: emptyList()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable { selectedPlaylistForDetail = null }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = CyanLight)
                        Text("Volver a Playlists", color = CyanLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = {
                        onDeletePlaylist(pl.id)
                        selectedPlaylistForDetail = null
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444))
                    }
                }

                // Playlist Hero
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark)
                        .border(1.dp, BorderSubtleCyan, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AsyncImage(
                        model = pl.coverUrl.ifEmpty { if (tracksInPl.isNotEmpty()) tracksInPl.first().coverUrl else "" },
                        contentDescription = pl.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceElevated)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(pl.title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(pl.description.ifEmpty { "Creada por ${pl.author}" }, color = TextSecondary, fontSize = 12.sp)
                        Text("${tracksInPl.size} canciones en sesión", color = CyanLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Play / Shuffle Playlist Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onPlayPlaylist(tracksInPl, false) },
                        modifier = Modifier.weight(1f).height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF08080C)),
                        shape = RoundedCornerShape(20.dp),
                        enabled = tracksInPl.isNotEmpty()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Reproducir", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = { onPlayPlaylist(tracksInPl, true) },
                        modifier = Modifier.weight(1f).height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = TextPrimary),
                        shape = RoundedCornerShape(20.dp),
                        enabled = tracksInPl.isNotEmpty()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Shuffle, contentDescription = null, tint = CyanLight, modifier = Modifier.size(16.dp))
                            Text("Aleatorio", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (tracksInPl.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Esta playlist no tiene canciones aún.\nUsa el botón de menú o '+' en cualquier pista para añadirla.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp, top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(tracksInPl) { track ->
                            val isCurrent = playbackState.currentTrack?.id == track.id
                            val isPlaying = isCurrent && playbackState.isPlaying
                            val progress = downloadStatus[track.id]

                            TrackItemRow(
                                track = track,
                                isCurrentTrack = isCurrent,
                                isPlaying = isPlaying,
                                downloadProgress = progress,
                                onTrackClick = { onTrackClick(track) },
                                onFavoriteToggle = { onFavoriteToggle(track) },
                                onDownloadClick = { onDownloadClick(track) },
                                onRemoveDownloadClick = { onRemoveDownloadClick(track) }
                            )
                        }
                    }
                }
            }
        } else {
            // Sub-tabs Row
            ScrollableTabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = BackgroundDark,
                contentColor = CyanLight,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                        color = CyanLight
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSubTab == index,
                        onClick = { selectedSubTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedSubTab == index) CyanLight else TextSecondary,
                                fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedSubTab) {
                0 -> {
                    // Playlists Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp, top = 10.dp, start = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Create Playlist Banner
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x1F22D3EE))
                                    .border(1.dp, BorderSubtleCyan, RoundedCornerShape(12.dp))
                                    .clickable { showCreatePlaylistDialog = true }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = CyanLight, modifier = Modifier.size(22.dp))
                                    Column {
                                        Text("Crear Nueva Playlist", color = CyanLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("Organiza canciones para reproducir en sesión continua", color = TextSecondary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        if (playlists.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🎵", fontSize = 36.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Aún no has creado playlists", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text("Toca el botón arriba para crear tu primera lista personalizada.", color = TextMuted, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            items(playlists) { pl ->
                                val tracksInPl = playlistTracksMap[pl.id] ?: emptyList()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceElevated)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                        .clickable { selectedPlaylistForDetail = pl }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        AsyncImage(
                                            model = pl.coverUrl.ifEmpty { if (tracksInPl.isNotEmpty()) tracksInPl.first().coverUrl else "" },
                                            contentDescription = pl.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SurfaceDark)
                                        )

                                        Column {
                                            Text(pl.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text("${tracksInPl.size} canciones • ${pl.author}", color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { onPlayPlaylist(tracksInPl, false) },
                                            modifier = Modifier.size(36.dp),
                                            enabled = tracksInPl.isNotEmpty()
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = CyanLight)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Downloaded Tracks Offline
                    val totalDownloadSize = downloadedTracks.sumOf { it.downloadSizeMb }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp, start = 8.dp, end = 8.dp)
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x1F10B981))
                                    .border(1.dp, Color(0x3310B981), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "${downloadedTracks.size} pistas en Caché Offline Precifrado (.arkcache)",
                                            color = EmeraldLight,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Protección Vault DRM Arkaios • Almacenamiento: ${"%.1f".format(totalDownloadSize)} MB",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        if (downloadedTracks.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("💾", fontSize = 36.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No tienes canciones en el Caché Offline",
                                            color = TextPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Toca el ícono de descarga en cualquier pista o álbum para guardar en formato cifrado .arkcache.",
                                            color = TextMuted,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 24.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            items(downloadedTracks) { track ->
                                val isCurrent = playbackState.currentTrack?.id == track.id
                                val isPlaying = isCurrent && playbackState.isPlaying
                                val progress = downloadStatus[track.id]

                                TrackItemRow(
                                    track = track,
                                    isCurrentTrack = isCurrent,
                                    isPlaying = isPlaying,
                                    downloadProgress = progress,
                                    onTrackClick = { onTrackClick(track) },
                                    onFavoriteToggle = { onFavoriteToggle(track) },
                                    onDownloadClick = { onDownloadClick(track) },
                                    onRemoveDownloadClick = { onRemoveDownloadClick(track) }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // Favorites Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp, start = 8.dp, end = 8.dp)
                    ) {
                        if (favoriteTracks.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("❤️", fontSize = 36.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Aún no tienes favoritos",
                                            color = TextPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Toca el corazón en cualquier canción para guardarla aquí.",
                                            color = TextMuted,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            items(favoriteTracks) { track ->
                                val isCurrent = playbackState.currentTrack?.id == track.id
                                val isPlaying = isCurrent && playbackState.isPlaying
                                val progress = downloadStatus[track.id]

                                TrackItemRow(
                                    track = track,
                                    isCurrentTrack = isCurrent,
                                    isPlaying = isPlaying,
                                    downloadProgress = progress,
                                    onTrackClick = { onTrackClick(track) },
                                    onFavoriteToggle = { onFavoriteToggle(track) },
                                    onDownloadClick = { onDownloadClick(track) },
                                    onRemoveDownloadClick = { onRemoveDownloadClick(track) }
                                )
                            }
                        }
                    }
                }
                3 -> {
                    // Local Device Audio Files (.mp3 / .m4a)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp, start = 8.dp, end = 8.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SurfaceDark)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                                    .clickable { onScanDeviceAudio() }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = CyanLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Archivos de Música en el Dispositivo",
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${localDeviceTracks.size} archivos encontrados (.mp3, .m4a)",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Scan",
                                    tint = CyanLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (localDeviceTracks.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("📁", fontSize = 36.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No se encontraron audios locales",
                                            color = TextPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Toca el botón arriba para escanear archivos .mp3 o .m4a en tu teléfono.",
                                            color = TextMuted,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 24.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            items(localDeviceTracks) { track ->
                                val isCurrent = playbackState.currentTrack?.id == track.id
                                val isPlaying = isCurrent && playbackState.isPlaying

                                TrackItemRow(
                                    track = track,
                                    isCurrentTrack = isCurrent,
                                    isPlaying = isPlaying,
                                    downloadProgress = null,
                                    onTrackClick = { onTrackClick(track) },
                                    onFavoriteToggle = { onFavoriteToggle(track) },
                                    onDownloadClick = {},
                                    onRemoveDownloadClick = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("Crear Nueva Playlist", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = playlistTitle,
                        onValueChange = { playlistTitle = it },
                        label = { Text("Título de la Playlist") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanLight,
                            focusedLabelColor = CyanLight
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = playlistDesc,
                        onValueChange = { playlistDesc = it },
                        label = { Text("Descripción (opcional)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanLight,
                            focusedLabelColor = CyanLight
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistTitle.isNotBlank()) {
                            onCreatePlaylist(playlistTitle, playlistDesc)
                            showCreatePlaylistDialog = false
                            playlistTitle = ""
                            playlistDesc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF08080C))
                ) {
                    Text("Crear", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}
