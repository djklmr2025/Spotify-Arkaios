# Spotify-Arkaios v2.0.0 (Versión Oficial & Final) 🎵⚡

[![Release v2.0.0](https://img.shields.io/badge/Release-v2.0.0%20Official-06B6D4?style=for-the-badge&logo=android)](https://github.com/djklmr2025/Spotify-Arkaios/releases/tag/v2.0.0)
[![Plataforma Web](https://img.shields.io/badge/Web%20Platform-Vercel%20Live-22C55E?style=for-the-badge&logo=vercel)](https://spotify-arkaios.vercel.app)
[![Licencia](https://img.shields.io/badge/License-MIT%20Free-purple?style=for-the-badge)](https://github.com/djklmr2025/Spotify-Arkaios)

**Spotify-Arkaios** es un ecosistema inteligente y reproductor de música nativo en **Kotlin (Jetpack Compose)** para Android, acompañado de su plataforma web unificada. Ofrece calidad de audio sin pérdidas, búsqueda multi-fuente en tiempo real, radio FM en vivo 24/7 y **descargas de audio originales sin cifrar**.

---

## 📥 Enlaces Oficiales de Descarga & Plataforma Web

- 📱 **Descargar APK v2.0.0 (Servidor Directo GitHub)**: [app-debug.apk (24.38 MB)](https://github.com/djklmr2025/Spotify-Arkaios/releases/download/v2.0.0/app-debug.apk)
- ☁ **Mirror de Descarga (Google Drive)**: [Descargar Servidor Externo (Opción 2)](https://drive.google.com/uc?id=1qx7US8ceQdLYn0ydELmDMqWcorxgdw55)
- 🌐 **Página Web Oficial de Descarga (Vercel)**: [https://spotify-arkaios.vercel.app](https://spotify-arkaios.vercel.app)
- ⚡ **Integración Web Engine (DJ Intelligence)**: [https://dj-intelligence-engine.vercel.app/spotify](https://dj-intelligence-engine.vercel.app/spotify)

---

## 🌟 Novedades y Cambios de la Versión Final v2.0.0

1. **Descargas Directas Sin Cifrado DRM**:
   - Se eliminó completamente el motor de encriptación `.arkcache`.
   - Las canciones descargadas se guardan en **formato original sin cifrar** (`.mp3`, `.m4a`, `.flac`) en la carpeta pública del teléfono (`Música/SpotifyArkaios`), totalmente accesibles desde cualquier reproductor externo o computadora.

2. **Motor de Búsqueda Multi-Fuente Resiliente (YouTube #1)**:
   - **1.º Lugar de Prioridad**: **YouTube & YouTube Music**, adaptando el motor ultra-rápido de `DJ_Assistant` con 15 instancias de APIs Piped/Invidious y un **scraper de búsqueda en tiempo real de YouTube HTML** como fallback definitivo.
   - **2.º Lugar de Prioridad**: **TIDAL Hi-Fi Master** (Stream sin pérdidas).
   - **Fuentes Adicionales**: SoundCloud / Audius, Jamendo Hi-Fi, Archive.org y enlaces directos de Google Drive.

3. **App 100% Gratuita e Ilimitada**:
   - Se retiraron totalmente la tienda AMR Pay, carteras de tokens, concursos de karaoke, cuentas premium restringidas y cuotas de almacenamiento.
   - Acceso total e ilimitado a todas las funciones sin costo ni registros.

4. **Radio FM & Estaciones en Vivo 24/7**:
   - Emisoras en vivo sin comerciales (Reggaeton Flow, Exa FM, SomaFM Groove Salad, Synthwave 80s).

5. **Corrección de Detección de Versión**:
   - `versionCode` actualizado a `200` y `versionName` a `"2.0.0"`.
   - `AppUpdateManager` sincronizado con la versión `v2.0.0` para evitar avisos falsos de actualización.

---

## 📱 Especificaciones Técnicas (Android APK)

- **Lenguaje & UI**: Kotlin 100% Nativo • Jetpack Compose • Material Design 3.
- **SDK Objetivo**: `compileSdk = 36`, `targetSdk = 36`, `minSdk = 24` (Android 8.0 Oreo en adelante).
- **Control de Audio**: `AudioPlayerEngine` en segundo plano con controles multimedia en la barra de notificaciones.
- **Base de Datos Local**: SQLite / Room (`TrackDao`, `PlaylistDao`).

---

## 🛠️ Estructura del Proyecto

```
app/src/main/java/com/example/
├── data/
│   ├── crypto/             # ArkaiosOfflineCryptoEngine (Passthrough sin cifrar)
│   ├── local/              # Base de datos Room (TrackEntity, AppDatabase, TrackDao)
│   ├── model/              # Modelos de datos (Track, PlaybackState, Playlist)
│   ├── repository/         # Repositorios (MusicRepository, YouTubeMusicProvider, DownloadRepository)
│   └── update/             # AppUpdateManager (Verificador v2.0.0)
├── player/                 # Motor de reproducción de audio (AudioPlayerEngine)
├── ui/
│   ├── components/         # Componentes UI (FullPlayerSheet, BottomNav, AddToPlaylistDialog)
│   ├── screens/            # Pantallas (HomeScreen, SearchScreen, LibraryScreen)
│   └── MainViewModel.kt    # ViewModel y Estado MVVM
└── MainActivity.kt         # Punto de entrada de la aplicación
```

---

## 🚀 Compilación Manual del APK

Si deseas compilar la APK desde tu equipo local:

```bash
# 1. Ejecutar el script compilador optimizado con JDK 21
run_build.bat

# 2. O compilar manualmente mediante Gradle Wrapper:
cmd.exe /c "set \"JAVA_HOME=C:\Android\Android Studio\jbr\" && gradle.bat assembleDebug"
```

El APK resultante estará listo en:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 Licencia

Este proyecto está bajo la Licencia **MIT** — Libre para uso personal y distribución. © 2026 Ecosistema ARKAIOS.
