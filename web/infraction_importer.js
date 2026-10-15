#!/usr/bin/env node
/**
 * ARKAIOS-PUTIFY — IMPORTADOR MUSICAL INFRACTION
 * Escáner e importador por lotes para archivos musicales de Infraction
 * Soporta: MP3, M4A, FLAC, WAV.
 * Deduplicación criptográfica: SHA-256.
 * Extracción de metadatos mediante ffprobe y preservación de nombres originales.
 */

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { execFileSync } = require('child_process');

const DEFAULT_SOURCE_DIR = process.env.INFRACTION_DIR || 'C:\\ARKAIOS\\MUSIC\\INFRACTION';
const VALID_EXTENSIONS = new Set(['.mp3', '.m4a', '.flac', '.wav']);

// Rutas de destino del catálogo
const CATALOG_OUTPUT_PATHS = [
    path.join(__dirname, 'infraction_catalog.json'),
    path.join(__dirname, 'public', 'infraction_catalog.json'),
    path.join(__dirname, '../app/src/main/assets', 'infraction_catalog.json')
];

/**
 * Encuentra el ejecutable ffprobe en el sistema
 */
function findFfprobe() {
    const candidatePaths = [
        'C:\\ARKAIOS\\INFRACTION\\ffprobe.exe',
        path.join(process.env.LOCALAPPDATA || '', 'Microsoft\\WinGet\\Packages\\Gyan.FFmpeg_Microsoft.Winget.Source_8wekyb3d8bbwe\\ffmpeg-9.0.2-full_build\\bin\\ffprobe.exe')
    ];
    for (const p of candidatePaths) {
        if (fs.existsSync(p)) return p;
    }
    return 'ffprobe'; // fallback al PATH
}

const FFPROBE_BIN = findFfprobe();

/**
 * Calcula el hash SHA-256 del contenido binario del archivo
 */
function calculateSha256(filePath) {
    const buffer = fs.readFileSync(filePath);
    return crypto.createHash('sha256').update(buffer).digest('hex');
}

/**
 * Extrae metadatos y duración con ffprobe
 */
function probeAudio(filePath) {
    try {
        const stdout = execFileSync(FFPROBE_BIN, [
            '-v', 'quiet',
            '-print_format', 'json',
            '-show_format',
            '-show_streams',
            filePath
        ], { encoding: 'utf-8', timeout: 5000 });

        const parsed = JSON.parse(stdout);
        const format = parsed.format || {};
        const durationSec = parseFloat(format.duration || 0);
        const tags = format.tags || {};

        return {
            durationSec: isNaN(durationSec) ? 0 : durationSec,
            bitRate: parseInt(format.bit_rate || 0, 10),
            tags: tags
        };
    } catch (e) {
        // Fallback si ffprobe falla
        return { durationSec: 0, bitRate: 0, tags: {} };
    }
}

/**
 * Formatea segundos a M:SS
 */
function formatDuration(seconds) {
    if (!seconds || seconds <= 0) return '3:30';
    const totalSec = Math.round(seconds);
    const m = Math.floor(totalSec / 60);
    const s = totalSec % 60;
    return `${m}:${s < 10 ? '0' : ''}${s}`;
}

/**
 * Limpia y normaliza el título y artista preservando los nombres originales
 */
function parseTrackDetails(fileName, subfolderName, tags) {
    const ext = path.extname(fileName);
    const baseName = path.basename(fileName, ext);

    let title = tags.title || tags.TITLE || '';
    let artist = tags.artist || tags.ARTIST || '';
    let album = tags.album || tags.ALBUM || '';

    // Si las etiquetas ID3 están vacías, extraer del nombre de archivo y carpeta
    if (!title) {
        let cleaned = baseName
            .replace(/\[no copyright music\]/gi, '')
            .replace(/\(no copyright music\)/gi, '')
            .replace(/no copyright music/gi, '')
            .trim();

        if (cleaned.includes(' - ')) {
            const parts = cleaned.split(' - ');
            artist = parts[0].trim();
            title = parts.slice(1).join(' - ').trim();
        } else if (cleaned.toLowerCase().includes(' by ')) {
            const parts = cleaned.split(/ by /i);
            title = parts[0].trim();
            artist = parts[1].trim();
        } else {
            title = cleaned;
            artist = 'Infraction';
        }
    }

    if (!artist) artist = 'Infraction';
    if (!album) {
        album = (subfolderName && subfolderName !== 'INFRACTION') ? subfolderName : 'Infraction Collection';
    }

    return {
        title: title || baseName,
        artist,
        album
    };
}

/**
 * Escaneo recursivo del directorio
 */
function scanInfractionLibrary(sourceDir = DEFAULT_SOURCE_DIR) {
    console.log(`[ARKAIOS-PUTIFY] Iniciando importación recursiva desde: ${sourceDir}`);
    if (!fs.existsSync(sourceDir)) {
        console.warn(`[WARN] Directorio no encontrado: ${sourceDir}. Creándolo...`);
        fs.mkdirSync(sourceDir, { recursive: true });
    }

    const tracks = [];
    const duplicates = [];
    const sha256Registry = new Map(); // hash -> first track id

    function walkDir(currentDir) {
        const entries = fs.readdirSync(currentDir, { withFileTypes: true });
        for (const entry of entries) {
            const fullPath = path.join(currentDir, entry.name);
            if (entry.isDirectory()) {
                walkDir(fullPath);
            } else if (entry.isFile()) {
                const ext = path.extname(entry.name).toLowerCase();
                if (VALID_EXTENSIONS.has(ext)) {
                    processAudioFile(fullPath, entry.name, currentDir);
                }
            }
        }
    }

    function processAudioFile(filePath, fileName, currentDir) {
        try {
            const stat = fs.statSync(filePath);
            const sha256 = calculateSha256(filePath);

            // Detección de duplicados mediante SHA-256
            if (sha256Registry.has(sha256)) {
                const existing = sha256Registry.get(sha256);
                console.log(`  [DUPLICADO DETECTADO] "${fileName}" coincide exactamente en hash con "${existing.fileName}". Omitiendo incorporación duplicada.`);
                duplicates.push({
                    duplicateFilePath: filePath,
                    originalTrackId: existing.id,
                    originalFilePath: existing.filePath,
                    sha256
                });
                return;
            }

            // Metadatos
            const probe = probeAudio(filePath);
            const durationSec = probe.durationSec > 0 ? probe.durationSec : Math.max(30, Math.round(stat.size / 32000));
            const durationMs = Math.round(durationSec * 1000);
            const durationFormatted = formatDuration(durationSec);

            const subfolder = path.basename(currentDir);
            const { title, artist, album } = parseTrackDetails(fileName, subfolder, probe.tags);

            const trackId = `infr_${sha256.substring(0, 12)}`;
            const formatStr = extToFormat(path.extname(fileName));

            const trackRecord = {
                id: trackId,
                title,
                artist,
                album,
                genre: subfolder !== 'INFRACTION' ? subfolder : 'Royalty-Free Electronic / Cinematic',
                duration: durationFormatted,
                durationMs,
                sizeBytes: stat.size,
                format: formatStr,
                fileName,
                filePath,
                sha256,
                streamUrl: `/api/infraction/stream/${trackId}`,
                cover: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80',
                licenseStatus: 'AUTHORIZED_ROYALTY_FREE_ATTRIBUTION',
                licenseAttribution: 'Music provided by Infraction / InAudio (https://inaudio.org)',
                distributionScope: 'PRIVATE_LOCAL_ONLY',
                allowPublicDownload: false,
                isPrivateFile: true,
                importedAt: new Date().toISOString()
            };

            sha256Registry.set(sha256, trackRecord);
            tracks.push(trackRecord);
            console.log(`  [OK] Indexado: [${formatStr}] "${title}" — ${artist} (${durationFormatted})`);
        } catch (err) {
            console.error(`  [ERROR] Fallo al procesar ${filePath}:`, err.message);
        }
    }

    walkDir(sourceDir);

    const catalog = {
        generatedAt: new Date().toISOString(),
        sourceDirectory: sourceDir,
        totalUniqueTracks: tracks.length,
        totalDuplicatesSkipped: duplicates.length,
        supportedFormats: Array.from(VALID_EXTENSIONS).map(e => e.replace('.', '').toUpperCase()),
        tracks,
        duplicates
    };

    // Guardar en las ubicaciones de salida
    for (const outPath of CATALOG_OUTPUT_PATHS) {
        try {
            const dir = path.dirname(outPath);
            if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
            fs.writeFileSync(outPath, JSON.stringify(catalog, null, 2), 'utf-8');
            console.log(`[GUARDADO] Catálogo Infraction exportado en: ${outPath}`);
        } catch (e) {
            console.error(`[ERROR] No se pudo guardar en ${outPath}:`, e.message);
        }
    }

    return catalog;
}

function extToFormat(ext) {
    switch (ext.toLowerCase()) {
        case '.mp3': return 'MP3';
        case '.wav': return 'WAV';
        case '.m4a': return 'M4A';
        case '.flac': return 'FLAC';
        default: return 'AUDIO';
    }
}

// Ejecutar directamente si se llama por CLI
if (require.main === module) {
    const targetDir = process.argv[2] || DEFAULT_SOURCE_DIR;
    const result = scanInfractionLibrary(targetDir);
    console.log(`=======================================================`);
    console.log(` [COMPLETADO] ${result.totalUniqueTracks} pistas únicas indexadas.`);
    console.log(` [FILTRADOS] ${result.totalDuplicatesSkipped} duplicados omitidos por SHA-256.`);
    console.log(`=======================================================`);
}

module.exports = {
    scanInfractionLibrary,
    calculateSha256,
    probeAudio
};
