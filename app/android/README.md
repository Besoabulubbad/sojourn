# app:android

The Android application:

- `SojournApplication` starts Koin with the application `Context`.
- `MainActivity` calls `enableEdgeToEdge()` before `setContent { App() }`. Status and navigation bar icons follow the system theme.
- The manifest sets `adjustResize` for the keyboard.

Still to come, as their features need them:
- The splash screen (Phase 6).
- Per-app language through `LocaleManager` (Profile).
- The Google Pay adapter (Checkout).

**May depend on:** app:shared, activity, koin-android.
