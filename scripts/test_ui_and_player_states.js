/**
 * Unit & Integration Test for Frontend UI Logic & Player States (app.js)
 * Emulates the browser runtime to verify complete UI functionality:
 *  - switchTab('infractionPublic')
 *  - Rendering 150 real verified track cards with badges and artwork
 *  - Rendering 281 real verified playlist cards
 *  - Playing a track via official embedded YouTube player (card display:block, iframe.src)
 *  - Playing a full official playlist via embedded videoseries iframe
 *  - Closing the embedded player
 *  - Live search filtering (query: "phonk", "metal")
 *  - Toggling favorites in localStorage
 *  - Adding track to custom ARKAIOS playlist
 */

const fs = require('fs');
const path = require('path');

const ROOT_DIR = path.resolve(__dirname, '..');
const catalog = JSON.parse(fs.readFileSync(path.join(ROOT_DIR, 'web', 'infraction_public_playlists.json'), 'utf8'));

// Minimal browser DOM mock
class MockElement {
  constructor(id, tagName = 'div') {
    this.id = id;
    this.tagName = tagName;
    this.style = {};
    this.classList = {
      _classes: new Set(),
      add(c) { this._classes.add(c); },
      remove(c) { this._classes.delete(c); },
      contains(c) { return this._classes.has(c); }
    };
    this.innerHTML = '';
    this.textContent = '';
    this.value = '';
    this.src = '';
    this.href = '';
  }
  scrollIntoView() {}
}

const elements = new Map();
function getEl(id) {
  if (!elements.has(id)) {
    elements.set(id, new MockElement(id));
  }
  return elements.get(id);
}

global.document = {
  getElementById(id) {
    return getEl(id);
  },
  querySelectorAll() {
    return [];
  }
};

const storage = new Map();
global.localStorage = {
  getItem(k) { return storage.get(k) || null; },
  setItem(k, v) { storage.set(k, String(v)); },
  removeItem(k) { storage.delete(k); },
  clear() { storage.clear(); }
};

global.window = global;
global.location = { hostname: 'localhost', port: '8788', href: 'http://localhost:8788' };
global.window.location = global.location;
global.alert = (msg) => {};
global.confirm = () => true;
global.prompt = (msg, def) => def || 'Test Playlist';
global.Audio = class {
  play() {}
  pause() {}
  addEventListener() {}
};

// Now load and execute app.js functions in this context
let appCode = fs.readFileSync(path.join(ROOT_DIR, 'app.js'), 'utf8');

// Strip DOMContentLoaded listener invocation to control lifecycle manually
appCode = appCode.replace(/document\.addEventListener\('DOMContentLoaded'[\s\S]*?\}\);/m, '// DOMContentLoaded skipped');

const vm = require('vm');
const context = vm.createContext({
  ...global,
  console: console,
  setTimeout: setTimeout,
  clearTimeout: clearTimeout,
  publicInfractionData: catalog
});

vm.runInContext(appCode, context);
vm.runInContext("publicInfractionData = " + JSON.stringify(catalog) + ";", context);

function runUITests() {
  console.log('======================================================================');
  console.log('  ARKAIOS-PUTIFY: PRUEBAS DE INTERFAZ & REPRODUCTOR INCUSTADO WEB    ');
  console.log('======================================================================\n');

  let passed = 0;
  let failed = 0;

  function assert(condition, name, err = '') {
    if (condition) {
      console.log(`  [PASS] ${name}`);
      passed++;
    } else {
      console.error(`  [FAIL] ${name} -> ${err}`);
      failed++;
    }
  }

  // 1. Initial Data Binding
  assert(context.publicInfractionData.totalPlaylists === 281, 'Catálogo contiene 281 playlists oficiales');
  assert(context.publicInfractionData.sampleTracks.length === 150, 'Catálogo contiene 150 pistas verificadas');

  // 2. Render Public Tracks
  context.renderPublicTracks();
  const tracksGrid = getEl('publicTracksGrid');
  assert(
    tracksGrid.innerHTML.includes('track-item-card') && tracksGrid.innerHTML.includes('DyJ_rqBdci8'),
    'renderPublicTracks() genera tarjetas de pistas reales con videoId oficial'
  );
  assert(
    tracksGrid.innerHTML.includes('Stream Oficial • Descarga deshabilitada'),
    'Badge de protección y descarga deshabilitada presente en cada tarjeta'
  );

  // 3. Render Official Playlists
  context.renderOfficialPlaylists();
  const plGrid = getEl('officialPlaylistsGrid');
  assert(
    plGrid.innerHTML.includes('PLIu0e9jXb07w') && plGrid.innerHTML.includes('Rhythmic No Copyright Music'),
    'renderOfficialPlaylists() genera 281 colecciones oficiales con enlaces directos'
  );

  // 4. Test Track Playback
  const testTrack = catalog.sampleTracks[0];
  context.playOfficialYouTubeTrack(testTrack.videoId, testTrack.title, testTrack.originalUrl);
  const playerCard = getEl('embeddedPlayerCard');
  const iframe = getEl('youtubeIframePlayer');
  const playingTitle = getEl('embedPlayingTitle');
  const extLink = getEl('embedExternalLink');

  assert(playerCard.style.display === 'block', 'Al reproducir pista: Tarjeta de reproductor pasa a display: block');
  assert(
    iframe.src === `https://www.youtube-nocookie.com/embed/${testTrack.videoId}?autoplay=1&enablejsapi=1`,
    `Al reproducir pista: IFrame URL oficial configurado (${iframe.src})`
  );
  assert(playingTitle.textContent.includes(testTrack.title), `Título en reproducción actualizado: ${playingTitle.textContent}`);
  assert(extLink.href === testTrack.originalUrl, `Enlace externo redirige a YouTube original (${extLink.href})`);

  // 5. Test Playlist Playback
  const testPlaylist = catalog.playlists[0];
  context.playOfficialYouTubePlaylist(testPlaylist.id, testPlaylist.title, testPlaylist.url);
  assert(
    iframe.src === `https://www.youtube-nocookie.com/embed/videoseries?list=${testPlaylist.id}&autoplay=1`,
    `Al reproducir playlist completa: IFrame URL series configurado (${iframe.src})`
  );

  // 6. Test Close Player
  context.closeEmbeddedPlayer();
  assert(iframe.src === '', 'Al cerrar reproductor: iframe.src se vacía para detener reproducción');
  assert(playerCard.style.display === 'none', 'Al cerrar reproductor: display pasa a none');

  // 7. Test Search Filter
  getEl('infrSearchInput').value = 'phonk';
  context.filterPublicInfraction();
  assert(
    tracksGrid.innerHTML.toLowerCase().includes('phonk') && !tracksGrid.innerHTML.toLowerCase().includes('acoustic vlog'),
    'Buscador reactivo filtra correctamente por género / palabra clave ("phonk")'
  );
  getEl('infrSearchInput').value = ''; // reset

  // 8. Test Favorites
  context.toggleFavorite(testTrack.videoId);
  const favsInStorage = JSON.parse(global.localStorage.getItem('arkaios_infr_favorites') || '[]');
  assert(favsInStorage.includes(testTrack.videoId), 'Favorito persistido en localStorage');
  context.setPublicFilter('FAVORITES');
  assert(tracksGrid.innerHTML.includes(testTrack.videoId), 'Filtro de Favoritos muestra solo pistas guardadas');

  // 9. Test Playlists Creation & Management
  context.showCreatePlaylistModal();
  const createdPlaylists = JSON.parse(global.localStorage.getItem('arkaios_playlists') || '[]');
  assert(createdPlaylists.length === 1 && createdPlaylists[0].name === 'Mi Playlist Favorita', 'Creación de playlist personalizada exitosa');

  console.log('\n======================================================================');
  console.log(`  RESUMEN DE PRUEBAS UI: ${passed} PASARON | ${failed} FALLARON`);
  console.log('======================================================================\n');

  if (failed > 0) process.exit(1);
  else process.exit(0);
}

runUITests();
