const SERVER_HOST = (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1')
    ? `http://${window.location.hostname}:8788` 
    : (window.location.origin.startsWith('http') && !window.location.origin.includes('file:') && window.location.hostname !== 'spotify-arkaios.vercel.app' ? window.location.origin : 'http://192.168.101.106:8788');
const API_BASE = `${SERVER_HOST}/api`;

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
    { id: 'cd_trckas_official', title: 'CD TRCKAS (28 Tracks Oficial)', artist: 'KLMR', cover: 'cd_cover.svg', tracksCount: 28, isCdTrckas: true },
    { id: 'al_01', title: 'DJ KLMR Vault (19,000+ Master Pistas)', artist: 'DJ KLMR', cover: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600', tracksCount: 19026 },
    { id: 'al_02', title: 'Krazy Rhythm & Urban Remixes', artist: 'DJ KLMR Extended', cover: 'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600', tracksCount: 420 },
    { id: 'al_03', title: 'VirtualDJ UltraReggaeton 2026', artist: 'DJ KLMR Master Edit', cover: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600', tracksCount: 185 },
    { id: 'al_04', title: 'Google Drive 5TB Cloud Vault', artist: 'ARKAIOS Sovereign Node', cover: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600', tracksCount: 5000 }
];

document.addEventListener('DOMContentLoaded', () => {
    checkDaemonStatus();
    loadHomeData();
    loadRadioStations();
    loadPublicInfractionLibrary();
    renderPlaylists();
    initCdTrckasModule();

    audioPlayer.addEventListener('timeupdate', updateProgress);
    audioPlayer.addEventListener('ended', onTrackEnded);

    audioPlayer.addEventListener('play', () => {
        isPlaying = true;
        updatePlayPauseButton();
    });
    audioPlayer.addEventListener('playing', () => {
        isPlaying = true;
        updatePlayPauseButton();
    });
    audioPlayer.addEventListener('pause', () => {
        isPlaying = false;
        updatePlayPauseButton();
    });
    audioPlayer.addEventListener('error', (e) => {
        const err = audioPlayer.error;
        console.warn('[AUDIO ERROR]', err ? `Code: ${err.code}, Msg: ${err.message}` : e, 'Current src:', audioPlayer.src);
        handleAudioPlaybackError();
    });

    // Initial default track setup
    setupInitialTrack();
});

function handleAudioPlaybackError() {
    const track = currentTrackList[currentTrackIndex];
    if (!track) return;

    const isCdTrckasAlbum = (track.album_id === 'cd_trckas_official') || 
                           (track.album && track.album.toLowerCase().includes('cd trckas'));
    const padNum = track.number ? String(track.number).padStart(2, '0') : null;

    if (isCdTrckasAlbum && padNum && (audioPlayer.src.startsWith('file://') || audioPlayer.src.includes('/local-sdcard/'))) {
        const cloudUrl = track.drive_file_id
            ? `https://drive.usercontent.google.com/download?id=${track.drive_file_id}&export=download`
            : `https://cd-trckas.vercel.app/plataforma/audio/${padNum}.mp3`;
        console.log('[FALLBACK] CD-TRCKAS jalando directamente del SSASS en Google Drive:', cloudUrl);
        audioPlayer.src = cloudUrl;
        audioPlayer.play().then(() => {
            isPlaying = true;
            updatePlayPauseButton();
            const trackKey = AudioPrecacheManager.getTrackKey(track);
            fetch(cloudUrl, { mode: 'cors' })
                .then(r => r.ok ? r.blob() : null)
                .then(b => b ? AudioPrecacheManager.saveBlob(trackKey, b) : null)
                .catch(() => {});
        }).catch(e => console.error('[FALLBACK] Error CD-TRCKAS remoto:', e));
    } else if (track.drive_file_id) {
        if (!audioPlayer.src.includes('spotify-arkaios.vercel.app/api/stream')) {
            const proxyUrl = `https://spotify-arkaios.vercel.app/api/stream?id=${track.drive_file_id}`;
            console.log('[FALLBACK] Intentando proxy serverless Vercel:', proxyUrl);
            audioPlayer.src = proxyUrl;
            audioPlayer.play().then(() => {
                isPlaying = true;
                updatePlayPauseButton();
            }).catch(e2 => console.error('[FALLBACK] Proxy Vercel también falló:', e2));
        }
    }
}

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
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 2000);
        const res = await fetch(`${API_BASE}/search`, { signal: controller.signal });
        clearTimeout(timeoutId);
        if (res.ok) {
            const data = await res.json();
            currentTrackList = data.results || [];
        }
    } catch (e) {
        // Demonio no disponible - se utilizará la biblioteca local
    }

    // Si el demonio local está en standby, poblar con las pistas de CD-TRCKAS
    if (!currentTrackList || currentTrackList.length === 0) {
        if (cdTrckasState && cdTrckasState.allAlbums && cdTrckasState.allAlbums.length > 0) {
            currentTrackList = cdTrckasState.allAlbums[0].tracks || [];
        }
    }
    renderHomeTracks(currentTrackList);
}

function renderAlbumsCarousel() {
    const container = document.getElementById('homeAlbumsCarousel');
    if (!container) return;
    container.innerHTML = featuredAlbums.map(al => `
        <div class="album-card" onclick="${al.isCdTrckas ? 'openCdTrckasFromHome()' : `searchByTerm('${al.artist}')`}">
            <img src="${al.cover || 'cd_cover.svg'}" alt="${al.title}" onerror="this.onerror=null; this.src='cd_cover.svg';">
            <h4>${al.title}</h4>
            <p>${al.artist} • ${al.tracksCount} Pistas</p>
        </div>
    `).join('');
}

function renderHomeTracks(tracks) {
    const container = document.getElementById('homeTracksList');
    if (!container) return;
    const trackItems = (tracks && tracks.length > 0) ? tracks : (cdTrckasState?.currentTracks || []);
    container.innerHTML = `<div class="tracks-grid">` + trackItems.map((t, idx) => `
        <div class="track-item-card">
            <img src="${t.cover || 'cd_cover.svg'}" alt="${t.title}" onerror="this.onerror=null; this.src='cd_cover.svg';">
            <h4>${t.title}</h4>
            <p>${t.artist} • <span style="color:var(--cyan-light);">${t.genre || 'Pista HQ'}</span></p>
            <div class="track-actions">
                <button class="btn-track-play" onclick="playTrackFromList(${idx})"><i class="fa-solid fa-play"></i> Escuchar</button>
                <button class="btn-track-download" onclick="requestDownload('${t.title}', '${t.artist}', '${t.url || ''}')"><i class="fa-solid fa-download"></i></button>
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

    resultsHeader.innerHTML = `<span><i class="fa-solid fa-spinner fa-spin"></i> Buscando "${query}" en tiempo real y catálogo maestro...</span>`;
    resultsGrid.innerHTML = '';

    let results = [];
    let sourceLabel = 'Catálogo Offline & CD-TRCKAS';

    // 1. Intento con demonio local si está en ejecución
    try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 2000);
        const res = await fetch(`${API_BASE}/search?q=${encodeURIComponent(query)}`, { signal: controller.signal });
        clearTimeout(timeoutId);
        if (res.ok) {
            const data = await res.json();
            if (data && data.results && data.results.length > 0) {
                results = data.results;
                sourceLabel = data.source || 'Demonio Local Arkaios';
            }
        }
    } catch (e) {
        // Demonio local en standby - proceder inmediatamente con búsqueda nativa
    }

    // 2. Si no hay respuesta del demonio local, buscar en los catálogos integrados
    if (results.length === 0) {
        const qLower = query.toLowerCase();

        // Buscar en álbumes y pistas de CD-TRCKAS
        if (cdTrckasState && cdTrckasState.allAlbums) {
            cdTrckasState.allAlbums.forEach(al => {
                (al.tracks || []).forEach(tr => {
                    const match = (tr.title && tr.title.toLowerCase().includes(qLower)) ||
                                  (tr.artist && tr.artist.toLowerCase().includes(qLower)) ||
                                  (tr.genre && tr.genre.toLowerCase().includes(qLower)) ||
                                  (al.title && al.title.toLowerCase().includes(qLower));
                    if (match) {
                        results.push({
                            title: tr.title,
                            artist: tr.artist || al.artist || 'DJ KLMR',
                            genre: tr.genre || 'CD-TRCKAS Master',
                            cover: tr.cover || 'cd_cover.svg',
                            url: tr.stream_url || tr.audio_url || tr.url || '',
                            streamUrl: tr.stream_url || tr.audio_url || tr.url || '',
                            format: tr.format || 'FLAC/MP3',
                            source: 'CD-TRCKAS'
                        });
                    }
                });
            });
        }

        // Buscar en Biblioteca Pública Infraction
        if (publicInfractionData && publicInfractionData.sampleTracks) {
            publicInfractionData.sampleTracks.forEach(tr => {
                const match = (tr.title && tr.title.toLowerCase().includes(qLower)) ||
                              (tr.artist && tr.artist.toLowerCase().includes(qLower)) ||
                              (tr.genre && tr.genre.toLowerCase().includes(qLower));
                if (match) {
                    results.push({
                        title: tr.title,
                        artist: tr.artist || 'Ivan Malkov (Infraction)',
                        genre: tr.genre || 'Royalty Free',
                        cover: 'album_default.svg',
                        url: tr.url || tr.streamUrl || '',
                        streamUrl: tr.url || tr.streamUrl || '',
                        format: 'MP3',
                        source: 'Infraction Library'
                    });
                }
            });
        }

        // 3. Si hay conexión a internet y pocos resultados, consultar motor en tiempo real
        if (results.length < 5 && navigator.onLine) {
            try {
                const controller = new AbortController();
                const timeoutId = setTimeout(() => controller.abort(), 3500);
                const itunesRes = await fetch(`https://itunes.apple.com/search?term=${encodeURIComponent(query)}&media=music&limit=20`, { signal: controller.signal });
                clearTimeout(timeoutId);
                if (itunesRes.ok) {
                    const itunesData = await itunesRes.json();
                    (itunesData.results || []).forEach(item => {
                        results.push({
                            title: item.trackName || item.collectionName,
                            artist: item.artistName,
                            genre: item.primaryGenreName || 'Mundial',
                            cover: item.artworkUrl100 ? item.artworkUrl100.replace('100x100bb', '600x600bb') : 'album_default.svg',
                            url: item.previewUrl,
                            streamUrl: item.previewUrl,
                            format: 'HQ Stream',
                            source: 'En Tiempo Real'
                        });
                    });
                    if (results.length > 0) sourceLabel = 'En Tiempo Real & Catálogo CD-TRCKAS';
                }
            } catch (itErr) {
                // Modo sin conexión silencioso
            }
        }
    }

    // Filtrado por chip
    if (currentSourceFilter === 'YT') {
        const f = results.filter(r => (r.genre || '').includes('YouTube') || (r.source || '').includes('YouTube'));
        if (f.length > 0) results = f;
    } else if (currentSourceFilter === 'TIDAL') {
        const f = results.filter(r => (r.genre || '').includes('TIDAL') || (r.source || '').includes('TIDAL'));
        if (f.length > 0) results = f;
    } else if (currentSourceFilter === 'AUDIUS') {
        const f = results.filter(r => (r.genre || '').includes('Audius') || (r.source || '').includes('CD-TRCKAS'));
        if (f.length > 0) results = f;
    }

    currentTrackList = results;

    if (results.length === 0) {
        resultsHeader.innerHTML = `<span>Búsqueda finalizada para "<strong>${query}</strong>":</span>`;
        resultsGrid.innerHTML = `
            <div style="grid-column:1/-1; padding:40px; text-align:center; color:var(--text-muted);">
                <i class="fa-solid fa-compact-disc" style="font-size:2.5rem; color:var(--cyan-light); margin-bottom:14px; display:block;"></i>
                <h4 style="color:#f8fafc; margin-bottom:8px;">No se encontraron canciones</h4>
                <p style="font-size:0.9rem;">Prueba buscando "KLMR", "Baila", o revisa los 28 temas en la pestaña CD-TRCKAS.</p>
            </div>
        `;
        return;
    }

    resultsHeader.innerHTML = `<span>Se encontraron <strong>${results.length} resultados</strong> para "${query}" (${sourceLabel}):</span>`;

    resultsGrid.innerHTML = results.map((t, idx) => `
        <div class="track-item-card">
            <img src="${t.cover || 'cd_cover.svg'}" alt="${t.title}" onerror="this.onerror=null; this.src='cd_cover.svg';">
            <h4>${t.title}</h4>
            <p>${t.artist}</p>
            <span style="font-size:0.75rem; color:var(--cyan-light); font-weight:700;">${t.genre || 'Pista Master'} • ${t.format || 'MP3'}</span>
            <div class="track-actions">
                <button class="btn-track-play" onclick="playTrackFromList(${idx})"><i class="fa-solid fa-play"></i> Escuchar</button>
                <button class="btn-track-download" onclick="requestDownload('${t.title}', '${t.artist}', '${t.url || ''}')"><i class="fa-solid fa-download"></i></button>
            </div>
        </div>
    `).join('');
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

// =========================================================================
// MOTOR DE PRE-CACHE PREDICTIVO INTELIGENTE Y REPRODUCCIÓN INSTANTÁNEA (0 ms)
// =========================================================================
const AudioPrecacheManager = (function() {
    const DB_NAME = 'ArkaiosAudioPrecacheDB';
    const DB_VERSION = 1;
    const STORE_NAME = 'audio_blobs';
    let db = null;
    const memCache = new Map();
    let secondaryPreloader = null;
    let prefetchingId = null;
    let precacheTimer = null;

    function initPreloader() {
        if (!secondaryPreloader && typeof Audio !== 'undefined') {
            try {
                secondaryPreloader = new Audio();
                secondaryPreloader.preload = 'auto';
                secondaryPreloader.muted = true;
            } catch (e) {}
        }
    }

    function openDB() {
        if (db) return Promise.resolve(db);
        return new Promise((resolve) => {
            if (typeof window === 'undefined' || !window.indexedDB) {
                return resolve(null);
            }
            try {
                const req = indexedDB.open(DB_NAME, DB_VERSION);
                req.onupgradeneeded = (e) => {
                    const d = e.target.result;
                    if (!d.objectStoreNames.contains(STORE_NAME)) {
                        d.createObjectStore(STORE_NAME, { keyPath: 'id' });
                    }
                };
                req.onsuccess = (e) => {
                    db = e.target.result;
                    resolve(db);
                };
                req.onerror = () => resolve(null);
            } catch (err) {
                resolve(null);
            }
        });
    }

    async function getCachedUrl(id) {
        if (!id) return null;
        if (memCache.has(id)) {
            return memCache.get(id);
        }
        try {
            const database = await openDB();
            if (!database) return null;
            return new Promise((resolve) => {
                const tx = database.transaction(STORE_NAME, 'readonly');
                const store = tx.objectStore(STORE_NAME);
                const req = store.get(id);
                req.onsuccess = () => {
                    if (req.result && req.result.blob) {
                        const objUrl = URL.createObjectURL(req.result.blob);
                        memCache.set(id, objUrl);
                        console.log('[PRECACHE] ⚡ Hit en caché local instantáneo (0ms) para:', id);
                        resolve(objUrl);
                    } else {
                        resolve(null);
                    }
                };
                req.onerror = () => resolve(null);
            });
        } catch (e) {
            return null;
        }
    }

    async function saveBlob(id, blob) {
        if (!id || !blob) return null;
        const objUrl = URL.createObjectURL(blob);
        memCache.set(id, objUrl);
        try {
            const database = await openDB();
            if (database) {
                const tx = database.transaction(STORE_NAME, 'readwrite');
                const store = tx.objectStore(STORE_NAME);
                store.put({ id, blob, size: blob.size, timestamp: Date.now() });
            }
        } catch (e) {}
        return objUrl;
    }

    function getDirectStreamUrl(track) {
        if (!track) return '';
        const isCdTrckas = (track.album_id === 'cd_trckas_official') || 
                           (track.album && track.album.toLowerCase().includes('cd trckas'));
        const padNum = track.number ? String(track.number).padStart(2, '0') : null;
        if (track.drive_file_id) {
            return `https://drive.usercontent.google.com/download?id=${track.drive_file_id}&export=download`;
        }
        if (isCdTrckas && padNum) {
            return `https://cd-trckas.vercel.app/plataforma/audio/${padNum}.mp3`;
        }
        return track.streamUrl || track.stream_url || track.url || '';
    }

    function getTrackKey(track) {
        if (!track) return null;
        if (track.drive_file_id) return 'drive_' + track.drive_file_id;
        if (track.id) return 'track_' + track.id;
        const padNum = track.number ? String(track.number).padStart(2, '0') : null;
        const isCdTrckas = (track.album_id === 'cd_trckas_official') || 
                           (track.album && track.album.toLowerCase().includes('cd trckas'));
        if (isCdTrckas && padNum) return 'cd_trckas_' + padNum;
        return track.title ? 'title_' + track.title.toLowerCase().replace(/\s+/g, '_') : null;
    }

    async function precacheTrack(track) {
        if (!track) return;
        initPreloader();
        const key = getTrackKey(track);
        if (!key || memCache.has(key)) return;
        const cached = await getCachedUrl(key);
        if (cached) return;

        const streamUrl = getDirectStreamUrl(track);
        if (!streamUrl || streamUrl.startsWith('file://')) return;

        if (prefetchingId === key) return;
        prefetchingId = key;

        if (secondaryPreloader) {
            try {
                secondaryPreloader.src = streamUrl;
                secondaryPreloader.load();
            } catch (e) {}
        }

        try {
            console.log('[PRECACHE] 🚀 Precargando silenciosamente pista siguiente:', track.title);
            const res = await fetch(streamUrl, { mode: 'cors' });
            if (res.ok) {
                const blob = await res.blob();
                await saveBlob(key, blob);
                console.log('[PRECACHE] ✔ Pista precacheada lista para reproducir al toque (0ms):', track.title);
            }
        } catch (e) {
            console.log('[PRECACHE] Buffer secundario activo para:', track.title);
        } finally {
            if (prefetchingId === key) prefetchingId = null;
        }
    }

    function scheduleNextTrackPrecache(currentIndex, trackList) {
        if (precacheTimer) clearTimeout(precacheTimer);
        if (!trackList || trackList.length <= 1) return;
        precacheTimer = setTimeout(() => {
            const nextIdx = (currentIndex + 1) % trackList.length;
            const nextTrack = trackList[nextIdx];
            if (nextTrack) {
                precacheTrack(nextTrack);
            }
        }, 1800);
    }

    return {
        getCachedUrl,
        saveBlob,
        getDirectStreamUrl,
        getTrackKey,
        precacheTrack,
        scheduleNextTrackPrecache
    };
})();

async function loadTrackToPlayer(track, autoPlay = true) {
    if (!track) return;
    document.getElementById('playerTitle').textContent = track.title || 'Desconocido';
    document.getElementById('playerArtist').textContent = track.artist || 'KLMR';
    const coverEl = document.getElementById('playerCover');
    if (coverEl) {
        coverEl.onerror = function() { this.src = 'cd_cover.svg'; };
        coverEl.src = track.cover || 'cd_cover.svg';
    }

    let streamSrc = '';
    const trackKey = AudioPrecacheManager.getTrackKey(track);
    const cachedUrl = await AudioPrecacheManager.getCachedUrl(trackKey);

    const isCdTrckasAlbum = (track.album_id === 'cd_trckas_official') || 
                           (track.album && track.album.toLowerCase().includes('cd trckas'));
    const padNum = track.number ? String(track.number).padStart(2, '0') : null;

    if (cachedUrl) {
        streamSrc = cachedUrl;
        console.log('[PLAYER] ⚡ Reproducción instantánea (0ms) desde caché:', track.title);
    } else if (isCdTrckasAlbum && padNum && parseInt(padNum, 10) >= 1 && parseInt(padNum, 10) <= 28) {
        streamSrc = window.location.protocol.startsWith('http')
            ? `https://spotify-arkaios.vercel.app/local-sdcard/Music/CD-TRCKAS/${padNum}.mp3`
            : `file:///sdcard/Music/CD-TRCKAS/${padNum}.mp3`;
    } else if (track.drive_file_id) {
        streamSrc = `https://drive.usercontent.google.com/download?id=${track.drive_file_id}&export=download`;
    } else if (track.streamUrl) {
        streamSrc = track.streamUrl;
    } else if (track.stream_url) {
        streamSrc = track.stream_url;
    } else if (track.proxy_stream_url) {
        streamSrc = track.proxy_stream_url;
    } else if (track.static_stream_url) {
        streamSrc = track.static_stream_url;
    } else {
        streamSrc = track.url || track.audio_url || '';
    }

    if (streamSrc && streamSrc.includes('drive.google.com/uc?export=download&id=')) {
        const fileId = streamSrc.split('id=')[1]?.split('&')[0];
        if (fileId) {
            streamSrc = `https://drive.usercontent.google.com/download?id=${fileId}&export=download`;
        }
    }

    if (streamSrc && streamSrc.startsWith('/') && !streamSrc.startsWith('//')) {
        streamSrc = `${SERVER_HOST}${streamSrc}`;
    }

    console.log('[PLAYER] Cargando pista:', track.title, 'Álbum:', track.album, 'URL:', streamSrc);

    audioPlayer.pause();
    audioPlayer.src = streamSrc;

    if (autoPlay) {
        const p = audioPlayer.play();
        if (p !== undefined) {
            p.then(() => {
                isPlaying = true;
                updatePlayPauseButton();
                console.log('[PLAYER] Reproduciendo:', track.title);
                if (!cachedUrl && streamSrc.startsWith('http')) {
                    fetch(streamSrc, { mode: 'cors' })
                        .then(r => r.ok ? r.blob() : null)
                        .then(b => b ? AudioPrecacheManager.saveBlob(trackKey, b) : null)
                        .catch(() => {});
                }
                AudioPrecacheManager.scheduleNextTrackPrecache(currentTrackIndex, currentTrackList);
            }).catch(err => {
                console.warn('[PLAYER] Error al iniciar play:', err.name, err.message);
                handleAudioPlaybackError();
            });
        }
    } else {
        AudioPrecacheManager.scheduleNextTrackPrecache(currentTrackIndex, currentTrackList);
    }
}

function playHeroTrack() {
    playCdTrckasOfficialAlbum();
}

function playRadioStream(streamUrl, name, genre, cover) {
    document.getElementById('playerTitle').textContent = name;
    document.getElementById('playerArtist').textContent = `📻 Radio En Vivo (${genre})`;
    const radCoverEl = document.getElementById('playerCover');
    if (radCoverEl) {
        radCoverEl.onerror = function() { this.src = 'album_default.svg'; };
        radCoverEl.src = cover || 'album_default.svg';
    }
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
    const cur = audioPlayer.currentTime || 0;
    const dur = audioPlayer.duration;
    document.getElementById('currentTime').textContent = formatTime(cur);
    if (dur && !isNaN(dur) && isFinite(dur)) {
        const percent = (cur / dur) * 100;
        document.getElementById('progressBar').value = percent;
        document.getElementById('totalDuration').textContent = formatTime(dur);
    }
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

// =========================================================================
// BIBLIOTECA PÚBLICA INFRACTION — MÚSICA DEL ARTISTA ORIGINAL (IVAN MALKOV)
// =========================================================================
let publicInfractionData = { playlists: [], sampleTracks: [] };
let publicInfractionFilter = 'ALL';
let infrFavorites = JSON.parse(localStorage.getItem('arkaios_infr_favorites') || '[]');

async function loadPublicInfractionLibrary() {
    try {
        let data = window.INFRACTION_PUBLIC_PLAYLISTS || null;
        if (!data) {
            try {
                const res = await fetch('infraction_public_playlists.json');
                if (res.ok) data = await res.json();
            } catch (e) {
                // Intento secundario desde API
            }
        }

        if (!data) {
            try {
                const res = await fetch(`${API_BASE}/infraction/public-catalog`);
                if (res.ok) data = await res.json();
            } catch (e) {
                console.warn('No se pudo conectar a API de catálogo Infraction');
            }
        }

        if (!data) return;

        publicInfractionData = data;

        const totalPlEl = document.getElementById('totalPlaylistsCount');
        const totalTrEl = document.getElementById('publicTracksCount');
        const totalFavEl = document.getElementById('favoritesCount');

        if (totalPlEl) totalPlEl.textContent = (data.playlists || []).length;
        if (totalTrEl) totalTrEl.textContent = (data.sampleTracks || []).length;
        if (totalFavEl) totalFavEl.textContent = infrFavorites.length;

        renderPublicTracks();
        renderOfficialPlaylists();
    } catch (err) {
        console.error('Error cargando biblioteca pública Infraction:', err);
    }
}

function renderPublicTracks(tracksToRender = null) {
    const container = document.getElementById('publicTracksGrid');
    if (!container) return;

    let tracks = tracksToRender || publicInfractionData.sampleTracks || [];

    if (publicInfractionFilter === 'FAVORITES') {
        tracks = (publicInfractionData.sampleTracks || []).filter(t => infrFavorites.includes(t.videoId));
    }

    const query = (document.getElementById('infrSearchInput')?.value || '').trim().toLowerCase();
    if (query) {
        tracks = tracks.filter(t => 
            (t.title && t.title.toLowerCase().includes(query)) || 
            (t.playlistTitle && t.playlistTitle.toLowerCase().includes(query)) ||
            (t.album && t.album.toLowerCase().includes(query)) ||
            (t.genre && t.genre.toLowerCase().includes(query))
        );
    }

    if (tracks.length === 0) {
        container.innerHTML = `<p style="grid-column:1/-1; padding:30px; text-align:center; color:var(--text-muted);">No se encontraron canciones en esta vista.</p>`;
        return;
    }

    container.innerHTML = tracks.map(t => {
        const isFav = infrFavorites.includes(t.videoId);
        const thumb = t.coverUrl || t.thumbnailUrl || `https://i.ytimg.com/vi/${t.videoId}/hqdefault.jpg`;
        const vUrl = t.originalUrl || t.videoUrl || `https://www.youtube.com/watch?v=${t.videoId}`;
        const plTitle = t.album || t.playlistTitle || 'Infraction Music';
        return `
        <div class="track-item-card">
            <div style="position:relative;">
                <img src="${thumb}" alt="${t.title}" loading="lazy">
                <span style="position:absolute; bottom:8px; right:8px; background:rgba(0,0,0,0.8); color:#fff; font-size:0.75rem; padding:2px 6px; border-radius:4px; font-weight:700;">${t.duration}</span>
            </div>
            <h4>${t.title}</h4>
            <p>${t.artist}</p>
            <div style="display:flex; justify-content:space-between; align-items:center; margin:6px 0 10px;">
                <span style="font-size:0.75rem; color:var(--cyan-light); font-weight:700;">${t.genre || 'Royalty-Free'}</span>
                <span style="font-size:0.7rem; color:var(--text-muted);">${plTitle}</span>
            </div>
            <div class="track-actions">
                <button class="btn-track-play" onclick="playOfficialYouTubeTrack('${t.videoId}', '${t.title.replace(/'/g, "\\'")}', '${vUrl}')">
                    <i class="fa-brands fa-youtube" style="color:#ef4444;"></i> Reproducir Oficial
                </button>
                <button class="btn-track-download ${isFav ? 'active' : ''}" style="color:${isFav ? '#ef4444' : 'var(--text-muted)'};" title="Añadir a Favoritos" onclick="toggleFavorite('${t.videoId}')">
                    <i class="${isFav ? 'fa-solid' : 'fa-regular'} fa-heart"></i>
                </button>
                <button class="btn-track-download" title="Añadir a Playlist ARKAIOS" onclick="showAddToPlaylistModal('${t.id}')">
                    <i class="fa-solid fa-plus"></i>
                </button>
            </div>
            <div style="font-size:0.7rem; color:var(--text-muted); margin-top:8px; display:flex; align-items:center; gap:6px;">
                <i class="fa-solid fa-shield-halved" style="color:var(--green-accent);"></i> Stream Oficial • Descarga deshabilitada
            </div>
        </div>
        `;
    }).join('');
}

function renderOfficialPlaylists() {
    const container = document.getElementById('officialPlaylistsGrid');
    if (!container) return;

    let playlists = publicInfractionData.playlists || [];
    const query = (document.getElementById('infrSearchInput')?.value || '').trim().toLowerCase();
    if (query) {
        playlists = playlists.filter(p => 
            p.title.toLowerCase().includes(query) || 
            (p.genre && p.genre.toLowerCase().includes(query))
        );
    }

    container.innerHTML = playlists.map((pl, idx) => `
        <div class="playlist-official-card">
            <div>
                <div class="playlist-card-title">${pl.title}</div>
                <div class="playlist-card-genre"><i class="fa-solid fa-tag"></i> ${pl.genre}</div>
            </div>
            <div style="display:flex; flex-direction:column; gap:8px; margin-top:12px;">
                <button class="btn-play-playlist-yt" onclick="playOfficialYouTubePlaylist('${pl.id}', '${pl.title.replace(/'/g, "\\'")}', '${pl.url}')">
                    <i class="fa-brands fa-youtube"></i> Reproducir en IFrame
                </button>
                <a href="${pl.url}" target="_blank" rel="noopener noreferrer" style="font-size:0.75rem; color:var(--text-muted); text-align:center; text-decoration:none;">
                    <i class="fa-solid fa-arrow-up-right-from-square"></i> Abrir en YouTube.com
                </a>
            </div>
        </div>
    `).join('');
}

function setPublicFilter(mode, btnEl) {
    publicInfractionFilter = mode;
    document.querySelectorAll('.infr-view-modes .chip-filter').forEach(c => c.classList.remove('active'));
    if (btnEl) btnEl.classList.add('active');

    const tracksSection = document.getElementById('infrTracksSection');
    const playlistsSection = document.getElementById('infrPlaylistsSection');
    const titleEl = document.getElementById('infrSectionTitle');

    if (mode === 'PLAYLISTS') {
        if (tracksSection) tracksSection.style.display = 'none';
        if (playlistsSection) playlistsSection.style.display = 'block';
    } else {
        if (tracksSection) tracksSection.style.display = 'block';
        if (playlistsSection) playlistsSection.style.display = 'none';

        if (titleEl) {
            titleEl.innerHTML = mode === 'FAVORITES' 
                ? '<i class="fa-solid fa-heart" style="color:#ef4444;"></i> Tus Canciones Favoritas de Infraction'
                : '<i class="fa-solid fa-compact-disc"></i> Canciones y Composiciones Originales';
        }
        renderPublicTracks();
    }
}

function filterPublicInfraction() {
    if (publicInfractionFilter === 'PLAYLISTS') {
        renderOfficialPlaylists();
    } else {
        renderPublicTracks();
    }
}

function toggleFavorite(videoId) {
    const idx = infrFavorites.indexOf(videoId);
    if (idx === -1) {
        infrFavorites.push(videoId);
    } else {
        infrFavorites.splice(idx, 1);
    }
    localStorage.setItem('arkaios_infr_favorites', JSON.stringify(infrFavorites));
    
    const favCountEl = document.getElementById('favoritesCount');
    if (favCountEl) favCountEl.textContent = infrFavorites.length;

    renderPublicTracks();
}

// Reproductor Oficial YouTube IFrame
function playOfficialYouTubeTrack(videoId, title, videoUrl) {
    // Pausar audio local si estaba reproduciendo
    if (audioPlayer && isPlaying) {
        audioPlayer.pause();
        isPlaying = false;
        updatePlayPauseButton();
    }

    const card = document.getElementById('embeddedPlayerCard');
    const iframe = document.getElementById('youtubeIframePlayer');
    const titleEl = document.getElementById('embedPlayingTitle');
    const extLink = document.getElementById('embedExternalLink');

    if (titleEl) titleEl.textContent = `${title} — Infraction (Ivan Malkov)`;
    if (extLink) extLink.href = videoUrl || `https://www.youtube.com/watch?v=${videoId}`;

    if (iframe) {
        const embedOrigin = (window.location.origin && window.location.origin.startsWith('http'))
            ? window.location.origin
            : 'https://spotify-arkaios.vercel.app';
        iframe.src = `https://www.youtube.com/embed/${videoId}?autoplay=1&playsinline=1&rel=0&modestbranding=1&enablejsapi=1&origin=${encodeURIComponent(embedOrigin)}`;
    }

    if (card) {
        card.style.display = 'block';
        card.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
}

function playOfficialYouTubePlaylist(listId, title, url) {
    if (audioPlayer && isPlaying) {
        audioPlayer.pause();
        isPlaying = false;
        updatePlayPauseButton();
    }

    const card = document.getElementById('embeddedPlayerCard');
    const iframe = document.getElementById('youtubeIframePlayer');
    const titleEl = document.getElementById('embedPlayingTitle');
    const extLink = document.getElementById('embedExternalLink');

    if (titleEl) titleEl.textContent = `Playlist: ${title} — Infraction (Ivan Malkov)`;
    if (extLink) extLink.href = url || `https://www.youtube.com/playlist?list=${listId}`;

    if (iframe) {
        const embedOrigin = (window.location.origin && window.location.origin.startsWith('http'))
            ? window.location.origin
            : 'https://spotify-arkaios.vercel.app';
        iframe.src = `https://www.youtube.com/embed/videoseries?list=${listId}&autoplay=1&playsinline=1&rel=0&modestbranding=1&enablejsapi=1&origin=${encodeURIComponent(embedOrigin)}`;
    }

    if (card) {
        card.style.display = 'block';
        card.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
}

function closeEmbeddedPlayer() {
    const card = document.getElementById('embeddedPlayerCard');
    const iframe = document.getElementById('youtubeIframePlayer');
    if (iframe) iframe.src = '';
    if (card) card.style.display = 'none';
}

// ==========================================
// GESTOR DE PLAYLISTS (CREACIÓN & REPRODUCCIÓN)
// ==========================================
function renderPlaylists() {
    const container = document.getElementById('playlistsGrid');
    if (!container) return;

    if (customPlaylists.length === 0) {
        container.innerHTML = `<p style="grid-column:1/-1; padding:20px; text-align:center; color:var(--text-muted);">Aún no has creado playlists personalizadas. Pulsa "Nueva Playlist".</p>`;
        return;
    }

    container.innerHTML = customPlaylists.map((pl, idx) => `
        <div class="track-item-card" style="cursor:pointer;" onclick="playPlaylist(${idx})">
            <img src="${pl.cover || 'cd_cover.svg'}" alt="${pl.name}" onerror="this.onerror=null; this.src='cd_cover.svg';">
            <h4>${pl.name}</h4>
            <p>${(pl.tracks || []).length} pistas • Playlist Personalizada</p>
            <div class="track-actions" style="margin-top:8px;">
                <button class="btn-track-play" onclick="event.stopPropagation(); playPlaylist(${idx})"><i class="fa-solid fa-play"></i> Reproducir</button>
                <button class="btn-track-download" title="Eliminar" onclick="event.stopPropagation(); deletePlaylist(${idx})"><i class="fa-solid fa-trash"></i></button>
            </div>
        </div>
    `).join('');
}

function showCreatePlaylistModal() {
    const name = prompt('Ingresa el nombre de tu nueva playlist:', 'Mi Playlist Favorita');
    if (!name || !name.trim()) return;

    const newPlaylist = {
        id: 'pl_' + Date.now(),
        name: name.trim(),
        cover: 'cd_cover.svg',
        tracks: []
    };

    customPlaylists.push(newPlaylist);
    localStorage.setItem('arkaios_playlists', JSON.stringify(customPlaylists));
    renderPlaylists();
    alert(`✔ Playlist "${name.trim()}" creada con éxito.`);
    switchTab('library');
    switchLibrarySubtab('playlists');
}

function showAddToPlaylistModal(trackId) {
    if (customPlaylists.length === 0) {
        if (confirm('No tienes ninguna playlist creada. ¿Deseas crear una ahora?')) {
            showCreatePlaylistModal();
        }
        return;
    }

    // Buscar track en catálogo público Infraction o lista actual
    const track = (publicInfractionData.sampleTracks || []).find(t => t.id === trackId) || currentTrackList.find(t => t.id === trackId);
    if (!track) return;

    const options = customPlaylists.map((pl, i) => `${i + 1}. ${pl.name} (${(pl.tracks || []).length} pistas)`).join('\n');
    const choice = prompt(`Selecciona el número de la playlist donde añadir "${track.title}":\n\n${options}`);
    const selectedIdx = parseInt(choice, 10) - 1;

    if (isNaN(selectedIdx) || selectedIdx < 0 || selectedIdx >= customPlaylists.length) {
        return;
    }

    const targetPlaylist = customPlaylists[selectedIdx];
    if (!targetPlaylist.tracks) targetPlaylist.tracks = [];
    
    // Evitar duplicar en la misma playlist
    if (targetPlaylist.tracks.some(t => t.id === track.id)) {
        alert(`La pista ya se encuentra en "${targetPlaylist.name}".`);
        return;
    }

    targetPlaylist.tracks.push(track);
    localStorage.setItem('arkaios_playlists', JSON.stringify(customPlaylists));
    renderPlaylists();
    alert(`✔ Pista "${track.title}" añadida a la playlist "${targetPlaylist.name}".`);
}

function deletePlaylist(index) {
    if (confirm(`¿Seguro que deseas eliminar la playlist "${customPlaylists[index].name}"?`)) {
        customPlaylists.splice(index, 1);
        localStorage.setItem('arkaios_playlists', JSON.stringify(customPlaylists));
        renderPlaylists();
    }
}

function playPlaylist(index) {
    const pl = customPlaylists[index];
    if (!pl || !pl.tracks || pl.tracks.length === 0) {
        alert('Esta playlist no tiene canciones aún.');
        return;
    }
    const firstTrack = pl.tracks[0];
    if (firstTrack.playbackMode === 'YOUTUBE_OFFICIAL_EMBED') {
        playOfficialYouTubeTrack(firstTrack.videoId, firstTrack.title, firstTrack.videoUrl);
    } else {
        currentTrackList = pl.tracks;
        currentTrackIndex = 0;
        playTrackFromList(0);
    }
}


/* ==============================================================================
   CD-TRCKAS & SSASS MUSICAL DISCOTECA MODULE
   ============================================================================== */
let cdTrckasState = {
    catalog: null,
    allAlbums: [],
    selectedAlbumId: 'cd_trckas_official',
    currentTracks: [],
    filteredTracks: [],
    activeFilter: 'all',
    isLoading: false
};

async function initCdTrckasModule() {
    await loadCdTrckasCatalog();
    if (cdTrckasState.allAlbums && cdTrckasState.allAlbums.length > 0) {
        const official = cdTrckasState.allAlbums.find(a => a.id === 'cd_trckas_official') || cdTrckasState.allAlbums[0];
        if (official && official.tracks && official.tracks.length > 0) {
            currentTrackList = official.tracks;
            currentTrackIndex = 0;
            loadTrackToPlayer(official.tracks[0], false);
        }
    }
    refreshCdTrckasFromCloud(false).catch(err => console.warn('Cloud sync background notice:', err));
}

async function loadCdTrckasCatalog() {
    try {
        let catalogData = window.CD_TRCKAS_CATALOG_DATA || null;
        if (!catalogData) {
            try {
                const res = await fetch('cd_trckas_catalog.json');
                if (res.ok) catalogData = await res.json();
            } catch (e) {
                console.warn('Carga local cd_trckas_catalog.json fallback:', e);
            }
        }

        if (!catalogData) {
            try {
                const resRemote = await fetch('https://cd-trckas.vercel.app/catalog_manifest.json');
                if (resRemote.ok) catalogData = await resRemote.json();
            } catch (e) {
                console.warn('Carga remota cd-trckas fallback:', e);
            }
        }

        if (catalogData) {
            processCdTrckasData(catalogData);
        }
    } catch (err) {
        console.error('Error cargando catálogo CD-TRCKAS:', err);
    }
}

function processCdTrckasData(catalogData) {
    cdTrckasState.catalog = catalogData;
    const albumsList = [];

    (catalogData.labels || []).forEach(label => {
        (label.albums || []).forEach(album => {
            const isOfficial = album.id === 'cd_trckas_official';
            const isSsass = album.id.startsWith('ssass_') || (label.id && label.id.includes('ssass')) || (album.badge && album.badge.includes('SSASS')) || !isOfficial;
            albumsList.push({
                id: album.id,
                labelId: label.id,
                labelName: label.name,
                title: album.title,
                artist: album.artist || 'KLMR / SSASS Musical Vault',
                year: album.year || 2025,
                cover: album.cover || album.cover_url || 'cd_cover.svg',
                badge: album.badge || (isOfficial ? '28 Tracks • Master HQ' : 'Bóveda SSASS'),
                category: isOfficial ? 'official' : (isSsass ? 'ssass' : 'community'),
                totalTracks: (album.tracks || []).length,
                tracks: (album.tracks || []).map((t, idx) => ({
                    id: t.id || idx + 1,
                    album_id: album.id,
                    number: t.number || String(idx + 1).padStart(2, '0'),
                    title: t.title,
                    artist: t.artist || album.artist || 'KLMR / SSASS',
                    album: album.title,
                    genre: t.genre || 'Electrónica / House',
                    bpm: t.bpm || 124,
                    duration: t.duration || '03:30',
                    drive_file_id: t.drive_file_id || null,
                    streamUrl: t.drive_file_id ? `https://drive.usercontent.google.com/download?id=${t.drive_file_id}&export=download` : (t.stream_url || t.streamUrl || t.audio_url || ''),
                    proxy_stream_url: t.drive_file_id ? `https://spotify-arkaios.vercel.app/api/stream?id=${t.drive_file_id}` : (t.proxy_stream_url || ''),
                    static_stream_url: t.static_stream_url || '',
                    cover: album.cover || 'cd_cover.svg'
                }))
            });
        });
    });

    cdTrckasState.allAlbums = albumsList;
    updateCdTrckasStats();
    renderCdTrckasAlbums();

    if (albumsList.length > 0) {
        const found = albumsList.find(a => a.id === cdTrckasState.selectedAlbumId) || albumsList[0];
        selectCdAlbum(found.id);
    }
}

function updateCdTrckasStats() {
    const totalAlbums = cdTrckasState.allAlbums.length;
    let totalTracks = 0;
    let communityCount = 0;

    cdTrckasState.allAlbums.forEach(a => {
        totalTracks += a.totalTracks;
        if (a.category === 'community') communityCount++;
    });

    const elTracks = document.getElementById('cdStatTracks');
    const elAlbums = document.getElementById('cdStatAlbums');
    const elCommunity = document.getElementById('cdStatCommunity');

    if (elTracks) elTracks.textContent = totalTracks;
    if (elAlbums) elAlbums.textContent = totalAlbums;
    if (elCommunity) elCommunity.textContent = communityCount > 0 ? `${communityCount} Nuevos` : 'Sincronizado';
}

function filterCdAlbumGrid(query) {
    cdTrckasState.albumSearchQuery = (query || '').trim();
    renderCdTrckasAlbums();
}

function filterCdTrckasAlbums(filterType) {
    cdTrckasState.activeFilter = filterType;
    ['cdPillAll', 'cdPillOfficial', 'cdPillSsass2025', 'cdPillSsass', 'cdPillCommunity'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.classList.remove('active');
    });

    const pillMap = {
        'all': 'cdPillAll',
        'official': 'cdPillOfficial',
        'ssass_2025': 'cdPillSsass2025',
        'ssass': 'cdPillSsass',
        'community': 'cdPillCommunity'
    };
    const activePill = document.getElementById(pillMap[filterType]);
    if (activePill) activePill.classList.add('active');

    renderCdTrckasAlbums();
}

function renderCdTrckasAlbums() {
    const grid = document.getElementById('cdtrckasAlbumsGrid');
    if (!grid) return;

    let filtered = cdTrckasState.allAlbums;
    if (cdTrckasState.activeFilter === 'official') {
        filtered = filtered.filter(a => a.category === 'official');
    } else if (cdTrckasState.activeFilter === 'ssass_2025') {
        filtered = filtered.filter(a => a.labelId === 'label_ssass_2025' || (a.title && a.title.includes('2025')));
    } else if (cdTrckasState.activeFilter === 'ssass') {
        filtered = filtered.filter(a => a.category === 'ssass');
    } else if (cdTrckasState.activeFilter === 'community') {
        filtered = filtered.filter(a => a.category === 'community');
    }

    if (cdTrckasState.albumSearchQuery) {
        const q = cdTrckasState.albumSearchQuery.toLowerCase();
        filtered = filtered.filter(a => 
            (a.title && a.title.toLowerCase().includes(q)) || 
            (a.artist && a.artist.toLowerCase().includes(q)) ||
            (a.badge && a.badge.toLowerCase().includes(q))
        );
    }

    if (filtered.length === 0) {
        grid.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; padding: 30px; color: #94a3b8;">
                <i class="fa-solid fa-compact-disc" style="font-size: 2rem; margin-bottom: 10px; color: #06b6d4;"></i>
                <p>No se encontraron álbumes con ese criterio.</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = filtered.map(album => {
        const isSelected = album.id === cdTrckasState.selectedAlbumId;
        return `
            <div class="cd-album-card ${isSelected ? 'active' : ''}" onclick="selectCdAlbum('${album.id}')">
                <div class="cd-album-cover-box">
                    <img src="${album.cover || 'cd_cover.svg'}" alt="${album.title}" onerror="this.onerror=null; this.src='cd_cover.svg';">
                    <span class="cd-album-badge-floating">${album.badge || 'ÁLBUM'}</span>
                    <div class="cd-album-play-overlay">
                        <button onclick="event.stopPropagation(); playCdTrckasAlbumDirect('${album.id}')" title="Reproducir álbum">
                            <i class="fa-solid fa-play"></i>
                        </button>
                    </div>
                </div>
                <div class="cd-album-info">
                    <h4 title="${album.title}">${album.title}</h4>
                    <p>${album.artist}</p>
                    <span class="cd-album-meta-tag">${album.totalTracks} canciones • ${album.year}</span>
                </div>
            </div>
        `;
    }).join('');
}

function selectCdAlbum(albumId, autoScroll = true) {
    cdTrckasState.selectedAlbumId = albumId;
    const album = cdTrckasState.allAlbums.find(a => a.id === albumId);
    if (!album) return;

    cdTrckasState.currentTracks = album.tracks || [];
    cdTrckasState.filteredTracks = [...cdTrckasState.currentTracks];

    const elCover = document.getElementById('cdCurrentAlbumCover');
    const elTitle = document.getElementById('cdCurrentAlbumTitle');
    const elMeta = document.getElementById('cdCurrentAlbumMeta');
    const elBadge = document.getElementById('cdCurrentAlbumBadge');
    const elSearch = document.getElementById('cdTrackSearchInput');

    if (elCover) {
        elCover.onerror = function() { this.src = 'cd_cover.svg'; };
        elCover.src = album.cover || 'cd_cover.svg';
    }
    if (elTitle) elTitle.textContent = album.title;
    if (elMeta) elMeta.textContent = `${album.artist} • ${album.totalTracks} canciones • ${album.year} • Master HQ`;
    if (elBadge) elBadge.textContent = album.badge || 'ÁLBUM SELECCIONADO';
    if (elSearch) elSearch.value = '';

    renderCdTracks(cdTrckasState.filteredTracks);
    renderCdTrckasAlbums();

    if (autoScroll) {
        const sec = document.getElementById('cdSelectedAlbumSection');
        if (sec) {
            sec.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }
}

function renderCdTracks(tracks) {
    const container = document.getElementById('cdTracksContainer');
    if (!container) return;

    if (!tracks || tracks.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 30px; color: #94a3b8;">
                <p>No se encontraron canciones con ese criterio de búsqueda.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = tracks.map((t, index) => {
        const isCurrentPlaying = isPlaying && currentTrackList[currentTrackIndex]?.title === t.title;
        return `
            <div class="cd-track-row ${isCurrentPlaying ? 'playing' : ''}" onclick="playCdTrckasTrack('${cdTrckasState.selectedAlbumId}', ${index})">
                <div class="cd-track-num">
                    ${isCurrentPlaying ? '<i class="fa-solid fa-volume-high" style="color:#06b6d4;"></i>' : (t.number || (index + 1))}
                </div>
                <div class="cd-track-details">
                    <h4>${t.title}</h4>
                    <p>${t.artist}</p>
                </div>
                <div class="cd-track-badges">
                    <span class="cd-tag-genre">${t.genre}</span>
                    <span class="cd-tag-bpm"><i class="fa-solid fa-gauge-high"></i> ${t.bpm} BPM</span>
                    <span class="cd-tag-source"><i class="fa-solid fa-bolt"></i> 320kbps</span>
                </div>
                <div class="cd-track-duration">${t.duration}</div>
                <button class="btn-cd-play-track" onclick="event.stopPropagation(); playCdTrckasTrack('${cdTrckasState.selectedAlbumId}', ${index})" title="Reproducir">
                    <i class="fa-solid ${isCurrentPlaying ? 'fa-pause' : 'fa-play'}"></i>
                </button>
            </div>
        `;
    }).join('');
}

function filterCdTracks(query) {
    const q = (query || '').toLowerCase().trim();
    if (!q) {
        cdTrckasState.filteredTracks = [...cdTrckasState.currentTracks];
    } else {
        cdTrckasState.filteredTracks = cdTrckasState.currentTracks.filter(t => 
            (t.title && t.title.toLowerCase().includes(q)) ||
            (t.artist && t.artist.toLowerCase().includes(q)) ||
            (t.genre && t.genre.toLowerCase().includes(q)) ||
            String(t.bpm).includes(q)
        );
    }
    renderCdTracks(cdTrckasState.filteredTracks);
}

function playCdTrckasTrack(albumId, trackIndex) {
    const album = cdTrckasState.allAlbums.find(a => a.id === albumId);
    if (!album || !album.tracks || album.tracks.length === 0) return;

    currentTrackList = album.tracks;
    currentTrackIndex = trackIndex;
    playTrackFromList(trackIndex);
    renderCdTracks(cdTrckasState.filteredTracks);
}

function playCdTrckasAlbumDirect(albumId) {
    selectCdAlbum(albumId);
    playCdTrckasTrack(albumId, 0);
}

function playCdTrckasOfficialAlbum() {
    switchTab('cdtrckas');
    selectCdAlbum('cd_trckas_official');
    playCdTrckasTrack('cd_trckas_official', 0);
}

function shuffleCdTrckasAlbum() {
    const album = cdTrckasState.allAlbums.find(a => a.id === cdTrckasState.selectedAlbumId);
    if (!album || !album.tracks || album.tracks.length === 0) return;

    currentTrackList = [...album.tracks].sort(() => Math.random() - 0.5);
    currentTrackIndex = 0;
    playTrackFromList(0);
    renderCdTracks(cdTrckasState.filteredTracks);
}

function openCdTrckasFromHome() {
    switchTab('cdtrckas');
    selectCdAlbum('cd_trckas_official');
}

function openSsassVaultFromHeader() {
    switchTab('cdtrckas');
    filterCdTrckasAlbums('ssass');
    const ssassAlbum = cdTrckasState.allAlbums.find(a => a.category === 'ssass' || a.id.startsWith('ssass_'));
    if (ssassAlbum) {
        selectCdAlbum(ssassAlbum.id);
    }
    const grid = document.getElementById('cdtrckasAlbumsGrid');
    if (grid) grid.scrollIntoView({ behavior: 'smooth' });
}

async function refreshCdTrckasFromCloud(showToast = true) {
    try {
        const res = await fetch(`https://cd-trckas.vercel.app/catalog_manifest.json?_t=${Date.now()}`);
        if (!res.ok) throw new Error('Respuesta remota no disponible');
        const remoteData = await res.json();
        processCdTrckasData(remoteData);

        // Check local storage for user published labels
        const localSaved = localStorage.getItem('cd_trckas_manifest');
        if (localSaved) {
            try {
                const parsedLocal = JSON.parse(localSaved);
                if (parsedLocal && parsedLocal.labels) {
                    processCdTrckasData({ labels: [...(remoteData.labels || []), ...parsedLocal.labels] });
                }
            } catch (le) {
                console.warn('Local manifest parse err:', le);
            }
        }

        if (showToast) {
            alert(`💿 Catálogo CD-TRCKAS sincronizado con éxito.
Total Álbumes: ${cdTrckasState.allAlbums.length}
Pistas totales disponibles: ${document.getElementById('cdStatTracks')?.textContent || '28'}`);
        }
    } catch (err) {
        console.warn('No se pudo contactar el servidor remoto de CD-TRCKAS:', err);
        if (showToast) {
            alert('Se está utilizando la copia maestra local de CD-TRCKAS (28 tracks oficiales + Bóveda SSASS).');
        }
    }
}
