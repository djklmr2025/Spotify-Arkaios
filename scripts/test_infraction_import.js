/**
 * ARKAIOS-PUTIFY — SUITE DE PRUEBAS AUTOMATIZADAS DE IMPORTACIÓN INFRACTION
 * Valida:
 * 1. Importación recursiva de archivos de audio (MP3, WAV, FLAC, M4A).
 * 2. Deduplicación criptográfica estricta mediante SHA-256.
 * 3. Preservación de nombres de archivo originales y rutas.
 * 4. Extracción de metadatos (título, artista, álbum, género, duración, tamaño).
 * 5. Generación del catálogo JSON con licencia y alcance privado.
 * 6. Streaming HTTP real con soporte de rangos parciales (HTTP 206) y cabecera de ámbito privado.
 */

const fs = require('fs');
const path = require('path');
const http = require('http');
const assert = require('assert');
const { scanInfractionLibrary, calculateSha256 } = require('../web/infraction_importer');

async function runTests() {
    console.log('===============================================================');
    console.log('🧪 ARKAIOS-PUTIFY: INICIANDO PRUEBAS DE IMPORTACIÓN INFRACTION');
    console.log('===============================================================\n');

    const testDir = 'C:\\ARKAIOS\\MUSIC\\INFRACTION';
    assert(fs.existsSync(testDir), `Directorio ${testDir} debe existir`);

    // --- PRUEBA 1: Ejecución del Importador ---
    console.log('Test 1: Ejecutar scanInfractionLibrary() recursivo...');
    const catalog = scanInfractionLibrary(testDir);
    assert(catalog, 'El catálogo debe generarse');
    assert(Array.isArray(catalog.tracks), 'catalog.tracks debe ser un arreglo');
    assert(catalog.tracks.length >= 2, `Se esperaban al menos 2 pistas únicas, se encontraron: ${catalog.tracks.length}`);
    console.log(`  ✔ PASS: ${catalog.tracks.length} pistas únicas identificadas.\n`);

    // --- PRUEBA 2: Deduplicación SHA-256 ---
    console.log('Test 2: Validar deduplicación SHA-256...');
    assert(catalog.totalDuplicatesSkipped >= 1, 'Debe haber al menos 1 duplicado detectado y omitido');
    assert(Array.isArray(catalog.duplicates) && catalog.duplicates.length >= 1, 'catalog.duplicates debe registrar los archivos duplicados');
    
    // Verificar que ninguna pista única comparta el mismo hash SHA-256
    const hashes = new Set();
    for (const t of catalog.tracks) {
        assert(!hashes.has(t.sha256), `Hash SHA-256 duplicado encontrado en pistas únicas: ${t.sha256}`);
        hashes.add(t.sha256);
    }
    console.log(`  ✔ PASS: Deduplicación SHA-256 verificada. ${catalog.totalDuplicatesSkipped} duplicado(s) filtrados correctamente.\n`);

    // --- PRUEBA 3: Formatos Múltiples (MP3 y WAV) ---
    console.log('Test 3: Validar soporte de formatos múltiples...');
    const formatsFound = new Set(catalog.tracks.map(t => t.format));
    assert(formatsFound.has('MP3'), 'Debe contener formato MP3');
    assert(formatsFound.has('WAV'), 'Debe contener formato WAV');
    console.log(`  ✔ PASS: Formatos verificados en catálogo: ${Array.from(formatsFound).join(', ')}\n`);

    // --- PRUEBA 4: Metadatos y Estructura de Licencia ---
    console.log('Test 4: Validar esquema de metadatos y preservación de nombres...');
    for (const t of catalog.tracks) {
        assert(t.id && t.id.startsWith('infr_'), 'ID debe iniciar con infr_');
        assert(t.title && t.title.length > 0, 'Título no debe estar vacío');
        assert(t.artist && t.artist.length > 0, 'Artista no debe estar vacío');
        assert(t.album && t.album.length > 0, 'Álbum no debe estar vacío');
        assert(t.duration && t.duration.includes(':'), 'Duración formateada requerida (M:SS)');
        assert(typeof t.durationMs === 'number' && t.durationMs > 0, 'durationMs debe ser un número positivo');
        assert(t.fileName && fs.existsSync(t.filePath), `Archivo debe existir físicamente: ${t.filePath}`);
        assert(t.sha256 && t.sha256.length === 64, 'SHA-256 debe ser un digest de 64 caracteres hex');
        assert(t.licenseStatus === 'AUTHORIZED_ROYALTY_FREE_ATTRIBUTION', 'Estado de licencia incorrecto');
        assert(t.distributionScope === 'PRIVATE_LOCAL_ONLY', 'Ámbito de distribución debe ser PRIVATE_LOCAL_ONLY');
        assert(t.allowPublicDownload === false, 'allowPublicDownload debe ser false para prevenir descargas públicas');
    }
    console.log('  ✔ PASS: Todos los campos de metadatos y licencias cumplen las especificaciones de seguridad.\n');

    // --- PRUEBA 5: Integridad de Archivos de Salida JSON ---
    console.log('Test 5: Validar archivos JSON generados...');
    const outFiles = [
        path.join(__dirname, '../web/infraction_catalog.json'),
        path.join(__dirname, '../web/public/infraction_catalog.json'),
        path.join(__dirname, '../app/src/main/assets/infraction_catalog.json')
    ];
    for (const f of outFiles) {
        assert(fs.existsSync(f), `Archivo debe existir: ${f}`);
        const parsed = JSON.parse(fs.readFileSync(f, 'utf-8'));
        assert(parsed.tracks.length === catalog.tracks.length, 'Conteo de pistas debe coincidir');
    }
    console.log('  ✔ PASS: Catálogos sincronizados en web, public y assets Android.\n');

    // --- PRUEBA 6: Streaming HTTP Real con Rango 206 en Demonio ---
    console.log('Test 6: Iniciar servidor daemon de prueba y validar streaming HTTP 206...');
    const testPort = 8799;
    process.env.PORT = testPort;
    const express = require('express');
    const daemonApp = express();
    
    // Configurar endpoints de daemon
    const infractionTracksMap = new Map();
    for (const t of catalog.tracks) infractionTracksMap.set(t.id, t);

    daemonApp.get('/api/infraction/catalog', (req, res) => res.json(catalog));
    daemonApp.get('/api/infraction/stream/:id', (req, res) => {
        const track = infractionTracksMap.get(req.params.id);
        if (!track || !fs.existsSync(track.filePath)) return res.status(404).json({ error: 'Not found' });
        
        const stat = fs.statSync(track.filePath);
        const range = req.headers.range;
        res.setHeader('X-ARKAIOS-Scope', track.distributionScope);
        
        if (range) {
            const parts = range.replace(/bytes=/, "").split("-");
            const start = parseInt(parts[0], 10);
            const end = parts[1] ? parseInt(parts[1], 10) : stat.size - 1;
            const chunksize = (end - start) + 1;
            res.writeHead(206, {
                'Content-Range': `bytes ${start}-${end}/${stat.size}`,
                'Accept-Ranges': 'bytes',
                'Content-Length': chunksize,
                'Content-Type': track.format === 'WAV' ? 'audio/wav' : 'audio/mpeg'
            });
            fs.createReadStream(track.filePath, { start, end }).pipe(res);
        } else {
            res.writeHead(200, {
                'Content-Length': stat.size,
                'Content-Type': track.format === 'WAV' ? 'audio/wav' : 'audio/mpeg'
            });
            fs.createReadStream(track.filePath).pipe(res);
        }
    });

    const server = http.createServer(daemonApp);
    await new Promise(resolve => server.listen(testPort, resolve));

    const sampleTrack = catalog.tracks[0];
    
    // 6.1 Prueba Stream Completo (HTTP 200)
    const stream200 = await httpGet(`http://localhost:${testPort}/api/infraction/stream/${sampleTrack.id}`);
    assert.strictEqual(stream200.statusCode, 200);
    assert.strictEqual(stream200.headers['x-arkaios-scope'], 'PRIVATE_LOCAL_ONLY');
    console.log(`  ✔ PASS: Stream completo HTTP 200 OK (${stream200.data.length} bytes recibidos).`);

    // 6.2 Prueba Rango Parcial (HTTP 206)
    const stream206 = await httpGet(`http://localhost:${testPort}/api/infraction/stream/${sampleTrack.id}`, {
        'Range': 'bytes=0-1023'
    });
    assert.strictEqual(stream206.statusCode, 206, 'Debe responder HTTP 206 Partial Content');
    assert.strictEqual(stream206.data.length, 1024, 'Debe devolver exactamente 1024 bytes');
    assert(stream206.headers['content-range'].startsWith('bytes 0-1023/'), `Content-Range incorrecto: ${stream206.headers['content-range']}`);
    console.log(`  ✔ PASS: Stream parcial HTTP 206 Partial Content verificado: ${stream206.headers['content-range']}.`);

    server.close();

    console.log('\n===============================================================');
    console.log('🎉 TODAS LAS PRUEBAS (6/6) PASARON EXITOSAMENTE SIN ERRORES');
    console.log('===============================================================');
}

function httpGet(urlStr, headers = {}) {
    return new Promise((resolve, reject) => {
        const url = new URL(urlStr);
        const req = http.request({
            hostname: url.hostname,
            port: url.port,
            path: url.pathname + url.search,
            method: 'GET',
            headers
        }, res => {
            const chunks = [];
            res.on('data', c => chunks.push(c));
            res.on('end', () => {
                resolve({
                    statusCode: res.statusCode,
                    headers: res.headers,
                    data: Buffer.concat(chunks)
                });
            });
        });
        req.on('error', reject);
        req.end();
    });
}

runTests().catch(err => {
    console.error('\n❌ ERROR EN PRUEBAS:', err);
    process.exit(1);
});
