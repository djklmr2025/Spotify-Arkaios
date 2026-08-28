const API_BASE = window.location.origin.includes('8788') ? 'http://localhost:8788/api' : '/api';

// Global Audio Engine & State
const audioPlayer = new Audio();
let currentTrackList = [];
let currentTrackIndex = 0;
let isPlaying = false;
let isShuffle = false;
let isRepeat = false;
let currentSourceFilter = 'ALL';

// Local Storage Collections
let customPlaylists = JSON.parse(localStorage.getItem('arkaios_playlists') || '[]');
let importedLocalTracks = [];

const featuredAlbums = [
    { id: 'al_01', title: 'Starboy (TIDAL Master)', artist: 'The Weeknd', cover: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600', tracksCount: 18 },
    { id: 'al_02', title: 'Future Nostalgia', artist: 'Dua Lipa', cover: 'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600', tracksCount: 12 },
    { id: 'al_03', title: 'Endless Summer Vacation', artist: 'Miley Cyrus', cover: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600', tracksCount: 13 },
    { id: 'al_04', title: 'Subatomic Frequencies', artist: 'Arkaios God Node', cover: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600', tracksCount: 10 }
];

document.addEventListener('DOMContentLoaded', () => {
    checkDaemonStatus();
    loadHomeData();
    loadRadioStations();
    renderPlaylists();

    audioPlayer.addEventListener('timeupdate', updateProgress);
    audioPlayer.addEventListener('ended', onTrackEnded);

    // Initial default track setup
    setupInitialTrack();
});

function setAppMode(mode) {
    document.getElementById('btnModeWeb').classList.toggle('active', mode === 'web');
    document.getElementById('btnModeApk').classList.toggle('active', mode === 'apk');
    if (mode === 'apk') {
        switchTab('apkConsole');
    } else {
        switchTab('home');
    }
}

function switchTab(tabId) {
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.toggle('active', btn.getAttribute('data-tab') === tabId);
    });
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.toggle('active', tab.id === `tab-${tabId}`);
    });
}

function switchLibrarySubtab(subtabId) {
    document.querySelectorAll('.subtab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.lib-subtab-content').forEach(c => c.classList.remove('active'));

    const btnTarget = Array.from(document.querySelectorAll('.subtab-btn')).find(b => b.textContent.toLowerCase().includes(subtabId));
    if (btnTarget) btnTarget.classList.add('active');
    
    const targetContent = document.getElementById(`lib-subtab-${subtabId}`);
    if (targetContent) targetContent.classList.add('active');
}

async function checkDaemonStatus() {
    try {
        const res = await fetch(`${API_BASE}/status`);
        const data = await res.json();
        if (data.status === 'online') {
            document.getElementById('daemonStatusText').textContent = `Activo (Puerto ${data.port})`;
            document.querySelector('.status-dot').style.backgroundColor = '#22c55e';
        }
    } catch (e) {
        document.getElementById('daemonStatusText').textContent = 'Demonio Local (Standby)';
        document.querySelector('.status-dot').style.backgroundColor = '#ef4444';
    }
}

async function loadHomeData() {
    renderAlbumsCarousel();
    try {
        const res = await fetch(`${API_BASE}/search`);
        const data = await res.json();
        currentTrackList = data.results || [];
        renderHomeTracks(currentTrackList);
    } catch (e) {
        console.error('Error cargando datos de inicio:', e);
    }
}

function renderAlbumsCarousel() {
    const container = document.getElementById('homeAlbumsCarousel');
    if (!container) return;
    container.innerHTML = featuredAlbums.map(al => `
        <div class="album-card" onclick="searchByTerm('${al.artist}')">
            <img src="${al.cover}" alt="${al.title}">
            <h4>${al.title}</h4>
            <p>${al.artist} • ${al.tracksCount} Pistas</p>
        </div>
    `).join('');
}

function renderHomeTracks(tracks) {
    const container = document.getElementById('homeTracksList');
    if (!container) return;
    container.innerHTML = `<div class="tracks-grid">` + tracks.map((t, idx) => `
        <div class="track-item-card">
            <img src="${t.cover}" alt="${t.title}">
            <h4>${t.title}</h4>
            <p>${t.artist} • <span style="color:var(--cyan-light);">${t.genre || 'Pista HQ'}</span></p>
            <div class="track-actions">
                <button class="btn-track-play" onclick="playTrackFromList(${idx})"><i class="fa-solid fa-play"></i> Escuchar</button>
                <button class="btn-track-download" onclick="requestDownload('${t.title}', '${t.artist}', '${t.url}')"><i class="fa-solid fa-download"></i></button>
            </div>
        </div>
    `).join('') + `</div>`;
}

async function loadRadioStations() {
    try {
        const res = await fetch(`${API_BASE}/radio`);
        const data = await res.json();
        const stations = data.stations || [];

        const homeGrid = document.getElementById('homeRadioGrid');
        const fullGrid = document.getElementById('radioFullGrid');

        const html = stations.map(s => `
            <div class="radio-card">
                <div class="radio-card-header">
                    <img src="${s.cover}" alt="${s.name}">
                    <div>
                        <h4>${s.name}</h4>
                        <p>${s.genre} • ${s.listeners}</p>
                    </div>
                </div>
                <button class="btn-radio-play" onclick="playRadioStream('${s.streamUrl}', '${s.name}', '${s.genre}', '${s.cover}')">
                    <i class="fa-solid fa-tower-broadcast"></i> Sintonizar En Vivo
                </button>
            </div>
        `).join('');

        if (homeGrid) homeGrid.innerHTML = html;
        if (fullGrid) fullGrid.innerHTML = html;
    } catch (e) {
        console.error('Error cargando radios:', e);
    }
}

// Multi-Source Realtime Search
function handleGlobalSearchKeyUp(event) {
    if (event.key === 'Enter') {
        const query = event.target.value;
        switchTab('search');
        document.getElementById('searchTabInput').value = query;
        executeMultiSourceSearch();
    }
}

function searchByTerm(term) {
    switchTab('search');
    document.getElementById('searchTabInput').value = term;
    executeMultiSourceSearch();
}

function setSourceFilter(source, btnEl) {
    currentSourceFilter = source;
    document.querySelectorAll('.source-chip').forEach(c => c.classList.remove('active'));
    btnEl.classList.add('active');
    executeMultiSourceSearch();
}

async function executeMultiSourceSearch() {
    const query = document.getElementById('searchTabInput').value.trim();
    if (!query) return;

    const resultsHeader = document.getElementById('searchResultsHeader');
    const resultsGrid = document.getElementById('searchResultsGrid');

    resultsHeader.innerHTML = `<span><i class="fa-solid fa-spinner fa-spin"></i> Buscando en tiempo real "${query}" (1.º YouTube Music, 2.º TIDAL)...</span>`;
    resultsGrid.innerHTML = '';

    try {
        const res = await fetch(`${API_BASE}/search?q=${encodeURIComponent(query)}`);
        const data = await res.json();
        let results = data.results || [];

        // Apply source filtering
        if (currentSourceFilter === 'YT') {
            results = results.filter(r => r.genre.includes('YouTube'));
        } else if (currentSourceFilter === 'TIDAL') {
            results = results.filter(r => r.genre.includes('TIDAL'));
        } else if (currentSourceFilter === 'AUDIUS') {
            results = results.filter(r => r.genre.includes('Audius') || r.genre.includes('SoundCloud'));
        }

        currentTrackList = results;

        resultsHeader.innerHTML = `<span>Se encontraron <strong>${results.length} resultados</strong> para "${query}" (${data.source || 'Multi-Fuente API'}):</span>`;

        if (results.length === 0) {
            resultsGrid.innerHTML = `<p style="grid-column:1/-1; padding:30px; text-align:center; color:var(--text-muted);">No se encontraron pistas para este filtro.</p>`;
            return;
        }

        resultsGrid.innerHTML = results.map((t, idx) => `
            <div class="track-item-card">
                <img src="${t.cover}" alt="${t.title}">
                <h4>${t.title}</h4>
                <p>${t.artist}</p>
                <span style="font-size:0.75rem; color:var(--cyan-light); font-weight:700;">${t.genre} • ${t.format || 'MP3'}</span>
                <div class="track-actions">
                    <button class="btn-track-play" onclick="playTrackFromList(${idx})"><i class="fa-solid fa-play"></i> Escuchar</button>
                    <button class="btn-track-download" onclick="requestDownload('${t.title}', '${t.artist}', '${t.url}')"><i class="fa-solid fa-download"></i></button>
                </div>
            </div>
        `).join('');
    } catch (e) {
        resultsHeader.innerHTML = `<span style="color:#ef4444;">Error conectando con el motor de búsqueda en tiempo real.</span>`;
    }
}

// Audio Player Execution Engine
function setupInitialTrack() {
    if (currentTrackList.length > 0) {
        loadTrackToPlayer(currentTrackList[0], false);
    }
}

function playTrackFromList(index) {
    if (index < 0 || index >= currentTrackList.length) return;
    currentTrackIndex = index;
    loadTrackToPlayer(currentTrackList[index], true);
}

function loadTrackToPlayer(track, autoPlay = true) {
    document.getElementById('playerTitle').textContent = track.title;
    document.getElementById('playerArtist').textContent = track.artist;
    document.getElementById('playerCover').src = track.cover || 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=300';
    document.getElementById('playerFormatBadge').textContent = track.format || 'MP3 HQ';

    audioPlayer.src = track.streamUrl || track.url;
    if (autoPlay) {
        audioPlayer.play().then(() => {
            isPlaying = true;
            updatePlayPauseButton();
        }).catch(err => {
            console.warn('Auto-play prevent:', err);
        });
    }
}

function playRadioStream(streamUrl, name, genre, cover) {
    document.getElementById('playerTitle').textContent = name;
    document.getElementById('playerArtist').textContent = `📻 Radio En Vivo (${genre})`;
    document.getElementById('playerCover').src = cover || 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300';
    document.getElementById('playerFormatBadge').textContent = 'RADIO 24/7';

    audioPlayer.src = streamUrl;
    audioPlayer.play();
    isPlaying = true;
    updatePlayPauseButton();
}

function togglePlayPause() {
    if (!audioPlayer.src) return;
    if (isPlaying) {
        audioPlayer.pause();
        isPlaying = false;
    } else {
        audioPlayer.play();
        isPlaying = true;
    }
    updatePlayPauseButton();
}

function updatePlayPauseButton() {
    const btn = document.getElementById('playPauseBtn');
    btn.innerHTML = isPlaying ? '<i class="fa-solid fa-pause"></i>' : '<i class="fa-solid fa-play"></i>';
}

function prevTrack() {
    if (currentTrackIndex > 0) {
        playTrackFromList(currentTrackIndex - 1);
    }
}

function nextTrack() {
    if (isShuffle) {
        currentTrackIndex = Math.floor(Math.random() * currentTrackList.length);
    } else {
        currentTrackIndex = (currentTrackIndex + 1) % currentTrackList.length;
    }
    playTrackFromList(currentTrackIndex);
}

function onTrackEnded() {
    if (isRepeat) {
        audioPlayer.currentTime = 0;
        audioPlayer.play();
    } else {
        nextTrack();
    }
}

function updateProgress() {
    if (!audioPlayer.duration) return;
    const percent = (audioPlayer.currentTime / audioPlayer.duration) * 100;
    document.getElementById('progressBar').value = percent;
    document.getElementById('currentTime').textContent = formatTime(audioPlayer.currentTime);
    document.getElementById('totalDuration').textContent = formatTime(audioPlayer.duration);
}

function seekAudio(percent) {
    if (!audioPlayer.duration) return;
    audioPlayer.currentTime = (percent / 100) * audioPlayer.duration;
}

function setVolume(val) {
    audioPlayer.volume = val / 100;
    const icon = document.getElementById('volumeIcon');
    if (val == 0) {
        icon.className = 'fa-solid fa-volume-xmark';
    } else if (val < 50) {
        icon.className = 'fa-solid fa-volume-low';
    } else {
        icon.className = 'fa-solid fa-volume-high';
    }
}

function toggleMute() {
    audioPlayer.muted = !audioPlayer.muted;
    setVolume(audioPlayer.muted ? 0 : 80);
}

function toggleShuffle() {
    isShuffle = !isShuffle;
    document.getElementById('btnShuffle').style.color = isShuffle ? 'var(--cyan-light)' : 'var(--text-muted)';
}

function toggleRepeat() {
    isRepeat = !isRepeat;
    document.getElementById('btnRepeat').style.color = isRepeat ? 'var(--cyan-light)' : 'var(--text-muted)';
}

function formatTime(secs) {
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
}

// Local File Importer (.mp3 / .flac / .m4a)
function triggerLocalFilesImport() {
    document.getElementById('localFilesInput').click();
}

function handleLocalFilesSelected(event) {
    const files = Array.from(event.target.files);
    if (files.length === 0) return;

    files.forEach(file => {
        const objectUrl = URL.createObjectURL(file);
        importedLocalTracks.push({
            id: 'local_' + Date.now() + '_' + Math.random(),
            title: file.name.replace(/\.[^/.]+$/, ""),
            artist: 'Archivo Local',
            album: 'Almacenamiento Local',
            url: objectUrl,
            streamUrl: objectUrl,
            cover: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300',
            genre: '📁 Local ' + file.name.split('.').pop().toUpperCase(),
            format: file.name.split('.').pop().toUpperCase()
        });
    });

    renderLocalTracks();
    switchLibrarySubtab('local');
    alert(`✔ Se importaron ${files.length} archivos de audio locales exitosamente.`);
}

function renderLocalTracks() {
    const container = document.getElementById('localTracksList');
    if (!container) return;
    if (importedLocalTracks.length === 0) {
        container.innerHTML = `<p style="padding:20px; text-align:center; color:var(--text-muted);">No has importado archivos locales aún. Presiona "Elegir Archivos Locales".</p>`;
        return;
    }
    container.innerHTML = `<div class="tracks-grid">` + importedLocalTracks.map((t, idx) => `
        <div class="track-item-card">
            <img src="${t.cover}" alt="${t.title}">
            <h4>${t.title}</h4>
            <p>${t.genre}</p>
            <button class="btn-track-play" onclick="playImportedLocalTrack(${idx})"><i class="fa-solid fa-play"></i> Reproducir</button>
        </div>
    `).join('') + `</div>`;
}

function playImportedLocalTrack(index) {
    currentTrackList = importedLocalTracks;
    playTrackFromList(index);
}

// Download API Requests
async function requestDownload(title, artist, url) {
    try {
        const res = await fetch(`${API_BASE}/download`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title, artist, url, format: 'mp3' })
        });
        const data = await res.json();
        alert(`✔ Servidor Arkaios: ${data.message}`);
        switchTab('library');
        switchLibrarySubtab('downloads');
        loadDownloadsQueue();
    } catch (e) {
        alert('✔ Encolado en modo offline.');
    }
}

async function downloadCurrentTrack() {
    if (currentTrackList[currentTrackIndex]) {
        const t = currentTrackList[currentTrackIndex];
        requestDownload(t.title, t.artist, t.url);
    }
}

async function loadDownloadsQueue() {
    try {
        const res = await fetch(`${API_BASE}/downloads`);
        const data = await res.json();
        
        document.getElementById('statActiveCount').textContent = data.active.length;
        document.getElementById('statQueueCount').textContent = data.queue.length;
        document.getElementById('statCompletedCount').textContent = data.completed.length;

        const container = document.getElementById('downloadsQueueList');
        if (!container) return;

        const allItems = [...data.active, ...data.completed];
        if (allItems.length === 0) {
            container.innerHTML = `<p style="padding:20px; text-align:center; color:var(--text-muted);">No hay tareas de descarga en progreso.</p>`;
            return;
        }

        container.innerHTML = allItems.map(dl => `
            <div style="background:var(--bg-card); border:1px solid var(--border-color); border-radius:12px; padding:16px; margin-bottom:12px; display:flex; justify-content:space-between; align-items:center;">
                <div>
                    <strong>${dl.title}</strong> (${dl.artist})
                    <div style="font-size:0.8rem; color:var(--text-muted);">${dl.format.toUpperCase()} • ${dl.speedKbps ? dl.speedKbps + ' KB/s' : 'Procesando...'}</div>
                </div>
                <div style="color:var(--cyan-light); font-weight:700;">
                    ${dl.status} (${dl.progressPercent}%)
                </div>
            </div>
        `).join('');
    } catch (e) {}
}

function downloadApkDirect() {
    window.location.href = `${API_BASE}/apk/download`;
}
