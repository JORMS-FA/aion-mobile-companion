# Aion Mobile Companion — Fix UI/UX Issues (Production Ready)

## CRITICAL BUGS TO FIX

### 1. HomeScreen — Overlapping Elements / Bad Layout
The current HomeScreen has:
- WebView fullscreen
- Floating hamburger button top-left
- Sidebar slides from left
- **PROBLEM**: Elements overlap, no proper spacing, looks amateur

**Required redesign**:
- Clean ChatGPT-style interface
- Top app bar with: logo/title, refresh, overflow menu
- WebView content below
- No floating buttons overlapping content
- Proper safe area handling (notches, system bars)
- Production-quality Material 3 design

### 2. Add Server Button Broken When Server Already Open
**BUG**: Navigation doesn't work properly. Button does nothing or crashes.

**Fix**: 
- In HomeScreen sidebar, "Agregar servidor" must navigate to AddServerScreen properly
- AddServerScreen should save and return to HomeScreen with new server selected
- Ensure navigation graph handles this correctly

### 3. QR Scanner — Horizontal Orientation + Not Working
**Issues**:
- Scanner activity forces landscape (wrong for phone)
- Does not return result properly
- No feedback when scanning

**Fix**:
- Portrait-only scanner activity
- Proper ZXing intent handling
- Vibration/haptic on success
- Return scanned URL and auto-connect
- Show torch button for dark environments

### 4. Production-Quality UI Polish
The app looks like a prototype. For production with thousands of users:
- Consistent spacing (8dp grid)
- Proper typography scale
- Meaningful empty states with illustrations
- Loading states everywhere
- Error states with retry
- Smooth animations
- Accessibility (content descriptions, talkback)
- RTL support

## SPECIFIC FILES TO UPDATE
- MainActivity.kt: Navigation, back stack
- HomeScreen.kt: Complete redesign with TopAppBar, no floating menu
- ConnectScreen.kt: Better visual design, fix QR scanner launch (portrait)
- AddServerScreen.kt: Form validation UX, test connection before saving
- WebViewComponent.kt: Pull-to-refresh, error page with retry
- QRScannerActivity.kt (NEW): Portrait orientation, torch toggle, haptic
- Theme.kt / Color.kt: Professional palette, consistent elevation

## NAVIGATION FIXES
- Splash -> Connect (if no servers) -> Home
- Home sidebar -> AddServer -> back to Home with new server active
- Home sidebar -> Servers list -> back
- QR scan -> auto-connect -> Home

## COMPILE & VERIFY
./gradlew assembleDebug
Must pass with zero warnings/errors.
