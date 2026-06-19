# Aion Mobile Companion — Critical Fixes v1.3.0

## ISSUES TO FIX (ALL CRITICAL)

### 1. WebView Shows Blank / Doesnt Render Like Browser
**Problem**: URL http://100.95.4.70:25808/qr-login?token=... shows blank white screen
**Root cause**: WebView missing critical settings for modern web apps
**Required settings**:
- settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
- settings.setSupportZoom(true)
- settings.setBuiltInZoomControls(true)
- settings.setDisplayZoomControls(false)
- settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING)
- settings.setUseWideViewPort(true)
- settings.setLoadWithOverviewMode(true)
- settings.setJavaScriptCanOpenWindowsAutomatically(true)
- settings.setMediaPlaybackRequiresUserGesture(false)
- settings.setCacheMode(WebSettings.LOAD_DEFAULT)
- settings.setDatabaseEnabled(true)
- settings.setDomStorageEnabled(true)
- settings.setAllowFileAccess(true)
- settings.setAllowContentAccess(true)
- settings.setAllowFileAccessFromFileURLs(true)
- settings.setAllowUniversalAccessFromFileURLs(true)
- User agent should be modern Chrome, not default WebView
- Enable third-party cookies properly
- WebView.setLayerType(View.LAYER_TYPE_HARDWARE, null) for hardware acceleration

### 2. Swipe to Delete - Red Background Too Large
**Problem**: In ServersScreen, the red delete background covers full item height, looks bigger than the card
**Fix**: The backgroundContent in SwipeToDismissBox should match the card height exactly, not fillMaxSize

### 3. Missing Welcome/Tutorial Screen
**Requirement**: First-time users need a welcome screen with tutorial based on README:
- Show the 3 connection methods (LAN, Tailscale, Server)
- Explain QR code login
- Show how to start AionUI with --webui --remote
- Only show once (use DataStore flag has_seen_welcome)

### 4. QR Scanner Missing When Adding New Server
**Problem**: ConnectScreen has QR scanner button but AddServerScreen doesnt
**Fix**: Add QR scan button to AddServerScreen that:
- Launches QRScannerActivity
- On result, auto-fills the URL field
- User can then save

### 5. Version Shows v1.0.0 Instead of v1.2.0
**Fix**: Update HomeScreen footer version from BuildConfig.VERSION_NAME

### 6. App Not Fast / Rendering Issues
**Performance fixes**:
- Add hardware acceleration to WebView
- Pre-load WebView when possible
- Optimize WebView settings for speed
- Remove unnecessary re-compositions

### 7. WebView Error Handling
**Current**: Error page shows but retry doesnt always work
**Fix**: Better error recovery, clear cache option, proper SSL handling

## FILES TO MODIFY
1. WebViewComponent.kt - Complete WebView settings overhaul + hardware accel
2. ServersScreen.kt - Fix swipe-to-delete background sizing
3. HomeScreen.kt - Update version, maybe add welcome navigation
4. ConnectScreen.kt - Already has QR scanner (good)
5. AddServerScreen.kt - ADD QR scan button
6. MainActivity.kt - Add WelcomeScreen to navigation flow
7. WelcomeScreen.kt (NEW) - Tutorial/welcome screen
8. AppPreferences.kt - Add has_seen_welcome flag
9. build.gradle.kts - Ensure version matches
10. NavGraph.kt - Add Welcome route

## NAVIGATION FLOW (NEW)
Splash -> Welcome (if first time) -> Connect (if no servers) -> Home
                 |
                 v
           (if has servers) -> Home directly

## REQUIREMENTS
- NO breaking changes to existing working features
- Keep Material 3 design
- Compile MUST pass: ./gradlew assembleDebug
- Test the URL: http://100.95.4.70:25808/qr-login?token=... should render
- Portrait-only QR scanner (already done)
- All WebView features work: file upload, cookies, JS, localStorage
