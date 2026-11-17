# app:shared

The umbrella Kotlin Multiplatform module, and the `Shared.framework` the Xcode project embeds. The only module that sees everything; nothing depends on it except `app:android` and the Xcode project.

| Thing | Role |
|---|---|
| `initKoin(platform)` | Starts Koin with `sojournModules`. Android: `SojournApplication` adds `androidContext`. iOS: `iOSApp.init` calls `startKoinIos()`. |
| `sojournModules`, `appModule` | Every Koin module the app starts with; `appModule` provides the `AppClock` and `DispatcherProvider`. Feature modules join as they land. |
| `App()` | The root on both platforms. It wraps everything in `SojournTheme`, using the guest's `AppLocale` (which also sets text direction) and reduce-motion from `AccessibilitySettings`. It also installs Coil's network image loader and the navigation host. |
| `SojournNavHost` | Navigation 3 with one `rememberNavBackStack` per tab (ADR 0005), after the official multiple-back-stacks recipe. Each tab's entries get their own saved state and per-entry ViewModels (`rememberViewModelStoreNavEntryDecorator`), so a tab keeps its screens' state while another tab is shown. Explore's stack is always at the bottom: back from another tab's root returns to Explore, and back from Explore's root leaves the app. |
| `availableTabs` | The tabs whose feature exists; each feature PR adds its tab. The bottom nav (`SojournTabBar`) needs at least two tabs, so it appears with the second feature. |
| `ShellPlaceholder` | The Explore root until `feature:explore` replaces it. |
| `MainViewController()`, `startKoinIos()` | iOS entry points. |

## Platform notes

- **iOS.** The framework is static, so its link flags don't reach the app target. `iosApp/Configuration/Config.xcconfig` adds `-lsqlite3` for SQLDelight's native driver.
- **Process death.** Each tab's stack is restored. The selected tab is not (`TabbedNavigator` holds it in memory), so the app reopens on Explore.

## Tests (`androidHostTest`)

- `SojournModulesTest`: Koin `verify()` over the whole graph (ADR 0004).
- `SojournNavHostTest`: the host composes with the available tabs. Decorating a stack resolves every key in it, so a tab whose root has no entry fails here with "Unknown screen", not on a device.

**May depend on:** everything.
