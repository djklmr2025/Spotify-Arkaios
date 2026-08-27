package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.outlined.Radio
import com.example.ui.MainViewModel
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.AlbumDetailModal
import com.example.ui.components.ArkaiosAuthModal
import com.example.ui.components.ArkaiosPayModal
import com.example.ui.components.BackgroundDownloaderSheet
import com.example.ui.components.CreatorStudioModal
import com.example.ui.components.FullPlayerSheet
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.TidalServerModal
import com.example.ui.screens.AmrStoreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.RadioScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtleCyan
import com.example.ui.theme.CyanLight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

import com.example.ui.components.UpdateAlertModal

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val wallet by viewModel.walletState.collectAsState()
    val catalogTracks by viewModel.catalogTracks.collectAsState()
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val favoriteTracks by viewModel.favoriteTracks.collectAsState()
    val localDeviceTracks by viewModel.localDeviceTracks.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGenre by viewModel.selectedGenreFilter.collectAsState()
    val downloadStatus by viewModel.downloadStatus.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val checkoutModal by viewModel.checkoutModal.collectAsState()
    val isFullPlayerExpanded by viewModel.isFullPlayerExpanded.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val isDownloaderSheetOpen by viewModel.isDownloaderSheetOpen.collectAsState()
    val backgroundTasks by viewModel.backgroundTasks.collectAsState()
    val engineStats by viewModel.engineStats.collectAsState()
    val tidalConfig by viewModel.tidalConfig.collectAsState()
    val tidalConnectionLog by viewModel.tidalConnectionLog.collectAsState()
    val tidalSearchResults by viewModel.tidalSearchResults.collectAsState()
    val isTidalSearching by viewModel.isTidalSearching.collectAsState()
    val isTidalModalOpen by viewModel.isTidalModalOpen.collectAsState()
    val selectedSourceFilter by viewModel.selectedSourceFilter.collectAsState()

    // Playlists & Auth & Modals
    val playlists by viewModel.playlists.collectAsState()
    val playlistTracksMap by viewModel.playlistTracksMap.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isAuthModalOpen by viewModel.isAuthModalOpen.collectAsState()
    val selectedAlbumForModal by viewModel.selectedAlbumForModal.collectAsState()
    val trackForPlaylistDialog by viewModel.trackForPlaylistDialog.collectAsState()

    // Creator Studio 50GB & Nube 5TB & CloudStream Video
    val creatorStats by viewModel.creatorStats.collectAsState()
    val creatorTracks by viewModel.creatorTracks.collectAsState()
    val isCreatorStudioOpen by viewModel.isCreatorStudioOpen.collectAsState()
    val radioStations by viewModel.radioStations.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val appUpdateInfo by viewModel.appUpdateInfo.collectAsState()

    // Community Voting & Live Listening Status
    val isCommunityVotingOpen by viewModel.isCommunityVotingOpen.collectAsState()
    val votedTracks by viewModel.votedTracks.collectAsState()
    val userListeningStatuses by viewModel.userListeningStatuses.collectAsState()

    // VIP Creators Directory ($500 MXN Annual Members) & Karaoke Studio
    val isVipCreatorsModalOpen by viewModel.isVipCreatorsModalOpen.collectAsState()
    val vipCreators by viewModel.vipCreators.collectAsState()
    val isKaraokeModalOpen by viewModel.isKaraokeModalOpen.collectAsState()
    val karaokeTracks by viewModel.karaokeTracks.collectAsState()

    appUpdateInfo?.let { info ->
        if (info.isUpdateAvailable) {
            UpdateAlertModal(
                updateInfo = info,
                onDismiss = { viewModel.dismissUpdateDialog() }
            )
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = BackgroundDark,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = if (playbackState.currentTrack != null) 140.dp else 80.dp)
                ) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = SurfaceCard,
                        contentColor = TextPrimary,
                        actionColor = CyanLight
                    )
                }
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundDark)
                ) {
                    // Mini Player Bar if a track is active and full player is not expanded
                    if (playbackState.currentTrack != null && !isFullPlayerExpanded) {
                        MiniPlayerBar(
                            playbackState = playbackState,
                            onBarClick = { viewModel.setFullPlayerExpanded(true) },
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onNextTrack = { viewModel.nextTrack() },
                            onFavoriteToggle = { playbackState.currentTrack?.let { viewModel.toggleFavorite(it) } }
                        )
                    }

                    // Navigation Bar (3 clean tabs: Inicio, Buscar, Biblioteca/Descargas)
                    NavigationBar(
                        containerColor = SurfaceDark,
                        contentColor = TextPrimary,
                        tonalElevation = 6.dp,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { viewModel.setTab(0) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "Inicio"
                                )
                            },
                            label = { Text("Inicio", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanLight,
                                selectedTextColor = CyanLight,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = Color(0x2606B6D4)
                            ),
                            modifier = Modifier.testTag("nav_tab_home")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { viewModel.setTab(1) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 1) Icons.Filled.Search else Icons.Outlined.Search,
                                    contentDescription = "Buscar"
                                )
                            },
                            label = { Text("Buscar (YouTube)", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanLight,
                                selectedTextColor = CyanLight,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = Color(0x2606B6D4)
                            ),
                            modifier = Modifier.testTag("nav_tab_search")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { viewModel.setTab(2) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 2) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic,
                                    contentDescription = "Biblioteca"
                                )
                            },
                            label = { Text("Biblioteca / Descargas", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanLight,
                                selectedTextColor = CyanLight,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = Color(0x2606B6D4)
                            ),
                            modifier = Modifier.testTag("nav_tab_library")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .statusBarsPadding()
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        tracks = catalogTracks,
                        featuredAlbums = viewModel.featuredAlbums,
                        genres = viewModel.genres,
                        wallet = wallet,
                        playbackState = playbackState,
                        downloadStatus = downloadStatus,
                        radioStations = radioStations,
                        onRadioClick = { viewModel.playRadioStation(it) },
                        onTrackClick = { tr, queue -> viewModel.playTrack(tr, queue) },
                        onAlbumClick = { viewModel.openAlbumModal(it) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        onDownloadClick = { viewModel.downloadTrack(it) },
                        onRemoveDownloadClick = { viewModel.removeDownload(it) },
                        onNavigateToAmrStore = { viewModel.setTab(4) },
                        onGenreSelect = { genre ->
                            viewModel.setGenreFilter(genre)
                            viewModel.setTab(1)
                        },
                        onOpenTidalModal = { viewModel.setTidalModalOpen(true) },
                        onOpenCreatorStudio = { viewModel.setCreatorStudioOpen(true) },
                        onOpenCommunityVoting = { viewModel.setCommunityVotingOpen(true) },
                        onOpenVipCreators = { viewModel.setVipCreatorsModalOpen(true) },
                        onOpenKaraokeStudio = { viewModel.setKaraokeModalOpen(true) },
                        onOpenAuthModal = { viewModel.openAuthModal() }
                    )
                    1 -> SearchScreen(
                        searchQuery = searchQuery,
                        selectedGenre = selectedGenre,
                        searchResults = searchResults,
                        genres = viewModel.genres,
                        playbackState = playbackState,
                        downloadStatus = downloadStatus,
                        isSearching = isTidalSearching,
                        selectedSource = selectedSourceFilter,
                        onSourceSelect = { viewModel.setSourceFilter(it) },
                        onQueryChange = { viewModel.setSearchQuery(it) },
                        onGenreSelect = { viewModel.setGenreFilter(it) },
                        onTrackClick = { tr, queue -> viewModel.playTrack(tr, queue) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        onDownloadClick = { viewModel.downloadTrack(it) },
                        onRemoveDownloadClick = { viewModel.removeDownload(it) },
                        onOpenDownloaderSheet = { viewModel.setDownloaderSheetOpen(true) },
                        onOpenTidalModal = { viewModel.setTidalModalOpen(true) }
                    )
                    2 -> LibraryScreen(
                        playlists = playlists,
                        playlistTracksMap = playlistTracksMap,
                        downloadedTracks = downloadedTracks,
                        favoriteTracks = favoriteTracks,
                        localDeviceTracks = localDeviceTracks,
                        radioStations = radioStations,
                        playbackState = playbackState,
                        downloadStatus = downloadStatus,
                        onTrackClick = { tr, queue -> viewModel.playTrack(tr, queue) },
                        onRadioClick = { viewModel.playRadioStation(it) },
                        onPlayPlaylist = { tracks, shuffle -> viewModel.playPlaylist(tracks, shuffle) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        onDownloadClick = { viewModel.downloadTrack(it) },
                        onRemoveDownloadClick = { viewModel.removeDownload(it) },
                        onScanDeviceAudio = { viewModel.scanLocalMusic() },
                        onImportAudioUris = { viewModel.importAudioUris(it) },
                        onImportM3uUri = { viewModel.importM3uUri(it) },
                        onImportM3uUrl = { url, name -> viewModel.importM3uFromUrl(url, name) },
                        onAddCustomRadio = { name, genre, url -> viewModel.addCustomRadioStation(name, genre, url) },
                        onCreatePlaylist = { name, desc -> viewModel.createPlaylist(name, desc) },
                        onDeletePlaylist = { id -> viewModel.deletePlaylist(id) },
                        onOpenDownloaderSheet = { viewModel.setDownloaderSheetOpen(true) },
                        onOpenAuthModal = { viewModel.openAuthModal() },
                        onOpenCreatorStudio = { viewModel.setCreatorStudioOpen(true) }
                    )
                    3 -> RadioScreen(
                        radioStations = radioStations,
                        playbackState = playbackState,
                        onRadioClick = { viewModel.playRadioStation(it) },
                        onAddRadioClick = { name, genre, url -> viewModel.addCustomRadioStation(name, genre, url) }
                    )
                    4 -> AmrStoreScreen(
                        wallet = wallet,
                        premiumTiers = viewModel.premiumTiers,
                        transactions = transactions,
                        onOpenCheckout = { viewModel.openArkaiosPayCheckout(it) },
                        onTransferTokens = { addr, amt -> viewModel.transferAmrTokens(addr, amt) },
                        onBuyTokensPayPal = { usd, tokens -> viewModel.buyTokensWithPayPal(usd, tokens) },
                        onRedeemCode = { code -> viewModel.redeemAmrCode(code) }
                    )
                }

                // Floating Active Download Indicator Pill
                if (engineStats.activeDownloadsCount > 0 && !isDownloaderSheetOpen) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp, end = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDark)
                            .border(1.dp, BorderSubtleCyan, RoundedCornerShape(20.dp))
                            .clickable { viewModel.setDownloaderSheetOpen(true) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("floating_download_status_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = CyanLight,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Descargando (${engineStats.activeDownloadsCount})",
                                color = CyanLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (engineStats.totalSpeedKbps > 0) {
                                Text(
                                    text = "• %.1f MB/s".format(engineStats.totalSpeedKbps / 1024.0),
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Full Player Modal with slide in / out animation
        AnimatedVisibility(
            visible = isFullPlayerExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            FullPlayerSheet(
                playbackState = playbackState,
                downloadProgress = playbackState.currentTrack?.let { downloadStatus[it.id] },
                onDismiss = { viewModel.setFullPlayerExpanded(false) },
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onNextTrack = { viewModel.nextTrack() },
                onPreviousTrack = { viewModel.previousTrack() },
                onSeekTo = { viewModel.seekTo(it) },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onCycleRepeat = { viewModel.cycleRepeatMode() },
                onFavoriteToggle = { playbackState.currentTrack?.let { viewModel.toggleFavorite(it) } },
                onDownloadClick = { playbackState.currentTrack?.let { viewModel.downloadTrack(it) } },
                onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                onEqualizerPresetSelect = { viewModel.setEqualizerPreset(it) },
                onTrackFromQueueClick = { track -> viewModel.playTrack(track) },
                onAddToPlaylistClick = { track -> viewModel.openAddToPlaylistDialog(track) }
            )
        }

        // Background Downloader Sheet
        AnimatedVisibility(
            visible = isDownloaderSheetOpen,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            BackgroundDownloaderSheet(
                tasks = backgroundTasks,
                engineStats = engineStats,
                onEnqueueUrl = { url, format, engine ->
                    viewModel.enqueueCustomUrlDownload(url, format = format, engine = engine)
                },
                onCancelTask = { taskId -> viewModel.cancelBackgroundTask(taskId) },
                onRetryTask = { taskId -> viewModel.retryBackgroundTask(taskId) },
                onClearCompleted = { viewModel.clearCompletedBackgroundTasks() },
                onPlayTrack = { track ->
                    viewModel.playTrack(track)
                    viewModel.setDownloaderSheetOpen(false)
                },
                onClose = { viewModel.setDownloaderSheetOpen(false) }
            )
        }

        // Arkaios Pay Checkout Modal Overlay
        ArkaiosPayModal(
            modalState = checkoutModal,
            wallet = wallet,
            onConfirmPay = { viewModel.confirmArkaiosPayCheckout() },
            onClose = { viewModel.closeCheckoutModal() }
        )

        // TIDAL Hi-Fi API Direct & Cloud Servers Modal
        AnimatedVisibility(
            visible = isTidalModalOpen,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            TidalServerModal(
                config = tidalConfig,
                connectionLog = tidalConnectionLog,
                tidalSearchResults = tidalSearchResults,
                isSearching = isTidalSearching,
                onUpdateToken = { viewModel.updateTidalToken(it) },
                onUpdateQuality = { viewModel.updateTidalQuality(it) },
                onUpdateProvider = { viewModel.updateTidalProvider(it) },
                onSearchTidal = { viewModel.searchTidal(it) },
                onPlayTrack = { track ->
                    viewModel.playTidalTrack(track)
                    viewModel.setTidalModalOpen(false)
                },
                onDownloadTrack = { track ->
                    viewModel.downloadTrack(track)
                },
                onClose = { viewModel.setTidalModalOpen(false) }
            )
        }

        // Add To Playlist Dialog
        if (trackForPlaylistDialog != null) {
            AddToPlaylistDialog(
                track = trackForPlaylistDialog!!,
                playlists = playlists,
                onDismiss = { viewModel.closeAddToPlaylistDialog() },
                onAddToPlaylist = { playlistId, tr ->
                    viewModel.addTrackToPlaylist(playlistId, tr)
                    viewModel.closeAddToPlaylistDialog()
                },
                onCreateAndAdd = { name, tr ->
                    viewModel.createPlaylistAndAddTrack(name, tr)
                    viewModel.closeAddToPlaylistDialog()
                }
            )
        }

        // Album Detail Modal
        AnimatedVisibility(
            visible = selectedAlbumForModal != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            if (selectedAlbumForModal != null) {
                val album = selectedAlbumForModal!!
                val albumTracks = catalogTracks.filter { it.album.contains(album.title.take(6), ignoreCase = true) }
                    .ifEmpty { catalogTracks.take(4) }

                AlbumDetailModal(
                    album = album,
                    albumTracks = albumTracks,
                    playbackState = playbackState,
                    downloadStatus = downloadStatus,
                    onDismiss = { viewModel.closeAlbumModal() },
                    onPlayAll = { tracks, shuffle -> viewModel.playAlbum(tracks, shuffle) },
                    onTrackClick = { tr, list -> viewModel.playTrack(tr, list) },
                    onDownloadAlbum = { tracks -> viewModel.downloadAlbum(tracks) },
                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                    onAddToPlaylist = { tr -> viewModel.openAddToPlaylistDialog(tr) }
                )
            }
        }

        // Arkaios & Google Auth Modal
        AnimatedVisibility(
            visible = isAuthModalOpen,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            ArkaiosAuthModal(
                userProfile = userProfile,
                onDismiss = { viewModel.closeAuthModal() },
                onLoginGoogle = { viewModel.loginWithGoogle() },
                onLoginEmail = { email -> viewModel.loginWithEmail(email) },
                onToggleOfflineCache = { enabled -> viewModel.toggleOfflineCache(enabled) },
                onUpgradeTier = { tier -> viewModel.updateUserTier(tier) }
            )
        }

        // Arkaios Creator Studio 50GB & Google Drive 5TB Modal
        AnimatedVisibility(
            visible = isCreatorStudioOpen,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            CreatorStudioModal(
                stats = creatorStats,
                creatorTracks = creatorTracks,
                onUploadNewTrack = { t, a, alb, g, mb ->
                    viewModel.uploadCreatorTrack(t, a, alb, g, mb)
                },
                onPlayTrack = { tr ->
                    viewModel.playTrack(tr)
                    viewModel.setCreatorStudioOpen(false)
                },
                onShareTrack = { ct ->
                    viewModel.showSnackbar("Enlace copiado: https://arkaios.music/track/${ct.id}")
                },
                onClaimEarnings = {
                    viewModel.claimCreatorEarnings()
                },
                onDismiss = { viewModel.setCreatorStudioOpen(false) }
            )
        }

        // Arkaios Pay & PayPal SDK v6 Checkout Modal
        if (checkoutModal.isOpen) {
            ArkaiosPayModal(
                modalState = checkoutModal,
                wallet = wallet,
                onConfirmPay = { viewModel.confirmArkaiosPayCheckout() },
                onConfirmPayPal = { viewModel.confirmPayPalCheckout() },
                onClose = { viewModel.closeCheckoutModal() }
            )
        }

        // Community Voting & Live Listening Status Modal
        com.example.ui.components.CommunityVotingModal(
            isOpen = isCommunityVotingOpen,
            onClose = { viewModel.setCommunityVotingOpen(false) },
            votedTracks = votedTracks,
            userListeningStatuses = userListeningStatuses,
            onVoteTrack = { id -> viewModel.voteTrack(id) },
            onPlayTrack = { tr ->
                viewModel.playTrack(tr)
                viewModel.setCommunityVotingOpen(false)
            },
            onProposeNewSong = { title, artist ->
                viewModel.proposeSongForVoting(title, artist)
            }
        )

        // VIP Creators Directory Modal (Karaoplay Style - $500 MXN Annual Members)
        if (isVipCreatorsModalOpen) {
            com.example.ui.components.VipCreatorsDirectoryModal(
                creators = vipCreators,
                isCurrentUserVip = wallet.isGodOwnerLicensed,
                onToggleFollow = { id -> viewModel.toggleFollowVipCreator(id) },
                onVoteCreatorTrack = { title ->
                    viewModel.proposeSongForVoting(title, "Creador VIP")
                },
                onOpenStoreForVip = {
                    viewModel.setTab(4)
                },
                onDismiss = { viewModel.setVipCreatorsModalOpen(false) }
            )
        }

        // Karaoke Studio & Karaoplay Engine Modal
        if (isKaraokeModalOpen) {
            com.example.ui.components.KaraokeStudioModal(
                karaokeTracks = karaokeTracks,
                onSearchKaraoke = { q -> viewModel.searchKaraokeTracks(q) },
                onPlayKaraoke = { tr ->
                    viewModel.playTrack(tr)
                    viewModel.setKaraokeModalOpen(false)
                },
                onDownloadKaraoke = { kt ->
                    viewModel.enqueueCustomUrlDownload(kt.sourceUrl, format = "MP3 (Karaoke HD)")
                },
                onForceLinkDownload = { url ->
                    viewModel.enqueueCustomUrlDownload(url, format = "MP3 (Multi-Downloader Engine)")
                },
                onDismiss = { viewModel.setKaraokeModalOpen(false) }
            )
        }
    }
}
