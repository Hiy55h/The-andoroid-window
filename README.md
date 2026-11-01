# AstraOS

Android 11+ desktop-style launcher/browser prototype for Google TV.

## Current preview
- Chrome-inspired desktop browser layout
- Desktop user-agent and wide viewport
- Multiple tabs (click a tab to switch; long-press to close)
- Page zoom controls
- AstraOS user-script manager (simple JavaScript injection)
- HTTP/HTTPS links can be routed to BrowserActivity using Android VIEW intents
- 32-bit ARM target: armeabi-v7a

## Important limitations
- The browser uses Android System WebView (Chromium-based), not the full Google Chrome engine.
- It does not currently support Chrome Web Store extensions or the full Chrome extensions API. The Extensions menu currently manages simple user scripts.
- Overlay apps can launch this browser through an Android VIEW intent or the custom `com.hiy55h.astraos.OPEN_BROWSER` action if they support external activity intents. Whether it appears as a floating window depends on the Overlay app and device.
- A normal Android app cannot force every installed app into a true resizable window. External app windowing depends on Android/device freeform or multi-window support.

## Build
Open this repository in Android Studio, then build the `app` debug variant.
