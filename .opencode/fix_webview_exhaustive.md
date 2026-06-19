# Aion Mobile — WebView Blank Screen FIX (Exhaustivo)

## PROBLEMA
- URL `http://100.95.4.70:25808/#/guid` funciona en Chrome móvil
- En WebView: **pantalla blanca/nada**
- Framework: Arco Design (ByteDance) + React/Vue SPA

## CAUSAS PROBABLES (orden de probabilidad)
1. **Mixed Content**: Página HTTP cargando recursos HTTPS (o viceversa)
2. **CSP (Content Security Policy)**: Bloquea inline scripts, eval, WebSocket
3. **Feature Detection**: `navigator.serviceWorker`, `indexedDB`, `localStorage`, `crypto.subtle` fallan en WebView
4. **Network Security Config**: Android 9+ bloquea cleartext HTTP por defecto
5. **Cookies/Session**: Third-party cookies, SameSite, secure flags
6. **User Agent Detection**: Sitio detecta WebView y sirve versión rota
8. **JavaScript Errors**: Errores silenciosos que rompen renderizado
9. **Viewport/CSS**: `100vh`, `100%`, `position: fixed` rotos en WebView

## FIX EXHAUSTIVO - WebViewComponent.kt

### 1. NETWORK SECURITY CONFIG (CRÍTICO para HTTP)
Crear `res/xml/network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">100.95.4.70</domain>
        <domain includeSubdomains="true">192.168.0.0/16</domain>
        <domain includeSubdomains="true">10.0.0.0/8</domain>
        <domain includeSubdomains="true">172.16.0.0/12</domain>
        <domain includeSubdomains="true">localhost</domain>
    </domain-config>
    <base-config cleartextTrafficPermitted="true" />
</network-security-config>
```
En AndroidManifest: `<application android:networkSecurityConfig="@xml/network_security_config">`

### 2. WEBVIEW SETTINGS EXHAUSTIVOS
```kotlin
// User Agent Chrome 120 Mobile EXACTO
settings.userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

// Viewport
settings.setLoadWithOverviewMode(true)
settings.setUseWideViewPort(true)
settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING)
settings.setTextZoom(100)
settings.setDefaultTextEncodingName("UTF-8")
settings.setDefaultFontSize(16)
settings.setMinimumFontSize(12)

// Hardware Acceleration
webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
webView.setDrawingCacheEnabled(true)

// JavaScript COMPLETO
settings.javaScriptEnabled = true
settings.javaScriptCanOpenWindowsAutomatically = true
settings.setMediaPlaybackRequiresUserGesture(false)

// Storage TODOS
settings.setDomStorageEnabled(true)
settings.setDatabaseEnabled(true)
settings.setAllowFileAccess(true)
settings.setAllowContentAccess(true)
settings.setAllowFileAccessFromFileURLs(true)
settings.setAllowUniversalAccessFromFileURLs(true)

// Mixed Content
settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

// Zoom
settings.setSupportZoom(true)
settings.setBuiltInZoomControls(true)
settings.setDisplayZoomControls(false)

// Cache
settings.setCacheMode(WebSettings.LOAD_DEFAULT)
settings.setAppCacheEnabled(true)
settings.setAppCachePath(context.cacheDir.absolutePath)

// Cookies
CookieManager.getInstance().setAcceptCookie(true)
CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

// Feature Detection Support
settings.setAllowFileAccessFromFileURLs(true)
settings.setAllowUniversalAccessFromFileURLs(true)
```

### 3. WEBVIEWCLIENT - Debug + Error Handling
```kotlin
webViewClient = object : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        // Permitir TODA navegación interna AionUI
        if (url.startsWith("http://") || url.startsWith("https://")) {
            view?.loadUrl(url)
            return true
        }
        return false
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        if (request?.isForMainFrame == true) {
            hasError = true
            isLoading = false
            isRefreshing = false
            Log.e("AionWebView", "onReceivedError: ${error?.description} (code: ${error?.errorCode}) url: ${request?.url}")
        }
    }

    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
        Log.w("AionWebView", "HTTP Error: ${errorResponse?.statusCode} ${errorResponse?.reasonPhrase} url: ${request?.url}")
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        isLoading = false
        isRefreshing = false
        onLoadingChanged(false)
        // Inyectar viewport meta si no existe
        view?.evaluateJavascript("""
            (function() {
                var meta = document.querySelector('meta[name=viewport]');
                if (!meta) {
                    meta = document.createElement('meta');
                    meta.name = 'viewport';
                    meta.content = 'width=device-width, initial-scale=1.0, user-scalable=no, viewport-fit=cover';
                    document.head.appendChild(meta);
                }
            })();
        """.trimIndent(), null)
        Log.d("AionWebView", "Page finished: $url")
    }
}
```

### 4. WEBCHROMECLIENT - Console Debug COMPLETO
```kotlin
webChromeClient = object : WebChromeClient() {
    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        val level = when (consoleMessage.messageLevel()) {
            ConsoleMessage.MessageLevel.ERROR -> "ERROR"
            ConsoleMessage.MessageLevel.WARNING -> "WARN"
            ConsoleMessage.MessageLevel.INFO -> "INFO"
            ConsoleMessage.MessageLevel.DEBUG -> "DEBUG"
            ConsoleMessage.MessageLevel.LOG -> "LOG"
            else -> "UNKNOWN"
        }
        Log.d("AionWebView", "[$level] ${consoleMessage.message()} (${consoleMessage.sourceId()}:${consoleMessage.lineNumber()})")
        return true
    }

    override fun onPermissionRequest(request: PermissionRequest?) {
        // Permitir cámara, micrófono, geolocalización
        request?.grant(request.resources)
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        progress = newProgress
    }
}
```

### 5. HABILITAR REMOTE DEBUGGING (para inspeccionar con Chrome DevTools)
```kotlin
if (BuildConfig.DEBUG) {
    WebView.setWebContentsDebuggingEnabled(true)
}
```

### 6. WEB SETTINGS ADICIONALES PARA SPAs
```kotlin
// Soporte para Service Workers, IndexedDB, Cache API
settings.setAllowFileAccessFromFileURLs(true)
settings.setAllowUniversalAccessFromFileURLs(true)

// Fix para 100vh en WebView
webView.setWebViewClient(object : WebViewClient() {
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        // Fix 100vh en mobile WebView
        view?.evaluateJavascript("""
            (function() {
                var style = document.createElement('style');
                style.textContent = '@supports (-webkit-touch-callout: none) { html, body { height: -webkit-fill-available; } body { min-height: -webkit-fill-available; } }';
                document.head.appendChild(style);
            })();
        """.trimIndent(), null)
    }
})
```

## ARCHIVOS A MODIFICAR/CREAR
1. `WebViewComponent.kt` - Settings exhaustivos + debug
2. `res/xml/network_security_config.xml` (NUEVO) - Cleartext traffic
3. `AndroidManifest.xml` - Agregar `android:networkSecurityConfig`

## DEBUG PARA VER QUÉ FALLA
1. `adb logcat -s AionWebView` → ver console logs, errores JS, network errors
2. `chrome://inspect` en desktop → inspeccionar WebView remoto
3. Verificar en logcat: `ERROR`, `WARN`, `AionWebView`

## TEST URL
`http://100.95.4.70:25808/#/guid` → Debe renderizar Arco Design dark completo

## REQUISITOS
- ./gradlew assembleDebug MUST PASS
- WebView render = Chrome móvil exactamente
- Logcat muestra qué falla (JS errors, network, CSP)
EOF
echo "Instructions written"