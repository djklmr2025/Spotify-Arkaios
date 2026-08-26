const express = require('express');
const cors = require('cors');
const { exec, spawn } = require('child_process');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 8788;

app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// Estado global del descargador y demonio
const downloadQueue = [];
const activeDownloads = new Map();
const completedDownloads = [];

// Catálogo por defecto de música y radio
const catalogTracks = [
    { id: '1', title: 'Starboy', artist: 'The Weeknd', album: 'Starboy', duration: '3:50', url: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3', cover: 'https://picsum.photos/id/10/300/300' },
    { id: '2', title: 'Blinding Lights', artist: 'The Weeknd', album: 'After Hours', duration: '3:20', url: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3', cover: 'https://picsum.photos/id/20/300/300' },
    { id: '3', title: 'As It Was', artist: 'Harry Styles', album: "Harry's House", duration: '2:47', url: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3', cover: 'https://picsum.photos/id/30/300/300' },
    { id: '4', title: 'Levitating', artist: 'Dua Lipa', album: 'Future Nostalgia', duration: '3:23', url: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3', cover: 'https://picsum.photos/id/40/300/300' },
    { id: '5', title: 'Flowers', artist: 'Miley Cyrus', album: 'Endless Summer Vacation', duration: '3:20', url: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3', cover: 'https://picsum.photos/id/50/300/300' }
];

const radioStations = [
    { id: 'r1', name: 'Reggaeton Flow FM', genre: 'Reggaeton', streamUrl: 'https://stream.zeno.fm/f3wvbbqmdg8uv', isLive: true },
    { id: 'r2', name: 'Exa FM 104.9 Live', genre: 'Pop Latino', streamUrl: 'https://stream.zeno.fm/05w6t7gq78quv', isLive: true },
    { id: 'r3', name: 'SomaFM Groove Salad', genre: 'Lo-Fi / Ambient', streamUrl: 'https://ice1.somafm.com/groovesalad-128-mp3', isLive: true },
    { id: 'r4', name: 'SomaFM Synthwave 80s', genre: 'Synthwave', streamUrl: 'https://ice1.somafm.com/synthwave-128-mp3', isLive: true },
    { id: 'r5', name: 'Classic Rock HD Radio', genre: 'Rock Clásico', streamUrl: 'https://ice1.somafm.com/deepspace-128-mp3', isLive: true }
];

// Health & Status
app.get('/api/status', (req, res) => {
    res.json({
        status: 'online',
        service: 'Spotify-Arkaios Daemon Engine',
        version: '1.2.0',
        port: PORT,
        activeDownloadsCount: activeDownloads.size,
        queueLength: downloadQueue.length,
        completedCount: completedDownloads.length,
        timestamp: new Date().toISOString()
    });
});

// Buscador de Canciones
app.get('/api/search', (req, res) => {
    const query = (req.query.q || '').toLowerCase();
    if (!query) {
        return res.json({ results: catalogTracks });
    }
    const filtered = catalogTracks.filter(t => 
        t.title.toLowerCase().includes(query) ||
        t.artist.toLowerCase().includes(query) ||
        t.album.toLowerCase().includes(query)
    );
    res.json({ query, count: filtered.length, results: filtered });
});

// Emisoras de Radio en Vivo
app.get('/api/radio', (req, res) => {
    res.json({ count: radioStations.length, stations: radioStations });
});

// AMR Pay Wallet Balance & Transactions
app.get('/api/amr/wallet', (req, res) => {
    res.json({
        address: '0xARKAIOS_W18392817293812739182739',
        balanceAmr: 1250.75,
        usdEquivalent: 125.075,
        network: 'ARKAIOS Mainnet v2',
        transactions: [
            { id: 'tx_01', type: 'RECHARGE', amount: 500, usd: 50.00, date: '2026-08-25', status: 'COMPLETED' },
            { id: 'tx_02', type: 'PREMIUM_SUBSCRIPTION', amount: -150, usd: 15.00, date: '2026-08-26', status: 'COMPLETED' }
        ]
    });
});

// Descargador de Audio API
app.post('/api/download', (req, res) => {
    const { url, title, artist, format } = req.body;
    if (!url && !title) {
        return res.status(400).json({ error: 'Se requiere parámetro "url" o "title"' });
    }

    const taskId = 'dl_' + Date.now();
    const downloadItem = {
        taskId,
        url: url || `ytsearch:${title} ${artist || ''}`,
        title: title || 'Pista de Audio',
        artist: artist || 'Artista Desconocido',
        format: format || 'mp3',
        status: 'PENDING',
        progressPercent: 0,
        createdAt: new Date().toISOString()
    };

    downloadQueue.push(downloadItem);
    processNextDownload();

    res.json({
        message: 'Solicitud de descarga encolada en el demonio Arkaios',
        taskId,
        downloadItem
    });
});

// Consultar Estado de Tareas de Descarga
app.get('/api/downloads', (req, res) => {
    res.json({
        queue: downloadQueue,
        active: Array.from(activeDownloads.values()),
        completed: completedDownloads
    });
});

function processNextDownload() {
    if (downloadQueue.length === 0 || activeDownloads.size >= 3) return;

    const task = downloadQueue.shift();
    task.status = 'DOWNLOADING';
    task.progressPercent = 10;
    activeDownloads.set(task.taskId, task);

    // Simulación de descarga asistida por yt-dlp
    const interval = setInterval(() => {
        task.progressPercent += 30;
        if (task.progressPercent >= 100) {
            clearInterval(interval);
            task.progressPercent = 100;
            task.status = 'COMPLETED';
            task.downloadPath = path.join(__dirname, 'downloads', `${task.taskId}.${task.format}`);
            activeDownloads.delete(task.taskId);
            completedDownloads.push(task);
            processNextDownload();
        }
    }, 1000);
}

// Iniciar Servidor
app.listen(PORT, () => {
    console.log(`=======================================================`);
    console.log(` 🚀 Demonio API de Spotify-Arkaios escuchando en puerto ${PORT}`);
    console.log(` 🌐 Web Interface: http://localhost:${PORT}`);
    console.log(` 📡 REST Endpoints: /api/status, /api/search, /api/download, /api/radio`);
    console.log(`=======================================================`);
});
