# Aion Mobile — WebView Render Fix (PWA-Quality)

## OBJETIVO
WebView debe renderizar AionUI **IDÉNTICO** a la captura DevTools (412x915, Samsung, Arco Design dark).

## REFERENCIA VISUAL (desde image-1(2).png)
- **URL**: `http://100.95.4.70:25808/#/guid` (ruta `/guid`, NO `/qr-login`)
- **Framework**: Arco Design (`arco-theme="dark"` en `<body>`, `data-theme="dark"` en `<html>`)
- **CSS Variables**: `--arcoblue-6`, `--arcoblue-5`, `--arcogray-*`
- **Layout móvil**:
  1. Header sticky (icono menu | "AionUi" | help icon)
  2. Greeting "Hi, what's your plan today?"
  3. Horizontal pills scrollables (Model selector)
  4. Input card (textarea + model dropdown + mic)
  5. Grid 2x3 assistants (cards con avatar + title + desc)
  6. Bottom nav sticky (Chat | Star | Globe)
- **Viewport**: 412x915, `viewport-fit=cover`

## FIXES REQUERIDOS EN WebViewComponent.kt

### 1. User Agent MODERNO Chrome Mobile
```kotlin
settings.userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
```

### 2. Viewport + Renderizado COMPLETO
```kotlin
settings.setLoadWithOverviewMode(true)
settings.setUseWideViewPort(true)
settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING)
settings.setTextZoom(100)
settings.setDefaultTextEncodingName("UTF-8")
```

### 3. Hardware Acceleration
```kotlin
webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
webView.setDrawingCacheEnabled(true)
```

### 4. Settings Críticos Faltantes
```kotlin
settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
settings.setDomStorageEnabled(true)
settings.setDatabaseEnabled(true)
settings.setAppCacheEnabled(true)
settings.setAppCachePath(context.cacheDir.absolutePath)
settings.setAllowFileAccess(true)
settings.setAllowContentAccess(true)
settings.setAllowFileAccessFromFileURLs(true)
settings.setAllowUniversalAccessFromFileURLs(true)
settings.setJavaScriptCanOpenWindowsAutomatically(true)
settings.setMediaPlaybackRequiresUserGesture(false)
settings.setSupportZoom(true)
settings.setBuiltInZoomControls(true)
settings.setDisplayZoomControls(false)
```

### 5. Cookies + SSL
```kotlin
CookieManager.getInstance().setAcceptCookie(true)
CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
```

### 6. WebViewClient - Manejo de URL /guid
```kotlin
webViewClient = object : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        // Permitir navegación interna AionUI (#/guid, #/chat, etc)
        if (url.contains("100.95.4.70:25808") || url.contains("192.168.") || url.contains("10.0.")) {
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
        }
    }
}
```

### 7. WebChromeClient - Console logs para debug
```kotlin
webChromeClient = object : WebChromeClient() {
    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        Log.d("AionWebView", "[${consoleMessage.messageLevel()}] ${consoleMessage.message()} (${consoleMessage.sourceId()}:${consoleMessage.lineNumber()})")
        return true
    }
}
```

## ARCHIVO A MODIFICAR
- `app/src/main/java/com/aion/mobile/ui/component/WebViewComponent.kt`

## TEST
- Abrir `http://100.95.4.70:25808/#/guid` → debe verse IDÉNTICO a DevTools
- Header sticky, pills scrollables, grid 2x3, bottom nav sticky
- Sin corte a la derecha, dark mode Arco Design aplicado

## REQUISITOS
- ./gradlew assembleDebug MUST PASS
- WebView render = DevTools emulation
