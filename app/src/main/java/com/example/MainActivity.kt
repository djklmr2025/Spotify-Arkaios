package com.example

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private lateinit var customViewContainer: FrameLayout
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    companion object {
        private const val FILE_CHOOSER_REQUEST_CODE = 1001
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Forzar y fijar modo horizontal (Landscape Spotify Player)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        // Activar aceleración por hardware
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        // Evitar que la pantalla se apague mientras se reproduce música
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Modo inmersivo para pantalla completa horizontal
        setupImmersiveMode()

        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#090D16"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        webView = WebView(this).apply {
            setBackgroundColor(Color.parseColor("#090D16"))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        customViewContainer = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            visibility = View.GONE
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        rootLayout.addView(webView)
        rootLayout.addView(customViewContainer)
        setContentView(rootLayout)

        configureWebView()

        // Cargar el reproductor completo bajo origen HTTPS virtual para habilitar embeds de YouTube sin Error 153
        webView.loadUrl("https://spotify-arkaios.vercel.app/index.html")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (customView != null) {
            hideCustomView()
        } else if (this::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    private fun setupImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        val settings: WebSettings = webView.settings

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false

        // Permisos para cargar archivos y recursos locales
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.allowFileAccessFromFileURLs = true
        settings.allowUniversalAccessFromFileURLs = true

        // Viewport y escala horizontal
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        settings.displayZoomControls = false

        // Cache y rendimiento
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        // Ajustar User-Agent quitando marca 'wv' para evitar bloqueos del reproductor embebido de YouTube
        val defaultUa = settings.userAgentString ?: ""
        settings.userAgentString = defaultUa
            .replace("; wv)", ")")
            .replace("; wv", "")
            .replace("Version/4.0 ", "")

        // Habilitar cookies de terceros para el reproductor IFrame de YouTube
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true)
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val uri = request?.url ?: return null
                val host = uri.host?.lowercase() ?: ""
                val path = uri.path ?: "/"

                if (host == "spotify-arkaios.vercel.app") {
                    // Permitir que las llamadas al proxy serverless (/api/...) salgan a internet
                    if (path.startsWith("/api/")) {
                        return null
                    }

                    // Servir pistas locales de /sdcard/ si existen en el dispositivo
                    if (path.startsWith("/local-sdcard/")) {
                        val relSdPath = path.removePrefix("/local-sdcard/")
                        val sdFile = File("/sdcard/$relSdPath")
                        return if (sdFile.exists() && sdFile.canRead()) {
                            WebResourceResponse(
                                getMimeType(relSdPath),
                                null,
                                200,
                                "OK",
                                mapOf("Access-Control-Allow-Origin" to "*"),
                                FileInputStream(sdFile)
                            )
                        } else {
                            WebResourceResponse(
                                "text/plain",
                                "UTF-8",
                                404,
                                "Not Found",
                                mapOf("Access-Control-Allow-Origin" to "*"),
                                ByteArrayInputStream(ByteArray(0))
                            )
                        }
                    }

                    val cleanPath = path.trimStart('/').ifEmpty { "index.html" }
                    val assetStream: InputStream? = try {
                        assets.open("web/$cleanPath")
                    } catch (e1: Exception) {
                        try {
                            assets.open(cleanPath)
                        } catch (e2: Exception) {
                            null
                        }
                    }

                    if (assetStream != null) {
                        val mimeType = getMimeType(cleanPath)
                        return WebResourceResponse(
                            mimeType,
                            "UTF-8",
                            200,
                            "OK",
                            mapOf(
                                "Access-Control-Allow-Origin" to "*",
                                "Cache-Control" to "no-cache"
                            ),
                            assetStream
                        )
                    }
                }

                return super.shouldInterceptRequest(view, request)
            }

            private fun getMimeType(filePath: String): String {
                val lower = filePath.lowercase()
                return when {
                    lower.endsWith(".html") -> "text/html"
                    lower.endsWith(".js") -> "application/javascript"
                    lower.endsWith(".css") -> "text/css"
                    lower.endsWith(".json") -> "application/json"
                    lower.endsWith(".svg") -> "image/svg+xml"
                    lower.endsWith(".png") -> "image/png"
                    lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
                    lower.endsWith(".webp") -> "image/webp"
                    lower.endsWith(".mp3") -> "audio/mpeg"
                    lower.endsWith(".flac") -> "audio/flac"
                    lower.endsWith(".m4a") -> "audio/mp4"
                    lower.endsWith(".woff2") -> "font/woff2"
                    else -> "application/octet-stream"
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                // No interceptar navegaciones internas dentro de iframes (ej. reproductor YouTube)
                if (request.isForMainFrame == false) {
                    return false
                }
                return handleUrl(url)
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url == null) return false
                return handleUrl(url)
            }

            private fun handleUrl(url: String): Boolean {
                // Mantener dentro del WebView: origen virtual local, assets locales y embeds de YouTube
                if (url.startsWith("https://spotify-arkaios.vercel.app") ||
                    url.startsWith("file:///") ||
                    url.contains("youtube.com/embed") ||
                    url.contains("youtube-nocookie.com/embed") ||
                    url.contains("localhost:8788")
                ) {
                    return false
                }

                // Abrir enlaces externos (como canal de YouTube o inaudio.org) en el navegador del sistema
                return try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    true
                } catch (e: Exception) {
                    false
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customView = view
                customViewCallback = callback
                customViewContainer.addView(view)
                customViewContainer.visibility = View.VISIBLE
                webView.visibility = View.GONE
            }

            override fun onHideCustomView() {
                hideCustomView()
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback?.onReceiveValue(null)
                this@MainActivity.filePathCallback = filePathCallback

                return try {
                    val acceptTypes = fileChooserParams?.acceptTypes ?: emptyArray()
                    val isM3u = acceptTypes.any { it.contains("m3u", ignoreCase = true) || it.contains("mpegurl", ignoreCase = true) }
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = if (isM3u) "*/*" else "audio/*"
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    }
                    startActivityForResult(Intent.createChooser(intent, "Seleccionar multimedia o playlist"), FILE_CHOOSER_REQUEST_CODE)
                    true
                } catch (e: Exception) {
                    this@MainActivity.filePathCallback = null
                    false
                }
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                android.util.Log.d("SpotifyArkaiosWeb", "[${consoleMessage?.messageLevel()}] ${consoleMessage?.message()} (line: ${consoleMessage?.lineNumber()})")
                return true
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            val callback = filePathCallback
            filePathCallback = null
            if (resultCode != RESULT_OK || data == null) {
                callback?.onReceiveValue(null)
                return
            }
            val results = mutableListOf<Uri>()
            val clipData = data.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    clipData.getItemAt(i)?.uri?.let { results.add(it) }
                }
            } else {
                data.data?.let { results.add(it) }
            }
            callback?.onReceiveValue(if (results.isNotEmpty()) results.toTypedArray() else null)
        }
    }

    private fun hideCustomView() {
        if (customView == null) return
        customViewContainer.visibility = View.GONE
        customViewContainer.removeView(customView)
        customView = null
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
        webView.visibility = View.VISIBLE
        setupImmersiveMode()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        setupImmersiveMode()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
