# Aion Mobile Companion — Full Refactor

## Project Identity
App Android nativa (Kotlin + Jetpack Compose + Material 3) que funciona como compañero móvil de AionUI (cowork de AI Agents open-source). Min SDK 26, Target SDK 35. AGP 8.7.3, Kotlin 2.0.21.

## Current State
The app compiles and installs but **crashes on startup** — opens and closes immediately. The most likely causes:
1. Immersive mode code in `MainActivity.enableImmersiveMode()` using deprecated `systemUiVisibility` flags
2. The `ConnectScreen` composable in the NavHost might have a runtime crash
3. `AionUIDiscovery` using deprecated WiFi APIs on Android 13+

## Requirements (PRIORITY ORDER)

### 1. FIX CRASH ON STARTUP (BLOCKER)
- Remove ALL the deprecated `systemUiVisibility` immersive mode code
- Use only the modern `WindowInsetsController` API (Android 11+, API 30+)
- Wrap in try/catch so it never crashes the app
- Test that the app opens and stays open

### 2. DEFAULT AMOLED DARK THEME
- Change `AppPreferences` so `darkMode` defaults to `true` (not `false`)
- The theme should use true black (`#FF000000`) background for AMOLED screens
- Update `Theme.kt` to use AMOLED-friendly colors in dark mode

### 3. DUAL MODE: LAN + TAILSCALE
- The app should work seamlessly with both:
  - **LAN mode**: Same WiFi, uses local IP (e.g., 192.168.1.x:25808)
  - **Tailscale mode**: Remote access via Tailscale IP (e.g., 100.x.x.x:25808)
- When adding/connecting to a server, allow the user to choose mode
- Display the current connection mode in the sidebar
- Add a Tailscale help section in the connect screen

### 4. QR CODE SCANNING
- Add ZXing or CameraX barcode scanning dependency
- Add a "Escanear QR" button in the ConnectScreen
- The QR code should encode the AionUI WebUI URL (e.g., `http://192.168.1.100:25808`)
- Parse the scanned QR and auto-connect
- Request camera permission properly

### 5. FILE RECEIVING (WEBVIEW FILE INPUT)
- Make the WebView support file uploads (`input type="file"`)
- Implement `onShowFileChooser` in WebChromeClient
- Support camera capture and file selection
- Handle `ValueCallback` properly
- Add `READ_EXTERNAL_STORAGE` / `READ_MEDIA_IMAGES` permissions

### 6. TAILSCALE HELP/INSTRUCTIONS
- Add a "Ayuda Tailscale" section in the ConnectScreen
- Include step-by-step: install Tailscale, enable subnet routing, find Tailscale IP
- Make it collapsible/expandable
- Text should be in Spanish

### 7. NETWORK DISCOVERY IMPROVEMENTS
- Use non-deprecated WiFi APIs (`WifiManager.getConnectionInfo()` is deprecated on API 31+)
- Add runtime permission `ACCESS_FINE_LOCATION` for WiFi scanning on Android 12+
- Make the scan non-blocking with progress feedback
- Handle the case where WiFi is not connected

## Architecture

- **Package**: `com.aion.mobile`
- **UI**: Jetpack Compose + Material 3
- **Navigation**: Navigation Compose
- **Persistence**: DataStore Preferences
- **Notifications**: WorkManager

## Key Files
- `app/src/main/java/com/aion/mobile/MainActivity.kt` — Entry point, immersive mode, navigation
- `app/src/main/java/com/aion/mobile/data/prefs/AppPreferences.kt` — DataStore, dark mode default
- `app/src/main/java/com/aion/mobile/ui/screen/HomeScreen.kt` — Main screen, WebView, sidebar
- `app/src/main/java/com/aion/mobile/ui/screen/ConnectScreen.kt` — First-launch connect with network scan
- `app/src/main/java/com/aion/mobile/ui/theme/Theme.kt` — Color theme (AMOLED dark)
- `app/src/main/java/com/aion/mobile/ui/theme/Color.kt` — Color definitions
- `app/src/main/java/com/aion/mobile/network/AionUIDiscovery.kt` — Network scanning
- `app/src/main/java/com/aion/mobile/ui/component/WebViewComponent.kt` — WebView wrapper
- `app/src/main/AndroidManifest.xml` — Manifest with permissions
- `app/build.gradle.kts` — Dependencies

## DO NOT
- Change the app name or package
- Remove existing screens (Servers, Settings, Reminders)
- Add unnecessary third-party dependencies
- Break the compileSdk/targetSdk version
- Remove the WebView core functionality
- Write code you can't test-compile

## DO
- Fix the crash FIRST
- Make dark mode default AMOLED
- Add QR scanning with ZXing
- Add file upload support in WebView
- Add Tailscale instructions in Spanish
- Clean up all deprecated API usage
- Test compile with `./gradlew assembleDebug`

## Credentials
- No API keys needed for this project
- GitHub token is in `~/.git-credentials` if needed for dependency access
- OpenCode model: `opencode/deepseek-v4-flash-free`

