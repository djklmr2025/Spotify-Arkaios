/**
 * Automated Verification Suite: ARKAIOS-Putify Public Infraction Library
 * Verifies compliance with:
 *  1. Zero MP3 extraction / no download policy (playbackMode: YOUTUBE_OFFICIAL_EMBED).
 *  2. Official YouTube channel and InAudio licensing links.
 *  3. Exact mandatory attribution to Ivan Malkov / Infraction.
 *  4. JSON catalog integrity (playlists count, track samples, embed URLs).
 *  5. Web daemon API endpoint /api/infraction/public-catalog.
 *  6. HTML template sync and sidebar navigation integrity.
 */

const fs = require('fs');
const path = require('path');
const http = require('http');

const EXPECTED_ATTRIBUTION = "Música original de Infraction, proyecto musical de Ivan Malkov. Escucha y conoce más sobre el artista en su canal oficial de YouTube. ARKAIOS-Putify presenta esta colección como biblioteca de descubrimiento musical y no reclama la autoría de las composiciones.";
const ROOT_DIR = path.resolve(__dirname, '..');

async function runTests() {
  console.log('===============================================================');
  console.log('  ARKAIOS-PUTIFY: VERIFICACIÓN BIBLIOTECA PÚBLICA INFRACTION  ');
  console.log('===============================================================\n');

  let passed = 0;
  let failed = 0;

  function assert(condition, testName, errorDetails = '') {
    if (condition) {
      console.log(`  [PASS] ${testName}`);
      passed++;
    } else {
      console.error(`  [FAIL] ${testName} -> ${errorDetails}`);
      failed++;
    }
  }

  // TEST 1: Check infraction_public_playlists.json exists and is valid
  const catalogPath = path.join(ROOT_DIR, 'web', 'infraction_public_playlists.json');
  assert(fs.existsSync(catalogPath), 'Catálogo JSON existe en web/infraction_public_playlists.json');

  let catalog = null;
  try {
    catalog = JSON.parse(fs.readFileSync(catalogPath, 'utf8'));
    assert(true, 'Catálogo JSON es sintácticamente válido');
  } catch (err) {
    assert(false, 'Catálogo JSON es sintácticamente válido', err.message);
  }

  if (catalog) {
    // TEST 2: Attribution text in catalog
    assert(
      catalog.attribution && catalog.attribution.trim() === EXPECTED_ATTRIBUTION.trim(),
      'Atribución obligatoria exacta en metadatos del catálogo JSON'
    );

    // TEST 3: External URLs
    const hasChannel = (catalog.officialChannel === 'https://www.youtube.com/@Infraction' || catalog.channelUrl === 'https://www.youtube.com/@Infraction');
    assert(
      hasChannel,
      'Enlace oficial al canal de YouTube (https://www.youtube.com/@Infraction)'
    );

    const hasLicense = (catalog.licensePlatform === 'https://inaudio.org/' || catalog.licensePlatformUrl === 'https://inaudio.org/');
    assert(
      hasLicense,
      'Enlace oficial a la plataforma de licencias InAudio (https://inaudio.org/)'
    );

    // TEST 4: Official Playlists count
    assert(
      Array.isArray(catalog.playlists) && catalog.playlists.length >= 200,
      `Playlists públicas indexadas (Total: ${catalog.playlists ? catalog.playlists.length : 0})`
    );

    // TEST 5: Sample Tracks count & policy
    assert(
      Array.isArray(catalog.sampleTracks) && catalog.sampleTracks.length > 0,
      `Pistas públicas de muestra indexadas (Total: ${catalog.sampleTracks ? catalog.sampleTracks.length : 0})`
    );

    // TEST 6: Check strict YT-embed and no-download flags on all tracks
    const allTracksCompliant = catalog.sampleTracks.every(t =>
      t.playbackMode === 'YOUTUBE_OFFICIAL_EMBED' &&
      t.canDownload === false &&
      t.isAudioExtracted === false &&
      t.embedUrl && t.embedUrl.startsWith('https://www.youtube-nocookie.com/embed/')
    );
    assert(
      allTracksCompliant,
      'Todas las pistas tienen playbackMode="YOUTUBE_OFFICIAL_EMBED", canDownload=false y embedUrl oficial'
    );
  }

  // TEST 7: Check index.html files have attribution and buttons
  const htmlFiles = [
    path.join(ROOT_DIR, 'index.html'),
    path.join(ROOT_DIR, 'web', 'public', 'index.html'),
    path.join(ROOT_DIR, 'app', 'src', 'main', 'assets', 'web', 'index.html')
  ];

  htmlFiles.forEach(file => {
    const relPath = path.relative(ROOT_DIR, file);
    if (!fs.existsSync(file)) {
      assert(false, `Archivo HTML existe: ${relPath}`);
      return;
    }
    const content = fs.readFileSync(file, 'utf8');
    assert(
      content.includes(EXPECTED_ATTRIBUTION),
      `Atribución obligatoria presente en ${relPath}`
    );
    assert(
      content.includes('https://www.youtube.com/@Infraction') &&
      content.includes('https://inaudio.org/'),
      `Botones de canal oficial y de licencia inaudio.org presentes en ${relPath}`
    );
    assert(
      content.includes('id="embeddedPlayerCard"') && content.includes('id="youtubeIframePlayer"'),
      `Contenedor para reproductor oficial incrustado YouTube en ${relPath}`
    );
  });

  // TEST 8: Verify daemon.js defines /api/infraction/public-catalog
  const daemonPath = path.join(ROOT_DIR, 'web', 'daemon.js');
  if (fs.existsSync(daemonPath)) {
    const daemonContent = fs.readFileSync(daemonPath, 'utf8');
    assert(
      daemonContent.includes('/api/infraction/public-catalog'),
      'Ruta GET /api/infraction/public-catalog configurada en daemon.js'
    );
  } else {
    assert(false, 'daemon.js existe');
  }

  // TEST 9: Android Kotlin provider checks
  const kotlinPath = path.join(ROOT_DIR, 'app', 'src', 'main', 'java', 'com', 'example', 'data', 'repository', 'ArkaiosNodeMusicProvider.kt');
  if (fs.existsSync(kotlinPath)) {
    const kotlinContent = fs.readFileSync(kotlinPath, 'utf8');
    assert(
      kotlinContent.includes('getPublicInfractionTracks'),
      'Método getPublicInfractionTracks() implementado en ArkaiosNodeMusicProvider.kt'
    );
    assert(
      kotlinContent.includes('/api/infraction/public-catalog'),
      'Endpoint /api/infraction/public-catalog invocado por el repositorio Android'
    );
  } else {
    assert(false, 'ArkaiosNodeMusicProvider.kt existe');
  }

  // TEST 10: Daemon live endpoint test against authentic daemon.js on port 8788
  console.log('\n  [INFO] Verificando daemon auténtico en puerto 8788...');
  try {
    http.get('http://127.0.0.1:8788/api/infraction/public-catalog', (res) => {
      let raw = '';
      res.on('data', chunk => raw += chunk);
      res.on('end', () => {
        try {
          const data = JSON.parse(raw);
          assert(
            res.statusCode === 200 && Array.isArray(data.playlists) && data.playlists.length === 281,
            `Endpoint auténtico /api/infraction/public-catalog responde HTTP 200 con 281 playlists reales`
          );
          assert(
            Array.isArray(data.sampleTracks) && data.sampleTracks.length >= 150,
            `Endpoint auténtico devuelve ${data.sampleTracks ? data.sampleTracks.length : 0} pistas verificadas`
          );
          summarize();
        } catch (e) {
          assert(false, 'Respuesta del servidor daemon es JSON válido', e.message);
          summarize();
        }
      });
    }).on('error', (err) => {
      assert(false, 'Llamada HTTP al demonio auténtico en puerto 8788', err.message);
      summarize();
    });
  } catch (err) {
    assert(false, 'Excepción en prueba del demonio auténtico', err.message);
    summarize();
  }

  function summarize() {
    console.log('\n===============================================================');
    console.log(`  RESUMEN: ${passed} PASARON | ${failed} FALLARON`);
    console.log('===============================================================\n');
    if (failed > 0) {
      process.exit(1);
    } else {
      process.exit(0);
    }
  }
}

runTests();
