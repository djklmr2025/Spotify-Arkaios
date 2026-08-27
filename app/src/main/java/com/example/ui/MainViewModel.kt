package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.downloader.BackgroundDownloadEngine
import com.example.data.downloader.BackgroundDownloadTask
import com.example.data.downloader.BackgroundEngineStats
import com.example.data.downloader.DownloadEngineSource
import com.example.data.local.AppDatabase
import com.example.data.model.Album
import com.example.data.model.AmrTransaction
import com.example.data.model.AmrWallet
import com.example.data.model.ArkaiosPremiumTier
import com.example.data.model.GenreCategory
import com.example.data.model.Playlist
import com.example.data.model.Track
import com.example.data.repository.AmrWalletRepository
import com.example.data.repository.DownloadProgress
import com.example.data.repository.DownloadRepository
import com.example.data.repository.MusicRepository
import com.example.data.tidal.AudioServerProvider
import com.example.data.tidal.TidalApiService
import com.example.data.tidal.TidalAudioQuality
import com.example.data.tidal.TidalConfig
import com.example.player.AudioPlayerEngine
import com.example.player.PlaybackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CheckoutModalState(
    val isOpen: Boolean = false,
    val tier: ArkaiosPremiumTier? = null,
    val customAmount: Double = 0.0,
    val customConcept: String = "",
    val isProcessing: Boolean = false,
    val isSuccess: Boolean = false,
    val lastTx: AmrTransaction? = null,
    val errorMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val musicRepository = MusicRepository(application, db)
    val downloadRepository = DownloadRepository(application, db)
    val backgroundDownloadEngine = BackgroundDownloadEngine(application, db)
    val amrWalletRepository = AmrWalletRepository(db)
    val playerEngine = AudioPlayerEngine(application)
    val tidalApiService = TidalApiService(application)
    val radioStationRepository = com.example.data.repository.RadioStationRepository()

    val radioStations: StateFlow<List<com.example.data.model.RadioStation>> = radioStationRepository.stations
    val playbackState: StateFlow<PlaybackState> = playerEngine.playbackState
    val walletState: StateFlow<AmrWallet> = amrWalletRepository.walletState
    val downloadStatus: StateFlow<Map<String, DownloadProgress>> = downloadRepository.downloadStatus
    val backgroundTasks: StateFlow<List<BackgroundDownloadTask>> = backgroundDownloadEngine.tasks
    val engineStats: StateFlow<BackgroundEngineStats> = backgroundDownloadEngine.engineStats
    val tidalConfig: StateFlow<TidalConfig> = tidalApiService.config
    val tidalConnectionLog: StateFlow<String> = tidalApiService.connectionLog

    private val _tidalSearchResults = MutableStateFlow<List<Track>>(emptyList())
    val tidalSearchResults: StateFlow<List<Track>> = _tidalSearchResults.asStateFlow()

    private val _isTidalSearching = MutableStateFlow(false)
    val isTidalSearching: StateFlow<Boolean> = _isTidalSearching.asStateFlow()

    private val _isTidalModalOpen = MutableStateFlow(false)
    val isTidalModalOpen: StateFlow<Boolean> = _isTidalModalOpen.asStateFlow()
    val transactions: StateFlow<List<AmrTransaction>> = amrWalletRepository.transactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _catalogTracks = MutableStateFlow<List<Track>>(emptyList())
    val catalogTracks: StateFlow<List<Track>> = _catalogTracks.asStateFlow()

    private val _localDeviceTracks = MutableStateFlow<List<Track>>(emptyList())
    val localDeviceTracks: StateFlow<List<Track>> = _localDeviceTracks.asStateFlow()

    val downloadedTracks: StateFlow<List<Track>> = musicRepository.downloadedTracksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTracks: StateFlow<List<Track>> = musicRepository.favoriteTracksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredAlbums: List<Album> = musicRepository.getFeaturedAlbums()
    val genres: List<GenreCategory> = musicRepository.getGenres()
    val premiumTiers: List<ArkaiosPremiumTier> = amrWalletRepository.getPremiumTiers()

    // Playlists & Vault DB flows
    val playlists: StateFlow<List<com.example.data.local.PlaylistEntity>> = db.playlistDao().getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _playlistTracksMap = MutableStateFlow<Map<String, List<Track>>>(emptyMap())
    val playlistTracksMap: StateFlow<Map<String, List<Track>>> = _playlistTracksMap.asStateFlow()

    // User Profile & Authentication State (Google + Arkaios Treasure)
    private val _userProfile = MutableStateFlow(com.example.data.model.UserProfile())
    val userProfile: StateFlow<com.example.data.model.UserProfile> = _userProfile.asStateFlow()

    private val _isAuthModalOpen = MutableStateFlow(false)
    val isAuthModalOpen: StateFlow<Boolean> = _isAuthModalOpen.asStateFlow()

    // Selected Album for Modal
    private val _selectedAlbumForModal = MutableStateFlow<Album?>(null)
    val selectedAlbumForModal: StateFlow<Album?> = _selectedAlbumForModal.asStateFlow()

    // Creator Studio 50GB Google Drive Node & Earnings
    private val _creatorStats = MutableStateFlow(com.example.data.model.CreatorCloudStats())
    val creatorStats: StateFlow<com.example.data.model.CreatorCloudStats> = _creatorStats.asStateFlow()

    private val _creatorTracks = MutableStateFlow<List<com.example.data.model.CreatorTrack>>(
        listOf(
            com.example.data.model.CreatorTrack(
                id = "ct_01",
                title = "Neon Genesis Arkaios",
                artist = "Arkaios Sound Creator",
                album = "Treasure Masters Vol. 1",
                genre = "Synthwave / Cyberpunk",
                durationSeconds = 215,
                fileSizeMb = 34.2,
                streamsCount = 890,
                amrEarned = 4.45,
                uploadDate = "24 Ago 2026",
                gdriveFileId = "14EVvOGfhLuhknCEz3uWqKkrNGPbukuWZ_f01",
                gdriveShareUrl = "https://cdn.arkaios.cloud/stream/genesis_neon.flac",
                coverUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=400"
            ),
            com.example.data.model.CreatorTrack(
                id = "ct_02",
                title = "Quantum Echoes FLAC",
                artist = "Arkaios Sound Creator",
                album = "Treasure Masters Vol. 1",
                genre = "Electronic / Ambient",
                durationSeconds = 188,
                fileSizeMb = 28.6,
                streamsCount = 530,
                amrEarned = 2.65,
                uploadDate = "25 Ago 2026",
                gdriveFileId = "14EVvOGfhLuhknCEz3uWqKkrNGPbukuWZ_f02",
                gdriveShareUrl = "https://cdn.arkaios.cloud/stream/quantum_echoes.flac",
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400"
            )
        )
    )
    val creatorTracks: StateFlow<List<com.example.data.model.CreatorTrack>> = _creatorTracks.asStateFlow()

    private val _isCreatorStudioOpen = MutableStateFlow(false)
    val isCreatorStudioOpen: StateFlow<Boolean> = _isCreatorStudioOpen.asStateFlow()

    // Track for Add To Playlist Dialog
    private val _trackForPlaylistDialog = MutableStateFlow<Track?>(null)
    val trackForPlaylistDialog: StateFlow<Track?> = _trackForPlaylistDialog.asStateFlow()

    // Downloader Sheet Visibility
    private val _isDownloaderSheetOpen = MutableStateFlow(false)
    val isDownloaderSheetOpen: StateFlow<Boolean> = _isDownloaderSheetOpen.asStateFlow()

    // App Update OTA State
    private val _appUpdateInfo = MutableStateFlow<com.example.data.update.AppUpdateInfo?>(null)
    val appUpdateInfo: StateFlow<com.example.data.update.AppUpdateInfo?> = _appUpdateInfo.asStateFlow()

    // Community Voting & Live Listening Status State
    private val _isCommunityVotingOpen = MutableStateFlow(false)
    val isCommunityVotingOpen: StateFlow<Boolean> = _isCommunityVotingOpen.asStateFlow()

    private val _votedTracks = MutableStateFlow<List<com.example.data.model.VotedTrack>>(
        listOf(
            com.example.data.model.VotedTrack(
                id = "vote_01",
                title = "Neon Genesis Arkaios",
                artist = "Arkaios Sound Creator",
                audioUrl = "https://cdn.arkaios.cloud/stream/genesis_neon.flac",
                coverUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=400",
                uploadedBy = "@ArkaiosMaster",
                votesCount = 142,
                userHasVoted = true,
                isOriginalMelody = true
            ),
            com.example.data.model.VotedTrack(
                id = "vote_02",
                title = "Quantum Echoes FLAC",
                artist = "Melody Maker Pro",
                audioUrl = "https://cdn.arkaios.cloud/stream/quantum_echoes.flac",
                coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400",
                uploadedBy = "@MelodyMaker",
                votesCount = 98,
                userHasVoted = false,
                isOriginalMelody = true
            ),
            com.example.data.model.VotedTrack(
                id = "vote_03",
                title = "Starlight Cyber Symphony",
                artist = "Aura Synthesizer",
                audioUrl = "https://cdn.arkaios.cloud/stream/starlight.mp3",
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400",
                uploadedBy = "@AuraUser",
                votesCount = 67,
                userHasVoted = false,
                isOriginalMelody = false
            )
        )
    )
    val votedTracks: StateFlow<List<com.example.data.model.VotedTrack>> = _votedTracks.asStateFlow()

    private val _userListeningStatuses = MutableStateFlow<List<com.example.data.model.UserListeningStatus>>(
        listOf(
            com.example.data.model.UserListeningStatus(
                id = "ls_01",
                username = "DJ Prometeo",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                songTitle = "Neon Genesis Arkaios",
                artistName = "Arkaios Sound Creator",
                timestampText = "Ahora mismo"
            ),
            com.example.data.model.UserListeningStatus(
                id = "ls_02",
                username = "BeatMaster99",
                avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=200",
                songTitle = "Blinding Lights",
                artistName = "The Weeknd",
                timestampText = "Hace 1 min"
            ),
            com.example.data.model.UserListeningStatus(
                id = "ls_03",
                username = "MelodyQueen",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
                songTitle = "Quantum Echoes FLAC",
                artistName = "Melody Maker Pro",
                timestampText = "Hace 3 min"
            )
        )
    )
    val userListeningStatuses: StateFlow<List<com.example.data.model.UserListeningStatus>> = _userListeningStatuses.asStateFlow()

    init {
        checkForUpdates()
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            val info = com.example.data.update.AppUpdateManager.checkForUpdates()
            if (info.isUpdateAvailable) {
                _appUpdateInfo.value = info
            }
        }
    }

    fun dismissUpdateDialog() {
        _appUpdateInfo.value = null
    }

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenreFilter = MutableStateFlow<String?>(null)
    val selectedGenreFilter: StateFlow<String?> = _selectedGenreFilter.asStateFlow()

    // Filtered search results combining Local Catalog and Live TIDAL API Cloud Streams
    val searchResults: StateFlow<List<Track>> = combine(
        _catalogTracks,
        _tidalSearchResults,
        _searchQuery,
        _selectedGenreFilter
    ) { catalog, tidalResults, query, genre ->
        val trimmed = query.trim()
        val localFiltered = if (trimmed.isNotBlank()) {
            catalog.filter {
                it.title.contains(trimmed, ignoreCase = true) ||
                it.artist.contains(trimmed, ignoreCase = true) ||
                it.album.contains(trimmed, ignoreCase = true) ||
                it.genre.contains(trimmed, ignoreCase = true)
            }
        } else {
            catalog
        }

        // Combine local results with live TIDAL API search results (avoiding duplicate IDs)
        val combined = mutableListOf<Track>()
        combined.addAll(localFiltered)
        for (t in tidalResults) {
            if (combined.none { it.id == t.id || (it.title.equals(t.title, ignoreCase = true) && it.artist.equals(t.artist, ignoreCase = true)) }) {
                combined.add(t)
            }
        }

        var finalList = combined.toList()
        if (!genre.isNullOrEmpty()) {
            finalList = finalList.filter { it.genre.contains(genre, ignoreCase = true) }
        }
        finalList
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Arkaios Pay Checkout Modal State
    private val _checkoutModal = MutableStateFlow(CheckoutModalState())
    val checkoutModal: StateFlow<CheckoutModalState> = _checkoutModal.asStateFlow()

    // Full Player Expanded Modal
    private val _isFullPlayerExpanded = MutableStateFlow(false)
    val isFullPlayerExpanded: StateFlow<Boolean> = _isFullPlayerExpanded.asStateFlow()

    // Snack Message Toast
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Active screen navigation tab: 0=Home, 1=Search, 2=Library, 3=Arkaios Pay Store
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    init {
        viewModelScope.launch {
            amrWalletRepository.initializeWallet()
            val initial = musicRepository.getInitialCatalog()
            _catalogTracks.value = initial
            scanLocalMusic()
        }
    }

    val selectedSourceFilter: StateFlow<com.example.data.tidal.MusicSourceFilter> = tidalApiService.selectedSource

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun setSourceFilter(source: com.example.data.tidal.MusicSourceFilter) {
        tidalApiService.setSourceFilter(source)
        val trimmed = _searchQuery.value.trim()
        if (trimmed.isNotBlank()) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                _isTidalSearching.value = true
                val results = tidalApiService.searchMultiSourceTracks(trimmed, source)
                _tidalSearchResults.value = results
                _isTidalSearching.value = false
            }
        }
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            _tidalSearchResults.value = emptyList()
            _isTidalSearching.value = false
            return
        }
        searchJob = viewModelScope.launch {
            _isTidalSearching.value = true
            kotlinx.coroutines.delay(200) // debounce
            val results = tidalApiService.searchMultiSourceTracks(trimmed, tidalApiService.selectedSource.value)
            _tidalSearchResults.value = results
            _isTidalSearching.value = false
        }
    }

    fun setGenreFilter(genreName: String?) {
        _selectedGenreFilter.value = if (_selectedGenreFilter.value == genreName) null else genreName
    }

    fun playTrack(track: Track, fromQueue: List<Track>? = null) {
        viewModelScope.launch {
            val resolvedTrack = if (track.audioUrl.contains("playbackinfopostpaywall") || track.audioUrl.isBlank()) {
                val streamInfo = tidalApiService.getPlaybackStreamInfo(track.tidalId ?: track.id)
                track.copy(audioUrl = streamInfo.streamUrl)
            } else {
                track
            }

            // Intelligently resolve active queue from explicit param or matching track collection
            val activeQueue = when {
                !fromQueue.isNullOrEmpty() -> {
                    if (fromQueue.any { it.id == track.id }) fromQueue else listOf(track) + fromQueue
                }
                _localDeviceTracks.value.any { it.id == track.id } -> {
                    _localDeviceTracks.value
                }
                downloadedTracks.value.any { it.id == track.id } -> {
                    downloadedTracks.value
                }
                favoriteTracks.value.any { it.id == track.id } -> {
                    favoriteTracks.value
                }
                searchResults.value.any { it.id == track.id } -> {
                    searchResults.value
                }
                _catalogTracks.value.any { it.id == track.id } -> {
                    _catalogTracks.value
                }
                else -> {
                    listOf(track)
                }
            }

            val index = activeQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
            val updatedQueue = activeQueue.map { if (it.id == track.id) resolvedTrack else it }
            playerEngine.setQueue(updatedQueue, index)
        }
    }

    fun togglePlayPause() {
        playerEngine.togglePlayPause()
    }

    fun nextTrack() {
        playerEngine.nextTrack()
    }

    fun previousTrack() {
        playerEngine.previousTrack()
    }

    fun seekTo(positionMs: Long) {
        playerEngine.seekTo(positionMs)
    }

    fun toggleShuffle() {
        playerEngine.toggleShuffle()
    }

    fun cycleRepeatMode() {
        playerEngine.cycleRepeatMode()
    }

    fun setPlaybackSpeed(speed: Float) {
        playerEngine.setPlaybackSpeed(speed)
    }

    fun setEqualizerPreset(preset: String) {
        playerEngine.setEqualizerPreset(preset)
        showSnackbar("Preset Ecualizador: $preset")
    }

    fun setFullPlayerExpanded(expanded: Boolean) {
        _isFullPlayerExpanded.value = expanded
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(track.id, track.isFavorite)
            val updated = _catalogTracks.value.map {
                if (it.id == track.id) it.copy(isFavorite = !track.isFavorite) else it
            }
            _catalogTracks.value = updated
            showSnackbar(if (!track.isFavorite) "Añadido a Tus Favoritos ❤️" else "Eliminado de Favoritos")
        }
    }

    fun setDownloaderSheetOpen(open: Boolean) {
        _isDownloaderSheetOpen.value = open
    }

    fun enqueueBackgroundDownload(
        track: Track,
        format: String = "MP3",
        engine: DownloadEngineSource = DownloadEngineSource.AUTO_FALLBACK
    ) {
        backgroundDownloadEngine.enqueueTrack(track, preferredFormat = format, preferredEngine = engine)
        showSnackbar("⬇️ Descargando \"${track.title}\" en 2do plano...")
        viewModelScope.launch {
            amrWalletRepository.rewardListeningContribution(5.00, "Bono por Almacenamiento Nodo Local")
        }
    }

    fun enqueueCustomUrlDownload(
        url: String,
        format: String = "MP3",
        engine: DownloadEngineSource = DownloadEngineSource.AUTO_FALLBACK
    ) {
        backgroundDownloadEngine.enqueueCustomUrl(url, preferredFormat = format, preferredEngine = engine)
        showSnackbar("⬇️ Enlace enviado al motor en 2do plano...")
    }

    fun cancelBackgroundTask(taskId: String) {
        backgroundDownloadEngine.cancelTask(taskId)
        showSnackbar("Tarea cancelada")
    }

    fun retryBackgroundTask(taskId: String) {
        backgroundDownloadEngine.retryTask(taskId)
        showSnackbar("Reintentando descarga en 2do plano...")
    }

    fun clearCompletedBackgroundTasks() {
        backgroundDownloadEngine.clearCompleted()
        showSnackbar("Historial de descargas limpiado")
    }

    fun downloadTrack(track: Track) {
        enqueueBackgroundDownload(track, format = track.audioFormat)
    }

    fun removeDownload(track: Track) {
        viewModelScope.launch {
            downloadRepository.removeDownload(track)
            showSnackbar("Pista eliminada de descargas locales")
        }
    }

    fun scanLocalMusic() {
        viewModelScope.launch {
            val locals = musicRepository.scanDeviceLocalAudio()
            _localDeviceTracks.value = locals
            if (locals.isNotEmpty()) {
                val merged = (_catalogTracks.value + locals).distinctBy { it.id }
                _catalogTracks.value = merged
                showSnackbar("🎵 Se encontraron ${locals.size} canciones en tu teléfono")
            } else {
                showSnackbar("No se encontraron audios en carpetas del sistema. Usa 'Elegir Archivos' para seleccionarlos directamente.")
            }
        }
    }

    fun importAudioUris(uris: List<android.net.Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val imported = musicRepository.importTracksFromUris(uris)
            if (imported.isNotEmpty()) {
                val existing = _localDeviceTracks.value
                val combined = (imported + existing).distinctBy { it.audioUrl }
                _localDeviceTracks.value = combined
                _catalogTracks.value = (_catalogTracks.value + imported).distinctBy { it.id }
                showSnackbar("✔ Se importaron ${imported.size} canciones con éxito")
            }
        }
    }

    fun importM3uUri(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val tracks = com.example.data.m3u.M3uParser.parseFromUri(getApplication(), uri)
                if (tracks.isNotEmpty()) {
                    val plId = "pl_m3u_${System.currentTimeMillis()}"
                    val playlistName = "Lista M3U Importada (${tracks.size} tracks)"
                    val pl = com.example.data.local.PlaylistEntity(
                        id = plId,
                        title = playlistName,
                        description = "Importado desde archivo .m3u",
                        coverUrl = tracks.firstOrNull()?.coverUrl ?: "",
                        author = _userProfile.value.displayName
                    )
                    db.playlistDao().insertPlaylist(pl)
                    for (track in tracks) {
                        db.trackDao().insertOrUpdateTrack(com.example.data.local.TrackEntity.fromTrack(track))
                        db.playlistDao().insertTrackToPlaylist(
                            com.example.data.local.PlaylistTrackCrossRefEntity(
                                playlistId = plId,
                                trackId = track.id
                            )
                        )
                    }
                    _playlistTracksMap.value = _playlistTracksMap.value.toMutableMap().apply {
                        put(plId, tracks)
                    }
                    _catalogTracks.value = (_catalogTracks.value + tracks).distinctBy { it.id }
                    showSnackbar("📻 Lista .M3U importada: ${tracks.size} canciones añadidas a tu biblioteca")
                } else {
                    showSnackbar("No se pudieron extraer canciones válidas del archivo .M3U")
                }
            } catch (e: Exception) {
                showSnackbar("Error al procesar archivo .M3U: ${e.message}")
            }
        }
    }

    fun importM3uFromUrl(url: String, name: String = "Radio M3U") {
        if (url.isBlank()) return
        viewModelScope.launch {
            try {
                val tracks = com.example.data.m3u.M3uParser.parseFromUrl(url)
                if (tracks.isNotEmpty()) {
                    val plId = "pl_m3u_url_${System.currentTimeMillis()}"
                    val pl = com.example.data.local.PlaylistEntity(
                        id = plId,
                        title = name,
                        description = "Enlace M3U Online: $url",
                        coverUrl = tracks.firstOrNull()?.coverUrl ?: "",
                        author = "M3U Stream"
                    )
                    db.playlistDao().insertPlaylist(pl)
                    for (track in tracks) {
                        db.trackDao().insertOrUpdateTrack(com.example.data.local.TrackEntity.fromTrack(track))
                        db.playlistDao().insertTrackToPlaylist(
                            com.example.data.local.PlaylistTrackCrossRefEntity(
                                playlistId = plId,
                                trackId = track.id
                            )
                        )
                    }
                    _playlistTracksMap.value = _playlistTracksMap.value.toMutableMap().apply {
                        put(plId, tracks)
                    }
                    _catalogTracks.value = (_catalogTracks.value + tracks).distinctBy { it.id }
                    showSnackbar("📻 Sintonizadas ${tracks.size} emisoras/temas desde la URL M3U")
                } else {
                    showSnackbar("No se encontraron transmisiones activas en la URL M3U proporcionada")
                }
            } catch (e: Exception) {
                showSnackbar("Error al conectar con la lista M3U: ${e.message}")
            }
        }
    }

    fun playRadioStation(station: com.example.data.model.RadioStation) {
        val radioTrack = station.toTrack()
        playTrack(radioTrack)
        showSnackbar("📻 Sintonizando en vivo: ${station.name} (${station.genre})")
    }

    fun addCustomRadioStation(name: String, genre: String, url: String) {
        radioStationRepository.addCustomStation(name, genre, url)
        showSnackbar("📻 Estación \"$name\" agregada exitosamente")
    }

    fun createPlaylist(name: String, desc: String) {
        viewModelScope.launch {
            val pl = com.example.data.local.PlaylistEntity(
                id = "pl_${System.currentTimeMillis()}",
                title = name,
                description = desc,
                coverUrl = "",
                author = _userProfile.value.displayName
            )
            db.playlistDao().insertPlaylist(pl)
            showSnackbar("Playlist \"$name\" creada exitosamente 🎵")
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            db.playlistDao().deletePlaylist(playlistId)
            _playlistTracksMap.value = _playlistTracksMap.value.toMutableMap().apply { remove(playlistId) }
            showSnackbar("Playlist eliminada")
        }
    }

    fun openAddToPlaylistDialog(track: Track) {
        _trackForPlaylistDialog.value = track
    }

    fun closeAddToPlaylistDialog() {
        _trackForPlaylistDialog.value = null
    }

    fun addTrackToPlaylist(playlistId: String, track: Track) {
        viewModelScope.launch {
            // Ensure track is cached in Room
            db.trackDao().insertOrUpdateTrack(com.example.data.local.TrackEntity.fromTrack(track))
            db.playlistDao().insertTrackToPlaylist(
                com.example.data.local.PlaylistTrackCrossRefEntity(
                    playlistId = playlistId,
                    trackId = track.id
                )
            )
            // Refresh map
            val currentList = _playlistTracksMap.value[playlistId] ?: emptyList()
            if (currentList.none { it.id == track.id }) {
                _playlistTracksMap.value = _playlistTracksMap.value.toMutableMap().apply {
                    put(playlistId, currentList + track)
                }
            }
            showSnackbar("✔ \"${track.title}\" añadida a la playlist")
        }
    }

    fun createPlaylistAndAddTrack(title: String, track: Track) {
        viewModelScope.launch {
            val newId = "pl_${System.currentTimeMillis()}"
            val pl = com.example.data.local.PlaylistEntity(
                id = newId,
                title = title,
                description = "Creada desde el reproductor",
                coverUrl = track.coverUrl,
                author = _userProfile.value.displayName
            )
            db.playlistDao().insertPlaylist(pl)
            addTrackToPlaylist(newId, track)
        }
    }

    fun playPlaylist(tracks: List<Track>, shuffle: Boolean = false) {
        if (tracks.isEmpty()) return
        val list = if (shuffle) tracks.shuffled() else tracks
        playerEngine.setQueue(list, 0)
        showSnackbar("▶ Reproduciendo playlist (${tracks.size} pistas)")
    }

    fun openAlbumModal(album: Album) {
        _selectedAlbumForModal.value = album
    }

    fun closeAlbumModal() {
        _selectedAlbumForModal.value = null
    }

    fun playAlbum(tracks: List<Track>, shuffle: Boolean = false) {
        if (tracks.isEmpty()) return
        val list = if (shuffle) tracks.shuffled() else tracks
        playerEngine.setQueue(list, 0)
        showSnackbar("▶ Reproduciendo álbum completo (${tracks.size} pistas)")
    }

    fun downloadAlbum(tracks: List<Track>) {
        val isEncrypted = _userProfile.value.offlineEncryptedCacheEnabled
        viewModelScope.launch {
            tracks.forEach { track ->
                if (!track.isDownloaded) {
                    downloadRepository.downloadTrack(track, isPremiumVaultEncrypted = isEncrypted)
                }
            }
            showSnackbar("⬇️ Descargando ${tracks.size} pistas del álbum en caché offline precifrado (.arkcache)")
        }
    }

    fun openAuthModal() {
        _isAuthModalOpen.value = true
    }

    fun closeAuthModal() {
        _isAuthModalOpen.value = false
    }

    fun loginWithGoogle() {
        viewModelScope.launch {
            _userProfile.value = _userProfile.value.copy(
                displayName = "Arkaios Master (Google)",
                email = "arkaios2026@gmail.com",
                isGoogleLinked = true,
                tier = com.example.data.model.UserTier.ARKAIOS_PREMIUM_HIFI
            )
            showSnackbar("✔ Conectado exitosamente con Google Account")
        }
    }

    fun loginWithEmail(email: String) {
        viewModelScope.launch {
            val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            _userProfile.value = _userProfile.value.copy(
                displayName = name,
                email = email,
                tier = com.example.data.model.UserTier.ARKAIOS_PREMIUM_HIFI
            )
            showSnackbar("✔ Sesión iniciada con $email")
        }
    }

    fun toggleOfflineCache(enabled: Boolean) {
        _userProfile.value = _userProfile.value.copy(offlineEncryptedCacheEnabled = enabled)
        showSnackbar(if (enabled) "🔒 Modo Caché Precifrado .arkcache Activado" else "Caché Offline en modo estándar")
    }

    fun updateUserTier(tier: com.example.data.model.UserTier) {
        _userProfile.value = _userProfile.value.copy(tier = tier)
        showSnackbar("Membresía actualizada: ${tier.name}")
    }

    // Arkaios Pay Checkout Triggers
    fun openArkaiosPayCheckout(tier: ArkaiosPremiumTier) {
        _checkoutModal.value = CheckoutModalState(
            isOpen = true,
            tier = tier,
            customAmount = tier.priceAmr,
            customConcept = tier.title,
            isProcessing = false,
            isSuccess = false,
            errorMessage = null
        )
    }

    fun openCustomCheckout(amount: Double, concept: String) {
        _checkoutModal.value = CheckoutModalState(
            isOpen = true,
            tier = null,
            customAmount = amount,
            customConcept = concept,
            isProcessing = false,
            isSuccess = false,
            errorMessage = null
        )
    }

    fun confirmArkaiosPayCheckout() {
        val current = _checkoutModal.value
        val amount = current.customAmount
        val concept = current.customConcept

        viewModelScope.launch {
            _checkoutModal.value = current.copy(isProcessing = true, errorMessage = null)
            val result = amrWalletRepository.processPayment(amount, concept)

            result.onSuccess { tx ->
                _checkoutModal.value = current.copy(
                    isProcessing = false,
                    isSuccess = true,
                    lastTx = tx
                )
                showSnackbar("✔ ¡Pago de ${amount} AMR procesado exitosamente!")
            }.onFailure { err ->
                _checkoutModal.value = current.copy(
                    isProcessing = false,
                    isSuccess = false,
                    errorMessage = err.message
                )
            }
        }
    }

    fun closeCheckoutModal() {
        _checkoutModal.value = CheckoutModalState(isOpen = false)
    }

    fun confirmPayPalCheckout() {
        val current = _checkoutModal.value
        val amountAmr = current.customAmount
        val concept = current.customConcept
        // Convert AMR price to approx USD equivalent (1 USD = 1 AMR for direct tier, or tier USD price)
        val usdPrice = when {
            concept.contains("God Owner", ignoreCase = true) -> 4.99
            concept.contains("Tidal", ignoreCase = true) -> 2.99
            concept.contains("Creator", ignoreCase = true) -> 3.99
            else -> (amountAmr * 0.10).coerceAtLeast(0.99)
        }

        viewModelScope.launch {
            _checkoutModal.value = current.copy(isProcessing = true, errorMessage = null)
            val result = amrWalletRepository.processPayPalPayment(
                amountUsd = usdPrice,
                concept = concept,
                isMembership = current.tier != null
            )

            result.onSuccess { tx ->
                _checkoutModal.value = current.copy(
                    isProcessing = false,
                    isSuccess = true,
                    lastTx = tx
                )
                showSnackbar("✔ ¡Pago de $$usdPrice USD aprobado con PayPal v6 SDK!")
            }.onFailure { err ->
                _checkoutModal.value = current.copy(
                    isProcessing = false,
                    isSuccess = false,
                    errorMessage = err.message
                )
            }
        }
    }

    fun buyTokensWithPayPal(usdAmount: Double, tokenAmount: Double) {
        viewModelScope.launch {
            val result = amrWalletRepository.processPayPalPayment(
                amountUsd = usdAmount,
                concept = "Compra de $tokenAmount Tokens AMR",
                isMembership = false
            )
            result.onSuccess {
                showSnackbar("✔ ¡Recibiste +${"%.0f".format(tokenAmount)} AMR vía PayPal ($$usdAmount USD)!")
            }.onFailure {
                showSnackbar("✖ Error en pasarela PayPal: ${it.message}")
            }
        }
    }

    fun transferAmrTokens(toAddress: String, amount: Double) {
        viewModelScope.launch {
            val res = amrWalletRepository.transferTokens(toAddress, amount)
            res.onSuccess {
                showSnackbar("✔ Transferencia de ${amount} AMR enviada a $toAddress")
            }.onFailure {
                showSnackbar("✖ Error en transferencia: ${it.message}")
            }
        }
    }

    // Tidal API & Cloud Audio Servers Controls
    fun setTidalModalOpen(open: Boolean) {
        _isTidalModalOpen.value = open
    }

    fun updateTidalToken(newToken: String) {
        tidalApiService.updateClientToken(newToken)
        showSnackbar("Token TIDAL actualizado: $newToken")
    }

    fun updateTidalQuality(quality: TidalAudioQuality) {
        tidalApiService.updateAudioQuality(quality)
        showSnackbar("Calidad TIDAL: ${quality.displayName}")
    }

    fun updateTidalProvider(provider: AudioServerProvider) {
        tidalApiService.updateProvider(provider)
        showSnackbar("Servidor activo: ${provider.displayName}")
    }

    fun searchTidal(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isTidalSearching.value = true
            val results = tidalApiService.searchTidalTracks(query)
            _tidalSearchResults.value = results
            _isTidalSearching.value = false
            showSnackbar("TIDAL API: ${results.size} canciones encontradas")
        }
    }

    fun playTidalTrack(track: Track) {
        viewModelScope.launch {
            val streamInfo = tidalApiService.getPlaybackStreamInfo(track.tidalId ?: track.id)
            val updatedTrack = track.copy(audioUrl = streamInfo.streamUrl)
            playTrack(updatedTrack)
            showSnackbar("▶ Transmitiendo ${track.title} [${streamInfo.audioQuality} Master]")
        }
    }

    // Creator Studio & Google Drive 5TB Hosting Actions
    fun setCreatorStudioOpen(open: Boolean) {
        _isCreatorStudioOpen.value = open
    }

    fun uploadCreatorTrack(
        title: String,
        artist: String,
        album: String,
        genre: String,
        fileMb: Double
    ) {
        viewModelScope.launch {
            val newId = "ct_${System.currentTimeMillis()}"
            val newFileId = "14EVvOGfhLuhknCEz3uWqKkrNGPbukuWZ_f${System.currentTimeMillis().toString().takeLast(4)}"
            val covers = listOf(
                "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400",
                "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400",
                "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400"
            )
            val randomCover = covers.random()

            val newCreatorTrack = com.example.data.model.CreatorTrack(
                id = newId,
                title = title,
                artist = artist,
                album = album,
                genre = genre,
                durationSeconds = (180..260).random(),
                fileSizeMb = fileMb,
                streamsCount = 1,
                amrEarned = 0.005,
                uploadDate = "Hoy",
                gdriveFileId = newFileId,
                gdriveShareUrl = "https://cdn.arkaios.cloud/stream/$newId.flac",
                coverUrl = randomCover,
                audioFormat = "FLAC 24-bit"
            )

            // Add to Creator Tracks list
            _creatorTracks.value = listOf(newCreatorTrack) + _creatorTracks.value

            // Add to general catalog for playback
            _catalogTracks.value = listOf(newCreatorTrack.toCatalogTrack()) + _catalogTracks.value

            // Update stats
            val currentStats = _creatorStats.value
            _creatorStats.value = currentStats.copy(
                totalTracksUploaded = currentStats.totalTracksUploaded + 1,
                usedStorageGb = currentStats.usedStorageGb + (fileMb / 1024.0)
            )

            showSnackbar("✔ \"$title\" subida a Google Drive 5TB (Carpeta: /Creators/amr_arkaios2026)")
        }
    }

    fun claimCreatorEarnings() {
        viewModelScope.launch {
            val stats = _creatorStats.value
            val earned = stats.totalAmrEarned
            if (earned <= 0) {
                showSnackbar("No tienes regalías pendientes por reclamar")
                return@launch
            }

            // Add reward to AMR Wallet
            amrWalletRepository.rewardListeningContribution(earned, "Regalías de Creador Arkaios 50GB Node")
            _creatorStats.value = stats.copy(totalAmrEarned = 0.0)
            showSnackbar("✔ Reclamaste ${"%.2f".format(earned)} AMR depositados a tu Cartera")
        }
    }

    fun setCommunityVotingOpen(open: Boolean) {
        _isCommunityVotingOpen.value = open
    }

    fun voteTrack(trackId: String) {
        val currentList = _votedTracks.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == trackId }
        if (index != -1) {
            val item = currentList[index]
            val newHasVoted = !item.userHasVoted
            val newVoteCount = if (newHasVoted) item.votesCount + 1 else (item.votesCount - 1).coerceAtLeast(0)
            currentList[index] = item.copy(userHasVoted = newHasVoted, votesCount = newVoteCount)
            _votedTracks.value = currentList.sortedByDescending { it.votesCount }

            if (newHasVoted) {
                showSnackbar("🗳️ ¡Tu voto fue registrado para \"${item.title}\"!")
            } else {
                showSnackbar("Voto retirado de \"${item.title}\"")
            }
        }
    }

    fun proposeSongForVoting(title: String, artist: String) {
        val newTrack = com.example.data.model.VotedTrack(
            id = "vote_user_" + System.currentTimeMillis(),
            title = title,
            artist = artist,
            audioUrl = "https://cdn.arkaios.cloud/stream/user_proposal.mp3",
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400",
            uploadedBy = "@ArkaiosMaster",
            votesCount = 1,
            userHasVoted = true,
            isOriginalMelody = true
        )
        val currentList = _votedTracks.value.toMutableList()
        currentList.add(0, newTrack)
        _votedTracks.value = currentList.sortedByDescending { it.votesCount }
        showSnackbar("🌟 ¡Tu propuesta \"$title\" fue publicada y ya puede ser votada!")
    }

    fun showSnackbar(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        playerEngine.release()
    }
}
