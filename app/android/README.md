# app:android

The Android application:

- `SojournApplication` starts Koin with the application `Context`.
- `MainActivity` calls `enableEdgeToEdge()` before `setContent { App() }`. The status and navigation bar icons follow the app's Appearance (System, Light or Dark), not only the device's.
- The manifest sets `adjustResize` for the keyboard.
- **Window and splash** (`Theme.Sojourn`): the window is the screen colour of the device's theme (`values-night` for dark), so a cold start never flashes white. On API 31+ the system splash shows the launcher icon's artwork on its brand ground (`values-v31`). The colours are generated from the design tokens by `tooling/scripts/make_brand.py` (`window_colors.xml`).
- **Per-app language** (Android 13+): `res/xml/locales_config.xml` lists English and Arabic, so Settings › Apps › Sojourn › Language offers them. A choice in the app is written to `LocaleManager`; a choice in Settings is read back when the activity is recreated (`AppLocaleController.syncFromPlatform`). Both ways, ADR 0009.
- **Back:** predictive back is on (target SDK 37); Navigation 3 and our sheets handle it.

**Payment:** no Google Pay adapter. Checkout shows a labelled demo wallet from the shared UI (ADR 0018).

**May depend on:** app:shared, activity, koin-android.
