# ARKAIOS-TIFY (Arkaios Music & Hi-Fi Lossless Streaming) 🎵⚡




https://github.com/user-attachments/assets/801ac834-7d53-4516-a84d-b361ec39d68a





**ArkaiosTify** es una plataforma y aplicación móvil de streaming musical en alta fidelidad y estudio construida en **Kotlin** y **Jetpack Compose** para Android. Combina la fidelidad de TIDAL/FLAC con un motor multi-fuente universal (YouTube Music, SoundCloud/Audius, Jamendo Hi-Fi, Google Drive 5TB y Servidores Subsonic/Navidrome), complementada con la pasarela de pagos descentralizada y fiat **ARKAIOS Pay & PayPal.Me**.

---

## 🌟 Características Principales

1. **Motor de Búsqueda y Streaming Multi-Fuente (Canciones Completas sin Cortes)**:
   - **YouTube Music Engine**: Streams continuos de alta tasa de bits en formatos AAC/Opus/M4A.
   - **SoundCloud / Audius Network**: Más de 10 millones de pistas y remixes en MP3 a 320 kbps.
   - **Jamendo Hi-Fi Studio**: Pistas completas sin restricciones.
   - **Google Drive 5TB & Local Cloud**: Detección y reproducción de archivos de audio directos desde enlaces o almacenamiento del dispositivo.
   - **TIDAL Hi-Res Lossless**: Calidades seleccionables (Normal, High 320k, HiFi Lossless 1411k, Master FLAC 24-bit/192kHz).

2. **ARKAIOS Pay & Checkout Híbrido**:
   - Integración nativa de **PayPal.Me** directo (`paypal.me/klmroffcialchannel`).
   - Enlace y validación con el **Portal Web ARKAIOS Pay**: `https://arkaios-puterlab-nexus-ide.vercel.app/arkaios-pay-demo`.
   - Sistema de Tokenomics **AMR** (Tokens de Cartera, Membresías VIP, Pro Studio y Master).

3. **Arquitectura y Reproductor**:
   - **Jetpack Compose & Material 3**: Interfaz oscura cyberpunk/neon con paleta Cyber Cyan (`#06B6D4`) y Arkaios Gold (`#EAB308`).
   - **AudioPlayerEngine**: Reproductor en segundo plano con soporte para metadata enriquecida, carátulas y reproducción en bucle/aleatoria.
   - **Gestor de Descargas Offline**: Persistencia y almacenamiento local en SQLite (Room Database).
   - **Creator Studio**: Panel para subir, organizar y publicar pistas con metadata personalizada.

---

## 📱 Guía de Construcción y Generación del APK (`.apk`)

### 1. Generación Directa en AI Studio / Antigravity
Para descargar el APK directamente desde el panel de AI Studio:
1. Dirígete a la barra superior o menú de configuración del proyecto (**Settings** / **Export**).
2. Selecciona **Download APK** o **Build AAB/APK**.
3. El sistema compilará el paquete optimizado `app-debug.apk` o `app-release.apk` listo para transferir o instalar en cualquier dispositivo Android.

---

### 2. Construcción Manual vía Terminal (Gradle)

#### Requisitos Previos:
- **JDK**: Java 17 o superior.
- **Android SDK**: `compileSdk = 35`, `minSdk = 24`.
- **Gradle**: 8.x con soporte de Kotlin DSL (`.gradle.kts`).

#### Comandos de Construcción:

```bash
# 1. Compilar y generar APK de desarrollo (Debug)
gradle :app:assembleDebug

# El archivo generado se ubicará en:
# app/build/outputs/apk/debug/app-debug.apk

# 2. Compilar APK optimizado para Producción (Release)
gradle :app:assembleRelease

# El archivo generado se ubicará en:
# app/build/outputs/apk/release/app-release-unsigned.apk
```

#### Firma del APK de Release (Opcional):
```bash
# Alinear el APK
zipalign -v -p 4 app/build/outputs/apk/release/app-release-unsigned.apk app-release-aligned.apk

# Firmar con tu Keystore de producción
apksigner sign --ks tu_keystore.jks --out arkaios-tify-release.apk app-release-aligned.apk
```

---

## 🛠️ Estructura del Proyecto

```
app/src/main/java/com/example/
├── data/
│   ├── local/              # Base de datos Room (TrackEntity, AppDatabase, TrackDao)
│   ├── model/              # Modelos de datos (Track, PlaybackState, User, Download)
│   ├── repository/         # Repositorios (MusicRepository, AmrWalletRepository, DownloadRepository)
│   └── tidal/              # Motor de APIs (TidalApiService, MusicSourceFilter, Config)
├── player/                 # Motor de reproducción de audio y background services (AudioPlayerEngine)
├── ui/
│   ├── components/         # Modales y componentes (ArkaiosPayModal, MiniPlayer, BottomNav, TrackItemRow)
│   ├── screens/            # Pantallas (HomeScreen, SearchScreen, LibraryScreen, AmrStoreScreen, etc.)
│   ├── theme/              # Esquema de color Cyberpunk M3 (Theme, Color, Type)
│   └── MainViewModel.kt    # StateFlow y arquitectura MVVM reactiva
└── MainActivity.kt         # Punto de entrada principal y navegación
```

---

## 🚀 Transferencia y Entrega a Antigravity

El proyecto se encuentra en estado **100% verde y compilado con éxito**. Todos los servicios, pasarelas de pago (PayPal & ARKAIOS Pay) y el motor multi-fuente de audio continuo están plenamente enlazados y validados.
