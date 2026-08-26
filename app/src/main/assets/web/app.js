const API_BASE = 'http://localhost:8788/api';

let currentCatalog = [];
let currentTrackIndex = 0;
let isPlaying = false;
let audioPlayer = new Audio();

document.addEventListener('DOMContentLoaded', () => {
    initTabNavigation();
    checkDaemonStatus();
    loadHomeCatalog();
    loadRadioStations();

    audioPlayer.addEventListener('timeupdate', updateProgress);
    audioPlayer.addEventListener('ended', nextTrack);
});

function initTabNavigation() {
    const navButtons = document.querySelectorAll('.nav-btn');
    navButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const tabName = btn.getAttribute('data-tab');
            switchTab(tabName);
        });
    });
}

function switchTab(tabName) {
    document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));

    const targetBtn = document.querySelector(`.nav-btn[data-tab="${tabName}"]`);
    const targetTab = document.getElementById(`tab-${tabName}`);

    if (targetBtn) targetBtn.classList.add('active');
    if (targetTab) targetTab.classList.add('active');
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
        document.getElementById('daemonStatusText').textContent = 'Demonio Offline';
        document.querySelector('.status-dot').style.backgroundColor = '#ef4444';
    }
}

async function loadHomeCatalog() {
    try {
        const res = await fetch(`${API_BASE}/search`);
        const data = await res.json();
        currentCatalog = data.results || [];
        renderTracksGrid(currentCatalog);
    } catch (e) {
        console.error('Error cargando catálogo:', e);
    }
}

function renderTracksGrid(tracks) {
    const container = document.getElementById('homeTracksGrid');
    container.innerHTML = '';

    tracks.forEach((track, index) => {
        const card = document.createElement('div');
        card.className = 'track-card';
        card.innerHTML = `
            <img src="${track.cover}" alt="${track.title}">
            <h4>${track.title}</h4>
            <p>${track.artist}</p>
        `;
        card.onclick = () => playTrackIndex(index);
        container.appendChild(card);
    });
}

async function loadRadioStations() {
    try {
        const res = await fetch(`${API_BASE}/radio`);
        const data = await res.json();
        const container = document.getElementById('radioStationsGrid');
        container.innerHTML = '';

        (data.stations || []).forEach(station => {
            const card = document.createElement('div');
            card.className = 'radio-card';
            card.innerHTML = `
                <span class="live-badge">🔴 EN VIVO</span>
                <i class="fa-solid fa-tower-broadcast"></i>
                <h4>${station.name}</h4>
                <p>${station.genre}</p>
                <button class="radio-play-btn" onclick="playRadioStream('${station.streamUrl}', '${station.name}', '${station.genre}')">Sintonizar</button>
            `;
            container.appendChild(card);
        });
    } catch (e) {
        console.error('Error cargando radios:', e);
    }
}

function playTrackIndex(index) {
    if (index < 0 || index >= currentCatalog.length) return;
    currentTrackIndex = index;
    const track = currentCatalog[index];

    document.getElementById('playerTitle').textContent = track.title;
    document.getElementById('playerArtist').textContent = track.artist;
    document.getElementById('playerCover').src = track.cover;

    audioPlayer.src = track.url;
    audioPlayer.play();
    isPlaying = true;
    updatePlayPauseButton();
}

function playRadioStream(url, name, genre) {
    document.getElementById('playerTitle').textContent = name;
    document.getElementById('playerArtist').textContent = `📻 ${genre} (Radio en Vivo)`;
    document.getElementById('playerCover').src = 'https://picsum.photos/id/147/300/300';

    audioPlayer.src = url;
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
    if (currentTrackIndex > 0) playTrackIndex(currentTrackIndex - 1);
}

function nextTrack() {
    if (currentTrackIndex < currentCatalog.length - 1) playTrackIndex(currentTrackIndex + 1);
}

function updateProgress() {
    if (!audioPlayer.duration) return;
    const percent = (audioPlayer.currentTime / audioPlayer.duration) * 100;
    document.getElementById('progressBar').value = percent;
    document.getElementById('currentTime').textContent = formatTime(audioPlayer.currentTime);
    document.getElementById('totalDuration').textContent = formatTime(audioPlayer.duration);
}

function formatTime(secs) {
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
}

async function executeDaemonSearch() {
    const query = document.getElementById('daemonSearchInput').value;
    const res = await fetch(`${API_BASE}/search?q=${encodeURIComponent(query)}`);
    const data = await res.json();
    const container = document.getElementById('searchResultsList');
    container.innerHTML = `<p style="margin-bottom: 10px;">Se encontraron ${data.count} resultados:</p>`;
    
    (data.results || []).forEach(t => {
        const item = document.createElement('div');
        item.style.cssText = 'background:#1e293b; padding:12px; border-radius:10px; margin-bottom:8px; display:flex; justify-content:space-between; align-items:center;';
        item.innerHTML = `
            <div><strong>${t.title}</strong> - ${t.artist}</div>
            <button class="download-action-btn" onclick="requestDownload('${t.title}', '${t.artist}', '${t.url}')">Descargar API</button>
        `;
        container.appendChild(item);
    });
}

async function requestDownload(title, artist, url) {
    const res = await fetch(`${API_BASE}/download`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title, artist, url })
    });
    const data = await res.json();
    alert(`✔ Demonio Arkaios: ${data.message} (Task ID: ${data.taskId})`);
    switchTab('library');
    loadDownloadsQueue();
}

async function loadDownloadsQueue() {
    try {
        const res = await fetch(`${API_BASE}/downloads`);
        const data = await res.json();
        document.getElementById('statActiveDownloads').textContent = data.active.length;
        document.getElementById('statQueueCount').textContent = data.queue.length;
        document.getElementById('statCompletedCount').textContent = data.completed.length;

        const container = document.getElementById('downloadsQueueList');
        container.innerHTML = '';

        [...data.active, ...data.completed].forEach(dl => {
            const item = document.createElement('div');
            item.style.cssText = 'background:#1e293b; padding:12px; border-radius:10px; margin-bottom:8px;';
            item.innerHTML = `
                <div style="display:flex; justify-content:space-between; margin-bottom:5px;">
                    <span><strong>${dl.title}</strong> (${dl.artist})</span>
                    <span style="color:#06b6d4; font-weight:bold;">${dl.status} (${dl.progressPercent}%)</span>
                </div>
            `;
            container.appendChild(item);
        });
    } catch (e) {
        console.error('Error cargando cola:', e);
    }
}
