package com.example.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
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
    radioStations: List<com.example.data.model.RadioStation> = emptyList(),
    playbackState: PlaybackState,
    downloadStatus: Map<String, DownloadProgress>,
    onTrackClick: (Track, List<Track>?) -> Unit = { _, _ -> },
    onRadioClick: (com.example.data.model.RadioStation) -> Unit = {},
    onPlayPlaylist: (List<Track>, Boolean) -> Unit,
    onFavoriteToggle: (Track) -> Unit,
    onDownloadClick: (Track) -> Unit,
    onRemoveDownloadClick: (Track) -> Unit,
    onScanDeviceAudio: () -> Unit,
    onImportAudioUris: (List<Uri>) -> Unit = {},
    onImportM3uUri: (Uri) -> Unit = {},
    onImportM3uUrl: (String, String) -> Unit = { _, _ -> },
    onAddCustomRadio: (String, String, String) -> Unit = { _, _, _ -> },
    onCreatePlaylist: (String, String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onOpenDownloaderSheet: () -> Unit = {},
    onOpenCreatorStudio: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showImportM3uUrlDialog by remember { mutableStateOf(false) }
    var showAddRadioDialog by remember { mutableStateOf(false) }
    var selectedPlaylistForDetail by remember { mutableStateOf<PlaylistEntity?>(null) }
    var playlistTitle by remember { mutableStateOf("") }
    var playlistDesc by remember { mutableStateOf("") }
    var m3uUrlInput by remember { mutableStateOf("") }
    var m3uNameInput by remember { mutableStateOf("") }
    var radioNameInput by remember { mutableStateOf("") }
    var radioGenreInput by remember { mutableStateOf("") }
    var radioUrlInput by remember { mutableStateOf("") }
    var radioSearchQuery by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        onScanDeviceAudio()
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportAudioUris(uris)
        }
    }

    val m3uFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportM3uUri(uri)
        }
    }

    fun requestPermissionsAndScan() {
        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        permissionLauncher.launch(permissionsToRequest)
    }

    fun sharePlaylistAsM3u(pl: PlaylistEntity, tracks: List<Track>) {
        val m3uText = com.example.data.m3u.M3uParser.exportToM3u(pl.title, tracks)
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Lista de Música M3U - ${pl.title}")
            putExtra(android.content.Intent.EXTRA_TEXT, m3uText)
        }
        context.startActivity(android.content.Intent.createChooser(shareIntent, "Exportar / Compartir Lista .M3U"))
    }

    val tabs = listOf("Playlists (${playlists.size})", "Radios & Jango Live (${radioStations.size})", "Descargas Offline / Vault", "Favoritos", "Archivos Locales")

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

                // Play / Shuffle Playlist Buttons & Export M3U
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onPlayPlaylist(tracksInPl, false) },
                        modifier = Modifier.weight(1.2f).height(42.dp),
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

                    Button(
                        onClick = { sharePlaylistAsM3u(pl, tracksInPl) },
                        modifier = Modifier.weight(1f).height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x2638BDF8), contentColor = CyanLight),
                        shape = RoundedCornerShape(20.dp),
                        enabled = tracksInPl.isNotEmpty()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = CyanLight, modifier = Modifier.size(15.dp))
                            Text(".M3U", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                                onTrackClick = { onTrackClick(track, tracksInPl) },
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
                        // Quick Action Buttons: Create, Import .M3U file, Import M3U URL
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x1F22D3EE))
                                        .border(1.dp, BorderSubtleCyan, RoundedCornerShape(12.dp))
                                        .clickable { showCreatePlaylistDialog = true }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = CyanLight, modifier = Modifier.size(18.dp))
                                        Text("Nueva Playlist", color = CyanLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceElevated)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                        .clickable { m3uFilePickerLauncher.launch(arrayOf("*/*")) }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Folder, contentDescription = null, tint = ArkaiosGoldLight, modifier = Modifier.size(16.dp))
                                        Text("Abrir .M3U", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceElevated)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                        .clickable { showImportM3uUrlDialog = true }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Share, contentDescription = null, tint = CyanLight, modifier = Modifier.size(16.dp))
                                        Text("URL .M3U", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                        Text("Toca 'Nueva Playlist' o 'Abrir .M3U' para importar listas de música.", color = TextMuted, fontSize = 12.sp)
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
                    // Radios & Jango Live Subtab
                    val filteredStations = remember(radioSearchQuery, radioStations) {
                        if (radioSearchQuery.isBlank()) radioStations
                        else radioStations.filter {
                            it.name.contains(radioSearchQuery, ignoreCase = true) ||
                            it.genre.contains(radioSearchQuery, ignoreCase = true)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp, start = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header Banner
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            listOf(Color(0xFF0F2A38), Color(0xFF131726))
                                        )
                                    )
                                    .border(1.dp, BorderSubtleCyan, RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("📻 Jango Live Radio", color = CyanLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0x26EF4444))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("EN VIVO", color = Color(0xFFEF4444), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text("Streaming 24/7", color = EmeraldLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Text(
                                        text = "Emisoras de música continuas sin cortes ni anuncios al estilo Jango / TuneIn. Compatible con transmisiones M3U, AAC, MP3 e Icecast.",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )

                                    Button(
                                        onClick = { showAddRadioDialog = true },
                                        modifier = Modifier.fillMaxWidth().height(38.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF08080C)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Text("➕ Añadir Estación de Radio / Stream Web", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Search Field for Radio
                        item {
                            OutlinedTextField(
                                value = radioSearchQuery,
                                onValueChange = { radioSearchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Buscar estación o género (Jazz, Rock, Reggaeton...)", fontSize = 12.sp, color = TextMuted) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanLight,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedContainerColor = SurfaceDark,
                                    unfocusedContainerColor = SurfaceDark
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        items(filteredStations) { station ->
                            val isCurrentStation = playbackState.currentTrack?.id == "radio_${station.id}"
                            val isPlaying = isCurrentStation && playbackState.isPlaying

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCurrentStation) Color(0x1F22D3EE) else SurfaceElevated)
                                    .border(1.dp, if (isCurrentStation) BorderSubtleCyan else BorderSubtle, RoundedCornerShape(12.dp))
                                    .clickable { onRadioClick(station) }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SurfaceDark)
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
                                                .padding(3.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xCC000000))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("LIVE", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Column {
                                        Text(station.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(station.genre, color = CyanLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Text("${station.listenersCount} • ${station.bitrate}", color = TextMuted, fontSize = 10.sp)
                                    }
                                }

                                IconButton(
                                    onClick = { onRadioClick(station) },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(if (isPlaying) Color(0xFFEF4444) else CyanPrimary)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.MusicNote else Icons.Default.PlayArrow,
                                        contentDescription = "Sintonizar",
                                        tint = Color(0xFF08080C),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
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
                                            text = "${downloadedTracks.size} canciones guardadas (.mp3 / .m4a / .flac)",
                                            color = EmeraldLight,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Archivos originales de audio sin cifrar • Almacenamiento: ${"%.1f".format(totalDownloadSize)} MB",
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
                                    onTrackClick = { onTrackClick(track, downloadedTracks) },
                                    onFavoriteToggle = { onFavoriteToggle(track) },
                                    onDownloadClick = { onDownloadClick(track) },
                                    onRemoveDownloadClick = { onRemoveDownloadClick(track) }
                                )
                            }
                        }
                    }
                }
                3 -> {
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
                                    onTrackClick = { onTrackClick(track, favoriteTracks) },
                                    onFavoriteToggle = { onFavoriteToggle(track) },
                                    onDownloadClick = { onDownloadClick(track) },
                                    onRemoveDownloadClick = { onRemoveDownloadClick(track) }
                                )
                            }
                        }
                    }
                }
                4 -> {
                    // Local Device Audio Files (.mp3 / .m4a / .flac / .wav)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp, start = 8.dp, end = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Scan & File Manager Action Card
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceDark)
                                    .border(1.dp, BorderSubtleCyan, RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FolderOpen,
                                                contentDescription = null,
                                                tint = CyanLight,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "Música en Almacenamiento Local",
                                                    color = TextPrimary,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${localDeviceTracks.size} canciones encontradas (.mp3, .m4a, .flac)",
                                                    color = CyanLight,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0x2610B981))
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text("Autorizado", color = EmeraldLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Action Buttons Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { requestPermissionsAndScan() },
                                            modifier = Modifier.weight(1f).height(42.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF08080C)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Text("Escanear Teléfono", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Button(
                                            onClick = { filePickerLauncher.launch(arrayOf("audio/*", "application/ogg", "*/*")) },
                                            modifier = Modifier.weight(1f).height(42.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = TextPrimary),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = CyanLight, modifier = Modifier.size(16.dp))
                                                Text("Elegir Archivos", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (localDeviceTracks.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("📁", fontSize = 40.sp)
                                        Text(
                                            text = "No se encontraron audios locales",
                                            color = TextPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Toca \"Escanear Teléfono\" para otorgar permisos automáticos o \"Elegir Archivos\" para seleccionar directamente tus canciones desde la carpeta Music.",
                                            color = TextMuted,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 20.dp),
                                            lineHeight = 16.sp
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Button(
                                            onClick = { filePickerLauncher.launch(arrayOf("audio/*", "application/ogg", "*/*")) },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF08080C)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Seleccionar Canciones de la Memoria", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
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
                                    onTrackClick = { onTrackClick(track, localDeviceTracks) },
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

    // Import M3U URL Dialog
    if (showImportM3uUrlDialog) {
        AlertDialog(
            onDismissRequest = { showImportM3uUrlDialog = false },
            title = { Text("Importar Playlist .M3U / .M3U8 Web", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Pega la URL de una lista de reproducción remota .m3u o .m3u8 para importarla automáticamente a tu biblioteca.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = m3uNameInput,
                        onValueChange = { m3uNameInput = it },
                        label = { Text("Nombre de la Lista (opcional)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanLight,
                            focusedLabelColor = CyanLight
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = m3uUrlInput,
                        onValueChange = { m3uUrlInput = it },
                        label = { Text("Enlace URL https://.../playlist.m3u") },
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
                        if (m3uUrlInput.isNotBlank()) {
                            onImportM3uUrl(m3uUrlInput.trim(), m3uNameInput.trim())
                            showImportM3uUrlDialog = false
                            m3uUrlInput = ""
                            m3uNameInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF08080C))
                ) {
                    Text("Importar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportM3uUrlDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Add Custom Radio Station Dialog
    if (showAddRadioDialog) {
        AlertDialog(
            onDismissRequest = { showAddRadioDialog = false },
            title = { Text("Añadir Estación de Radio Web", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Ingresa el enlace de transmisión en vivo (Icecast, Shoutcast, AAC, MP3 stream o archivo M3U).",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = radioNameInput,
                        onValueChange = { radioNameInput = it },
                        label = { Text("Nombre de la Emisora") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanLight,
                            focusedLabelColor = CyanLight
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = radioGenreInput,
                        onValueChange = { radioGenreInput = it },
                        label = { Text("Género / Estilo (e.g. Rock, Pop, Chillout)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanLight,
                            focusedLabelColor = CyanLight
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = radioUrlInput,
                        onValueChange = { radioUrlInput = it },
                        label = { Text("URL de Transmisión (http://...:8000/stream)") },
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
                        if (radioNameInput.isNotBlank() && radioUrlInput.isNotBlank()) {
                            onAddCustomRadio(radioNameInput.trim(), radioGenreInput.trim(), radioUrlInput.trim())
                            showAddRadioDialog = false
                            radioNameInput = ""
                            radioGenreInput = ""
                            radioUrlInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF08080C))
                ) {
                    Text("Guardar y Sintonizar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRadioDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}
