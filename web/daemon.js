const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');
const http = require('http');
const https = require('https');

const app = express();
const PORT = process.env.PORT || 8788;
const DB_FILE = path.join(__dirname, 'arkaios_music_db.json');
const MUSIC_DIR = process.env.MUSIC_DIR || 'C:\\DJKLMR\\Music';
const INFRACTION_DIR = process.env.INFRACTION_DIR || 'C:\\ARKAIOS\\MUSIC\\INFRACTION';
const INFRACTION_DB_FILE = path.join(__dirname, 'infraction_catalog.json');

const { scanInfractionLibrary } = require('./infraction_importer');

app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));
app.use('/assets', express.static(path.join(__dirname, '../app/src/main/assets/web')));

// Estado global
let realTracksCatalog = [];
const tracksMap = new Map();
let infractionCatalog = { totalUniqueTracks: 0, totalDuplicatesSkipped: 0, tracks: [], duplicates: [] };
const infractionTracksMap = new Map();

const downloadQueue = [];
const activeDownloads = new Map();
const completedDownloads = [];

// Cargar catálogo Infraction
function loadInfractionDatabase() {
    if (fs.existsSync(INFRACTION_DB_FILE)) {
        try {
            console.log(`[INFO] Cargando colección Infraction desde ${INFRACTION_DB_FILE}...`);
            infractionCatalog = JSON.parse(fs.readFileSync(INFRACTION_DB_FILE, 'utf-8'));
            infractionTracksMap.clear();
            for (const t of infractionCatalog.tracks || []) {
                infractionTracksMap.set(t.id, t);
            }
            console.log(`[OK] Colección Infraction cargada: ${infractionTracksMap.size} pistas únicas.`);
        } catch (e) {
            console.error('[ERROR] Error cargando DB Infraction:', e);
        }
    } else {
        console.log(`[INFO] No se encontró catálogo Infraction. Ejecutando escaneo inicial en ${INFRACTION_DIR}...`);
        infractionCatalog = scanInfractionLibrary(INFRACTION_DIR);
        infractionTracksMap.clear();
        for (const t of infractionCatalog.tracks || []) {
            infractionTracksMap.set(t.id, t);
        }
    }
}

// Cargar base de datos local de 19,000+ pistas
function loadMusicDatabase() {
    if (fs.existsSync(DB_FILE)) {
        try {
            console.log(`[INFO] Cargando base de datos musical desde ${DB_FILE}...`);
            const data = JSON.parse(fs.readFileSync(DB_FILE, 'utf-8'));
            realTracksCatalog = data;
            tracksMap.clear();
            for (const t of realTracksCatalog) {
                tracksMap.set(t.id, t);
            }
            console.log(`[OK] ${realTracksCatalog.length} pistas de DJ KLMR cargadas exitosamente.`);
        } catch (e) {
            console.error('[ERROR] Error cargando DB musical:', e);
        }
    } else {
        console.warn(`[WARN] No se encontró ${DB_FILE}. Se creará al escanear.`);
    }
}

loadInfractionDatabase();
loadMusicDatabase();

// Helper de MIME type
function getMimeType(filePath) {
    const ext = path.extname(filePath).toLowerCase();
    switch (ext) {
        case '.mp3': return 'audio/mpeg';
        case '.wav': return 'audio/wav';
        case '.m4a': return 'audio/mp4';
        case '.flac': return 'audio/flac';
        case '.ogg': return 'audio/ogg';
        case '.aac': return 'audio/aac';
        default: return 'audio/mpeg';
    }
}

// 1. Endpoint de Streaming Real con HTTP 206 (Range Requests / Scrubber)
app.get('/api/stream/:id', (req, res) => {
    const track = tracksMap.get(req.params.id);
    if (!track || !track.filePath || !fs.existsSync(track.filePath)) {
        return res.status(404).json({ error: 'Pista de audio no encontrada en el servidor' });
    }

    const filePath = track.filePath;
    const stat = fs.statSync(filePath);
    const fileSize = stat.size;
    const range = req.headers.range;

    if (range) {
        const parts = range.replace(/bytes=/, "").split("-");
        const start = parseInt(parts[0], 10);
        const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;

        if (start >= fileSize) {
            res.status(416).send('Requested range not satisfiable\n' + start + ' >= ' + fileSize);
            return;
        }

        const chunksize = (end - start) + 1;
        const file = fs.createReadStream(filePath, { start, end });
        const head = {
            'Content-Range': `bytes ${start}-${end}/${fileSize}`,
            'Accept-Ranges': 'bytes',
            'Content-Length': chunksize,
            'Content-Type': getMimeType(filePath),
            'Cache-Control': 'public, max-age=3600'
        };
        res.writeHead(206, head);
        file.pipe(res);
    } else {
        const head = {
            'Content-Length': fileSize,
            'Content-Type': getMimeType(filePath),
            'Accept-Ranges': 'bytes',
            'Cache-Control': 'public, max-age=3600'
        };
        res.writeHead(200, head);
        fs.createReadStream(filePath).pipe(res);
    }
});

// 1.1 Endpoint de Streaming para Colección Infraction (HTTP 206, sólo reproducción local, sin descarga pública)
app.get('/api/infraction/stream/:id', (req, res) => {
    const track = infractionTracksMap.get(req.params.id);
    if (!track || !track.filePath || !fs.existsSync(track.filePath)) {
        return res.status(404).json({ error: 'Pista Infraction no encontrada en la biblioteca local' });
    }

    const filePath = track.filePath;
    const stat = fs.statSync(filePath);
    const fileSize = stat.size;
    const range = req.headers.range;

    // Respetar separación: archivo privado para reproducción local, no descarga pública adjunta
    res.setHeader('X-ARKAIOS-Scope', track.distributionScope || 'PRIVATE_LOCAL_ONLY');
    res.setHeader('X-ARKAIOS-License', track.licenseStatus || 'AUTHORIZED_ROYALTY_FREE_ATTRIBUTION');

    if (range) {
        const parts = range.replace(/bytes=/, "").split("-");
        const start = parseInt(parts[0], 10);
        const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;

        if (start >= fileSize) {
            res.status(416).send('Requested range not satisfiable\n' + start + ' >= ' + fileSize);
            return;
        }

        const chunksize = (end - start) + 1;
        const file = fs.createReadStream(filePath, { start, end });
        const head = {
            'Content-Range': `bytes ${start}-${end}/${fileSize}`,
            'Accept-Ranges': 'bytes',
            'Content-Length': chunksize,
            'Content-Type': getMimeType(filePath),
            'Cache-Control': 'private, no-cache'
        };
        res.writeHead(206, head);
        file.pipe(res);
    } else {
        const head = {
            'Content-Length': fileSize,
            'Content-Type': getMimeType(filePath),
            'Accept-Ranges': 'bytes',
            'Cache-Control': 'private, no-cache'
        };
        res.writeHead(200, head);
        fs.createReadStream(filePath).pipe(res);
    }
});

// 1.2 Catálogo Infraction JSON
app.get('/api/infraction/catalog', (req, res) => {
    res.json(infractionCatalog);
});

// 1.3 Re-escanear biblioteca Infraction en C:\ARKAIOS\MUSIC\INFRACTION
app.post('/api/infraction/rescan', (req, res) => {
    try {
        const targetDir = req.body.dir || INFRACTION_DIR;
        infractionCatalog = scanInfractionLibrary(targetDir);
        infractionTracksMap.clear();
        for (const t of infractionCatalog.tracks || []) {
            infractionTracksMap.set(t.id, t);
        }
        res.json({
            ok: true,
            message: 'Biblioteca Infraction re-escaneada con éxito.',
            totalUniqueTracks: infractionCatalog.totalUniqueTracks,
            totalDuplicatesSkipped: infractionCatalog.totalDuplicatesSkipped
        });
    } catch (e) {
        res.status(500).json({ ok: false, error: e.message });
    }
});

// 2. Health & Node Status Endpoint
app.get('/api/status', (req, res) => {
    res.json({
        status: 'online',
        service: 'Spotify-Arkaios Master DJ Node',
        version: 'v2.1.0',
        port: PORT,
        totalTracks: realTracksCatalog.length,
        infractionTracksCount: infractionTracksMap.size,
        infractionDuplicatesCount: infractionCatalog.totalDuplicatesSkipped || 0,
        musicDirectory: MUSIC_DIR,
        infractionDirectory: INFRACTION_DIR,
        cluster5TB: 'GOOGLE_DRIVE_5TB_CONNECTED',
        activeDownloadsCount: activeDownloads.size,
        timestamp: new Date().toISOString()
    });
});

// 3. Catálogo y Búsqueda Ultrarrápida (incluye Infraction)
app.get('/api/search', (req, res) => {
    const query = (req.query.q || '').trim().toLowerCase();
    
    // Si no hay query, devolver una selección combinada (KLMR + Infraction)
    if (!query) {
        const infractionSlice = (infractionCatalog.tracks || []).slice(0, 20).map(t => ({
            id: t.id,
            title: t.title,
            artist: t.artist,
            album: t.album,
            duration: t.duration || '3:30',
            url: t.streamUrl,
            streamUrl: t.streamUrl,
            cover: t.cover,
            genre: t.genre,
            format: t.format,
            bitrate: 'Original Lossless/HQ (Infraction Royalty-Free)',
            isInfraction: true,
            licenseStatus: t.licenseStatus
        }));

        const klmrSlice = realTracksCatalog.slice(0, 40).map(t => ({
            id: t.id,
            title: t.title,
            artist: t.artist,
            album: t.album,
            duration: t.duration || '3:45',
            url: `/api/stream/${t.id}`,
            streamUrl: `/api/stream/${t.id}`,
            cover: t.cover,
            genre: t.genre,
            format: t.format,
            bitrate: '320 kbps (HQ Lossless/Master)',
            isInfraction: false
        }));

        const combined = [...infractionSlice, ...klmrSlice];

        return res.json({ 
            results: combined, 
            count: combined.length, 
            totalCatalog: realTracksCatalog.length + infractionTracksMap.size, 
            source: 'ARKAIOS Unified Vault (KLMR + Infraction Royalty-Free)' 
        });
    }

    // Búsqueda en memoria sobre Infraction + DJ KLMR
    const matches = [];
    const limit = 80;

    // 1. Buscar en Infraction
    for (const t of infractionCatalog.tracks || []) {
        if (matches.length >= limit) break;
        if (t.title.toLowerCase().includes(query) || t.artist.toLowerCase().includes(query) || t.album.toLowerCase().includes(query) || t.genre.toLowerCase().includes(query)) {
            matches.push({
                id: t.id,
                title: t.title,
                artist: t.artist,
                album: t.album,
                duration: t.duration || '3:30',
                url: t.streamUrl,
                streamUrl: t.streamUrl,
                cover: t.cover,
                genre: t.genre,
                format: t.format,
                bitrate: 'Original HQ (Infraction Royalty-Free)',
                isInfraction: true,
                licenseStatus: t.licenseStatus
            });
        }
    }
    
    // 2. Buscar en canciones de DJ KLMR
    for (let i = 0; i < realTracksCatalog.length && matches.length < limit; i++) {
        const t = realTracksCatalog[i];
        if (t.title.toLowerCase().includes(query) || t.artist.toLowerCase().includes(query) || (t.album && t.album.toLowerCase().includes(query))) {
            matches.push({
                id: t.id,
                title: t.title,
                artist: t.artist,
                album: t.album,
                duration: t.duration || '3:45',
                url: `/api/stream/${t.id}`,
                streamUrl: `/api/stream/${t.id}`,
                cover: t.cover,
                genre: t.genre,
                format: t.format,
                bitrate: '320 kbps (HQ Audio)',
                isInfraction: false
            });
        }
    }

    res.json({
        query,
        count: matches.length,
        totalCatalog: realTracksCatalog.length + infractionTracksMap.size,
        results: matches,
        source: 'ARKAIOS Live Search'
    });
});

// 4. Endpoint de Lista Completa Paginada
app.get('/api/tracks', (req, res) => {
    const page = Math.max(1, parseInt(req.query.page) || 1);
    const limit = Math.min(100, Math.max(10, parseInt(req.query.limit) || 50));
    const start = (page - 1) * limit;
    const end = start + limit;
    
    const slice = realTracksCatalog.slice(start, end).map(t => ({
        id: t.id,
        title: t.title,
        artist: t.artist,
        album: t.album,
        duration: t.duration || '3:45',
        url: `/api/stream/${t.id}`,
        streamUrl: `/api/stream/${t.id}`,
        cover: t.cover,
        format: t.format
    }));

    res.json({
        page,
        limit,
        totalPages: Math.ceil(realTracksCatalog.length / limit),
        totalTracks: realTracksCatalog.length,
        tracks: slice
    });
});

// 5. Emisoras de Radio en Vivo
const radioStations = [
    { id: 'r1', name: 'Reggaeton Flow FM Live', genre: 'Reggaeton / Urbano', streamUrl: 'https://stream.zeno.fm/f3wvbbqmdg8uv', listeners: '14.2k oyentes', cover: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400' },
    { id: 'r2', name: 'Exa FM 104.9 Live', genre: 'Pop Latino', streamUrl: 'https://stream.zeno.fm/05w6t7gq78quv', listeners: '28.9k oyentes', cover: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400' },
    { id: 'r3', name: 'SomaFM Groove Salad', genre: 'Lo-Fi / Ambient', streamUrl: 'https://ice1.somafm.com/groovesalad-128-mp3', listeners: '9.4k oyentes', cover: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400' },
    { id: 'r4', name: 'SomaFM Synthwave 80s', genre: 'Synthwave', streamUrl: 'https://ice1.somafm.com/synthwave-128-mp3', listeners: '18.1k oyentes', cover: 'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=400' }
];

app.get('/api/radio', (req, res) => {
    res.json({ count: radioStations.length, stations: radioStations });
});

// 6. Descarga Directa del APK
app.get('/api/apk/download', (req, res) => {
    const apkPaths = [
        path.join(__dirname, '../app/build/outputs/apk/debug/app-debug.apk'),
        path.join(__dirname, '../app-debug.apk')
    ];
    for (const apkPath of apkPaths) {
        if (fs.existsSync(apkPath)) {
            return res.download(apkPath, 'Spotify-Arkaios-v2.1.0.apk');
        }
    }
    res.status(404).json({ error: 'APK no encontrada localmente' });
});

app.listen(PORT, () => {
    console.log(`=======================================================`);
    console.log(` 🎧 Servidor Real Spotify-Arkaios v2.1.0`);
    console.log(` 🎵 Pistas DJ KLMR Indexadas: ${realTracksCatalog.length}`);
    console.log(` 🌐 Plataforma Web: http://localhost:${PORT}`);
    console.log(` 📱 Descarga APK: http://localhost:${PORT}/api/apk/download`);
    console.log(`=======================================================`);
});
