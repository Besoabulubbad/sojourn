# app:shared

The umbrella Kotlin Multiplatform module: `App()` root composable (theme, locale, Koin, `NavDisplay` with the four tab back stacks), `initKoin()`, the `Navigator` implementation, `MainViewController()` for iOS, and the `Shared.framework` binary the Xcode project embeds.

The only module that depends on everything. Nothing depends on it except `app:android` and the Xcode project.

**Status:** carries the Compose Multiplatform template `App()` until Phase 5 wires the real root. Contract: [docs/architecture/modules.md](../../docs/architecture/modules.md).
