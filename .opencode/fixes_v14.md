# Aion Mobile Companion — Critical Fixes v1.4.0 (Production Ready)

## PROBLEMAS CRÍTICOS DESDE CAPTURA DE PANTALLA

### 1. WebView - Tarjeta de login CORTADA A LA DERECHA
- **Problema**: La tarjeta de login de AionUI se ve cortada en el borde derecho
- **Causa**: WebView viewport no configurado para responsive/mobile
- **Fix requerido**:
  - `settings.setLoadWithOverviewMode(true)`
  - `settings.setUseWideViewPort(true)`
  - `settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING)`
  - `settings.setSupportZoom(true)`
  - `settings.setBuiltInZoomControls(true)`
  - `settings.setDisplayZoomControls(false)`
  - Meta viewport handling: `settings.setJavaScriptEnabled(true)` + inject viewport meta si no existe
  - User agent moderno Chrome móvil
  - Hardware acceleration: `setLayerType(View.LAYER_TYPE_HARDWARE, null)`

### 2. QR Scanner - NO FUNCIONA / NO PIDE PERMISOS
- **Problema**: Al tocar "Escanear QR" no abre cámara, no pide permisos
- **Causa**: QRScannerActivity no declara `android:requestLegacyExternalStorage`, falta permiso CAMERA en manifest, no usa ActivityResultContracts para solicitar permiso
- **Fix**:
  - Agregar `<uses-permission android:name="android.permission.CAMERA" />` en manifest
  - En ConnectScreen/AddServerScreen: usar `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` para CAMERA antes de lanzar scanner
  - QRScannerActivity ya tiene `android:exported="false"` y `screenOrientation="portrait"` ✓
  - Verificar que `barcodeView.decodeContinuous(callback)` se llama en `onResume`

### 3. Segmented Buttons - ICONOS SUPERPUESTOS CON TEXTO
- **Problema**: En ConnectScreen, botones LAN/Tailscale muestran icono encima del texto
- **Causa**: `SegmentedButton` con `Icon` + `Text` sin espaciado adecuado
- **Fix**: Usar `Row` con `Icon` + `Spacer` + `Text` dentro del `SegmentedButton`, o quitar iconos y usar solo texto con colores distintivos

### 4. NAVEGACIÓN - NO HAY FORMA FÁCIL DE VOLVER AL INICIO
- **Problema**: En HomeScreen, el drawer no tiene "Volver al tutorial" ni "Reiniciar configuración"
- **Fix**: Agregar en drawer:
  - "Ver tutorial" → navega a WelcomeScreen (resetea flag has_seen_welcome)
  - "Reiniciar app" → limpia servidores y vuelve a Welcome

### 5. WebView RENDERIZADO - "¿Meter un navegador completo?"
- **Realidad**: WebView **SÍ puede** renderizar como Chrome si se configura bien
- **No necesitas**: Chrome Custom Tabs, navegador externo, ni WebView2
- **SÍ necesitas**: Todos los settings de WebView correctamente configurados (ver #1)

---

## ARCHIVOS A MODIFICAR

1. **WebViewComponent.kt** - Settings completos + hardware accel + viewport fix
2. **QRScannerActivity.kt** - Verificar permisos, onResume/onPause correctos
3. **ConnectScreen.kt** - Arreglar segmented buttons (quitar iconos o espaciar), solicitar permiso CAMERA antes de scanner
4. **AddServerScreen.kt** - Mismo fix permiso CAMERA + segmented buttons si aplica
5. **HomeScreen.kt** - Agregar "Ver tutorial" y "Reiniciar" en drawer
6. **MainActivity.kt** - Función para resetear a Welcome
7. **AppPreferences.kt** - `resetToWelcome()` function
8. **AndroidManifest.xml** - Verificar permiso CAMERA

---

## REQUISITOS OBLIGATORIOS

- `./gradlew assembleDebug` DEBE PASAR (0 errores)
- WebView debe renderizar `http://100.95.4.70:25808/qr-login?token=...` SIN CORTAR
- QR Scanner debe abrir cámara, pedir permiso si no tiene, escanear, vibrar, volver con URL
- Segmented buttons LAN/Tailscale legibles sin superposición
- Drawer con navegación completa: Servidores, Tutorial, Reiniciar, Ajustes
- Material 3 consistente, dark AMOLED default
- Compile SDK 35, min SDK 26

---

## TESTING CHECKLIST

- [ ] Instalar APK, primera vez → WelcomeScreen
- [ ] "Continuar" → ConnectScreen
- [ ] "Escanear QR" → pide permiso cámara → abre scanner → escanea → vibra → vuelve con URL
- [ ] "Buscar en red" → encuentra servidor → conecta
- [ ] HomeScreen carga URL → tarjeta login NO cortada, responsive
- [ ] Drawer → "Ver tutorial" → WelcomeScreen
- [ ] Drawer → "Reiniciar" → limpia todo → WelcomeScreen
- [ ] AddServer → "Escanear QR" → funciona
- [ ] Swipe delete servidor → fondo rojo exacto