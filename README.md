# AstraOS 0.1.0

A desktop-style Android launcher and browser environment designed for Android TV devices.

## Included in 0.1.0
- AstraOS lock screen
- Desktop with Browser, Apps, and Settings shortcuts
- Start menu
- Installed-app launcher
- Movable desktop windows
- Minimize, restore, maximize, and close controls
- Taskbar buttons for open windows
- Taskbar clock
- Built-in browser window with address/search bar, back navigation, and page zoom support
- Keyboard and mouse input

## Target
- Android 11 or newer
- 32-bit ARM (armeabi-v7a)
- Google TV / Android TV

## Important Android limitations
AstraOS can display its own windows as movable desktop windows. Android does not let an ordinary app force every other installed app (for example, YouTube for Android TV) into AstraOS's window manager. Apps selected from Installed apps launch through Android and may appear full-screen or in the device's own multi-window mode, depending on the device firmware.

The built-in browser uses Android System WebView. It is not the full Google Chrome browser and does not support Chrome Web Store extensions or the complete Chrome extensions API.

## Build
Open the repository in Android Studio and build the app debug variant, or run the GitHub Actions workflow to create an APK artifact.

APK output name: AstraOS-0.1.0-debug.apk.