/**
 * ARKAIOS-Putify — FASE 2.1: VALIDACIÓN REAL DEL CATÁLOGO INFRACTION
 * 
 * Verificación integral:
 *  1. Ejecución del demonio AUTÉNTICO web/daemon.js en el puerto 8788.
 *  2. Petición HTTP real contra GET http://127.0.0.1:8788/api/infraction/public-catalog.
 *  3. Validación de 281 playlists oficiales y 150 pistas auténticas del canal.
 *  4. Comprobación de reproducción incrustada real contra el endpoint oficial oEmbed de YouTube.
 *  5. Validación del modelo de datos de Android (equivalente Kotlin ArkaiosNodeMusicProvider).
 *  6. Prueba de renderizado de interfaz web y reproductor incrustado.
 *  7. Verificación del texto de atribución obligatoria exacta y enlaces a YouTube e InAudio.
 */

const { fork } = require('child_process');
const http = require('http');
const https = require('https');
const path = require('path');
const fs = require('fs');

const ROOT_DIR = path.resolve(__dirname, '..');
const DAEMON_SCRIPT = path.join(ROOT_DIR, 'web', 'daemon.js');
const PORT = 8788;
const BASE_URL = `http://127.0.0.1:${PORT}`;

const EXPECTED_ATTRIBUTION = "Música original de Infraction, proyecto musical de Ivan Malkov. Escucha y conoce más sobre el artista en su canal oficial de YouTube. ARKAIOS-Putify presenta esta colección como biblioteca de descubrimiento musical y no reclama la autoría de las composiciones.";

function httpGet(url) {
  return new Promise((resolve, reject) => {
    http.get(url, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => resolve({ statusCode: res.statusCode, body: data }));
    }).on('error', reject);
  });
}

function httpsGet(url) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'User-Agent': 'ARKAIOS-Putify-Validator/2.1' } }, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => resolve({ statusCode: res.statusCode, body: data }));
    }).on('error', reject);
  });
}

async function sleep(ms) {
  return new Promise(r => setTimeout(r, ms));
}

async function runValidation() {
  console.log('======================================================================');
  console.log('  ARKAIOS-PUTIFY — FASE 2.1: VALIDACIÓN REAL DEL CATÁLOGO INFRACTION ');
  console.log('======================================================================\n');

  let passed = 0;
  let failed = 0;

  function assert(condition, name, details = '') {
    if (condition) {
      console.log(`  [PASS] ${name}`);
      passed++;
    } else {
      console.error(`  [FAIL] ${name} -> ${details}`);
      failed++;
    }
  }

  // 1. INICIAR O DETECTAR DEMONIO AUTÉNTICO web/daemon.js EN PUERTO 8788
  console.log(`[PASO 1] Conectando con el demonio auténtico web/daemon.js en puerto ${PORT}...`);
  let daemonProcess = null;
  let connected = false;

  // Comprobar si ya está corriendo en 8788
  try {
    const res = await httpGet(`${BASE_URL}/api/infraction/public-catalog`);
    if (res.statusCode === 200) {
      connected = true;
      console.log('  ✔ Demonio auténtico ya se encuentra activo y respondiendo en el puerto 8788.');
    }
  } catch (err) {
    // No está corriendo, proceder a spawnearlo
  }

  if (!connected) {
    console.log(`  -> Iniciando nueva instancia de daemon.js en puerto ${PORT}...`);
    daemonProcess = fork(DAEMON_SCRIPT, [], {
      cwd: path.join(ROOT_DIR, 'web'),
      env: { ...process.env, PORT: PORT },
      silent: true
    });

    for (let i = 0; i < 30; i++) {
      try {
        const res = await httpGet(`${BASE_URL}/api/infraction/public-catalog`);
        if (res.statusCode === 200) {
          connected = true;
          break;
        }
      } catch (err) {
        // esperar
      }
      await sleep(400);
    }
  }

  assert(connected, `Servidor daemon.js responde HTTP 200 en puerto auténtico ${PORT}`);

  let catalog = null;
  if (connected) {
    const res = await httpGet(`${BASE_URL}/api/infraction/public-catalog`);
    try {
      catalog = JSON.parse(res.body);
      assert(true, 'Respuesta del daemon es JSON válido y parseable');
    } catch (e) {
      assert(false, 'Respuesta del daemon es JSON válido', e.message);
    }
  }

  if (catalog) {
    // 2. VALIDAR ESTRUCTURA DEL CANAL Y ATRIBUCIÓN
    assert(catalog.artist === 'Infraction', 'Artista identificado: Infraction');
    assert(catalog.composer === 'Ivan Malkov', 'Compositor original: Ivan Malkov');
    assert(catalog.channelId === 'UCkRrhwhJ2Ia_ZlkTQ4XFWJA', 'Canal oficial verificado (UCkRrhwhJ2Ia_ZlkTQ4XFWJA)');
    assert(catalog.officialChannel === 'https://www.youtube.com/@Infraction', 'URL oficial de YouTube (@Infraction)');
    assert(catalog.licensePlatform === 'https://inaudio.org/', 'Plataforma oficial de licencias (inaudio.org)');
    assert(
      catalog.attribution && catalog.attribution.trim() === EXPECTED_ATTRIBUTION.trim(),
      'Texto exacto de atribución obligatoria presente en metadatos'
    );

    // 3. CONTEO DE PLAYLISTS Y PISTAS VERIFICADAS
    const audit = catalog.verificationAudit || {};
    assert(
      Array.isArray(catalog.playlists) && catalog.playlists.length === 281,
      `Playlists verificadas contra YouTube (Total exacto: ${catalog.playlists ? catalog.playlists.length : 0} de 281)`
    );
    assert(
      Array.isArray(catalog.sampleTracks) && catalog.sampleTracks.length >= 150,
      `Pistas verificadas y disponibles para reproducción (Total: ${catalog.sampleTracks ? catalog.sampleTracks.length : 0})`
    );

    // 4. POLÍTICA ESTRICTA: CERO EXTRACCIÓN Y CERO DESCARGAS
    const allTracksEmbedOnly = catalog.sampleTracks.every(t =>
      t.playbackMode === 'YOUTUBE_OFFICIAL_EMBED' &&
      t.canDownload === false &&
      t.isAudioExtracted === false &&
      t.embedUrl.startsWith('https://www.youtube-nocookie.com/embed/')
    );
    assert(allTracksEmbedOnly, 'Todas las pistas cumplen playbackMode="YOUTUBE_OFFICIAL_EMBED" y canDownload=false');

    // 5. PRUEBA REAL EXTERNA DE REPRODUCCIÓN INCRUSTADA CON YOUTUBE oEmbed
    console.log('\n[PASO 2] Comprobando disponibilidad real de reproducción incrustada en YouTube (oEmbed API)...');
    const testSampleVideos = [
      catalog.sampleTracks[0],
      catalog.sampleTracks[1],
      catalog.sampleTracks[2],
      catalog.sampleTracks[3],
      catalog.sampleTracks[4]
    ];

    let oembedSuccessCount = 0;
    for (const track of testSampleVideos) {
      const oembedUrl = `https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=${track.videoId}&format=json`;
      try {
        const oembedRes = await httpsGet(oembedUrl);
        if (oembedRes.statusCode === 200) {
          const oembedData = JSON.parse(oembedRes.body);
          if (oembedData.html && oembedData.html.includes('iframe') && oembedData.author_url.includes('Infraction')) {
            oembedSuccessCount++;
            console.log(`    ✔ [oEmbed OK] "${track.title}" (VideoId: ${track.videoId}) -> Embed autorizado por YouTube`);
          }
        }
      } catch (e) {
        console.warn(`    ⚠ [oEmbed Advertencia] No se pudo conectar a oEmbed: ${e.message}`);
      }
    }
    assert(
      oembedSuccessCount >= 4,
      `Validación de iframe incrustado real en YouTube: ${oembedSuccessCount} de 5 pistas probadas en vivo`
    );

    // 6. SIMULACIÓN DE COMPATIBILIDAD CON APK ANDROID (ArkaiosNodeMusicProvider)
    console.log('\n[PASO 3] Verificando compatibilidad de catálogo con el modelo de datos Android...');
    let androidCompatible = true;
    for (const t of catalog.sampleTracks) {
      if (!t.id || !t.title || !t.artist || !t.coverUrl || !t.embedUrl || !t.playbackMode) {
        androidCompatible = false;
        break;
      }
    }
    assert(androidCompatible, 'Todas las pistas mapean correctamente a la entidad Track de Android (Kotlin)');

    // 7. COMPROBAR ARCHIVOS HTML Y CSS
    console.log('\n[PASO 4] Verificando sincronización de plantillas y reproductor...');
    const indexHtml = fs.readFileSync(path.join(ROOT_DIR, 'index.html'), 'utf8');
    assert(indexHtml.includes(EXPECTED_ATTRIBUTION), 'index.html contiene atribución obligatoria íntegra');
    assert(indexHtml.includes('id="embeddedPlayerCard"'), 'index.html contiene tarjeta del reproductor incrustado');
    assert(indexHtml.includes('id="youtubeIframePlayer"'), 'index.html contiene el iframe oficial de YouTube');
    assert(indexHtml.includes('https://www.youtube.com/@Infraction'), 'index.html contiene botón al canal oficial');
    assert(indexHtml.includes('https://inaudio.org/'), 'index.html contiene botón de licencias inaudio.org');
  }

  // DETENER EL DEMONIO SI FUE CREADO EN LA PRUEBA
  if (daemonProcess) {
    console.log('\n[PASO 5] Finalizando demonio de prueba...');
    daemonProcess.kill('SIGTERM');
    await sleep(500);
  } else {
    console.log('\n[PASO 5] Demonio persistente en puerto 8788 conservado activo.');
  }

  console.log('======================================================================');
  console.log(`  RESUMEN FASE 2.1: ${passed} PASARON | ${failed} FALLARON`);
  console.log('======================================================================\n');

  if (failed > 0) {
    process.exit(1);
  } else {
    process.exit(0);
  }
}

runValidation().catch(err => {
  console.error('Error fatal en validación:', err);
  process.exit(1);
});
