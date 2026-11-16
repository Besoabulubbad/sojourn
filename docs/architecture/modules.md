# Modules

Every Gradle module, what it owns, what it may depend on, and the convention plugin that configures it. Paths are relative to the repository root; Gradle paths use `:`. A module's own `README.md` holds detail that changes often (public API, open questions); this page holds the contract.

Legend for *may depend on*: **core-base** = `core:model`, `core:common`.

## Application

| Module | Owns | May depend on | Plugin |
|---|---|---|---|
| `:app:android` | `MainActivity`, `enableEdgeToEdge`, splash, predictive back, per-app language (`LocaleManager`), Google Pay adapter `actual`s, app icon, manifest | `:app:shared`, `:core:platform` | `sojourn.android.application` |
| `:app:shared` | `App()` root composable, `initKoin()`, root `NavDisplay` and tab back stacks, `Navigator` implementation, iOS `MainViewController()`, `Shared.framework` | everything | `sojourn.kmp.library` + `sojourn.compose` + `sojourn.koin` |
| `iosApp/` | Xcode project: `iOSApp.swift`, `ContentView.swift` hosting the framework, Info.plist, assets, Apple Pay adapter | `Shared.framework` | — (Xcode) |

## Features

All feature modules share one contract: *may depend on* `:core:ui`, `:core:designsystem`, `:core:localization`, `:core:navigation`, `:core:domain`, `:core:platform`, core-base; **never** `:core:data`, `:core:database`, `:core:network`, or another `:feature:*`. Plugin: `sojourn.kmp.feature` (which applies compose, koin, screenshot and detekt). Each exposes `fun EntryProviderBuilder<NavKey>.<name>Entries(navigator: Navigator)` and `val <name>Module: Module`.

| Module | Design-system screen(s) | Key(s) in `core:navigation` |
|---|---|---|
| `:feature:explore` | ScreenExplore | `ExploreKey` |
| `:feature:search` | ScreenSearchFlow (Where · When · Who) | `SearchKey(step)` |
| `:feature:results` | ScreenResults, ScreenStates | `ResultsKey(query)` |
| `:feature:filters` | ScreenFilters (sheet) | `FiltersKey` |
| `:feature:details` | ScreenDetails, full-screen Gallery | `StayDetailsKey(stayId, quoteId?)`, `GalleryKey(stayId, index)` |
| `:feature:checkout` | ScreenCheckout, confirmation | `CheckoutKey(quoteId)`, `BookingConfirmedKey(bookingId)` |
| `:feature:map` | ScreenMap | `MapKey(query)` |
| `:feature:saved` | ScreenSaved | `SavedKey` |
| `:feature:bookings` | ScreenBookings | `BookingsKey`, `BookingDetailsKey(id)` |
| `:feature:profile` | ScreenProfile (language, theme, rewards) | `ProfileKey`, `LanguageKey`, `AppearanceKey` |

## Core

| Module | Owns | May depend on | Plugin |
|---|---|---|---|
| `:core:model` | Domain types: `Stay`, `City`, `Photo`, `Amenity`, `Policy`, `Quote`, `PriceBreakdown`, `PriceDisplay`, `Booking`, `SearchQuery`, `StayLength`, `Filters`, `Guests`. Pure Kotlin, immutable, no framework imports. | kotlinx-datetime | `sojourn.kmp.library` |
| `:core:common` | `DispatcherProvider`, `Clock` abstraction, `AppResult`/`AppError`, Kermit `Logger` facade, DI qualifiers. | Kermit, coroutines | `sojourn.kmp.library` |
| `:core:localization` | `AppLocale`, `LocalAppLocale`, `AppLocaleController`, `SojournFormatter` (money, dates, ranges, counts, bidi isolates), `strings.xml`/`plurals.xml` per locale (Compose Resources), plural-completeness Gradle check. The **only** module allowed to touch platform locale APIs. | core-base, Compose Resources | `sojourn.kmp.library` + `sojourn.compose` |
| `:core:designsystem` | Generated tokens (`SojournColors`, `SojournType`, `SojournSpacing`, `SojournShapes`, `SojournMotion`, `SojournSizes`), `SojournTheme`, fonts, `SojournIcon`, all 26 `Sojourn*` components, component READMEs and previews. The **only** module allowed to import Material 3. | `:core:localization`, core-base, Compose, Material 3, Coil | `sojourn.kmp.library` + `sojourn.compose` + `sojourn.tokens` + `sojourn.screenshot` |
| `:core:ui` | `ScreenState<T>`, `UiState`/`UiIntent`/`UiEffect`, `SojournViewModel`, `SojournScaffold`, `HostText`, `collectEffects`, preview/screenshot parameter providers (`SojournVariants`), common empty/error/offline screen content. | `:core:designsystem`, `:core:localization`, core-base, lifecycle-viewmodel | `sojourn.kmp.library` + `sojourn.compose` |
| `:core:navigation` | `SojournNavKey` sealed hierarchy, `Tab`, `Navigator` interface, `sojournNavSerializersModule`, `NavDisplay` scene strategies (sheet, dialog), transition specs from `SojournMotion`. | `:core:designsystem` (motion tokens), core-base, Navigation 3 | `sojourn.kmp.library` + `sojourn.compose` |
| `:core:platform` | `expect`/`actual`: `Haptics`, `ShareSheet`, `MapsLauncher`, `PaymentSheet` (Google Pay / Apple Pay stubs), `AccessibilitySettings` (reduce motion, font scale). | core-base | `sojourn.kmp.library` |
| `:core:database` | SQLDelight `.sq` schema and migrations, `SojournDatabase`, `DatabaseDriverFactory` (`expect`/`actual`), `SeedLoader`, typed adapters. | core-base, SQLDelight | `sojourn.kmp.library` + `sojourn.sqldelight` |
| `:core:network` | `SojournApi` interface, DTOs + mappers, `LocalSojournApi` (bundled JSON, `FakeNetworkConditions`), `KtorSojournApi` (configured, flag-gated), `HttpClient` factory. | core-base, Ktor, kotlinx-serialization | `sojourn.kmp.library` |
| `:core:data` | Repository interfaces **and** implementations: `StayRepository`, `CityRepository`, `SearchRepository`, `PricingRepository`, `SavedRepository`, `BookingRepository`, `PreferencesRepository`. Offline-first sync policy. Koin `dataModule`. | `:core:database`, `:core:network`, core-base, multiplatform-settings | `sojourn.kmp.library` + `sojourn.koin` |
| `:core:domain` | Use cases, one class each with `operator fun invoke`: `SearchStays`, `GetStayDetails`, `GetQuote`, `ToggleSaved`, `ObserveSaved`, `ListBookings`, `CreateBooking`, `ObserveFilters`, `UpdateFilters`, `GetCities`. Business rules and their tests. Koin `domainModule`. | `:core:data` (interfaces), core-base | `sojourn.kmp.library` + `sojourn.koin` |
| `:core:testing` | Fixtures (`StayFixtures`, `QuoteFixtures`), fake repositories, `SojournScreenshotTest` base (Roborazzi + Robolectric, matrix runner), `MainDispatcherRule`, Turbine helpers, Koin test module. Test-scoped only. | everything in `core`, test libraries | `sojourn.kmp.library` |

## Tooling and build

| Path | Owns |
|---|---|
| `build-logic/` | Convention plugins (`sojourn.*`), the token generator task, shared detekt and Roborazzi configuration. Included build. |
| `tooling/konsist` | Architecture tests over the whole repository (module graph, naming, forbidden imports). Runs in `check`. |
| `tooling/detekt-rules` | Custom detekt rule set encoding the design-system standards (tokens only, start/end only, no string literals into text, insets only in owners, motion tokens only), with tests. Applied via `detektPlugins`. |
| `design/tokens.json` | Verbatim copy of the design system's token file. Input to `sojourn.tokens`. |
| `config/detekt/` | `detekt.yml`, custom rule set wiring, baseline (ideally empty). |
| `.maestro/` | End-to-end flows (`booking-golden-path.yaml`, `language-switch.yaml`). |
| `.github/` | Workflows (`pr.yml`, `main.yml`, `release.yml`, `docs.yml`, `claude-review.yml`), PR/issue templates, `CODEOWNERS`, `renovate.json`. |
| `docs/` + `mkdocs.yml` | This site. |

## Convention plugins

| Plugin id | Applies / configures |
|---|---|
| `sojourn.kmp.library` | Kotlin Multiplatform with `android` (AGP KMP library DSL, namespace derived from the project path), `iosArm64`, `iosSimulatorArm64`; JVM target 11; host tests with Android resources; common test deps; detekt |
| `sojourn.kmp.feature` | `sojourn.kmp.library` + `sojourn.compose` + `sojourn.koin` + `sojourn.screenshot` + the standard feature dependency set |
| `sojourn.compose` | Compose Multiplatform plugin and compiler; `runtime`/`foundation`/`ui` as `api` (they appear in every UI module's public signatures), resources and preview tooling as `implementation` |
| `sojourn.android.application` | Android application, `compileSdk`/`targetSdk`/`minSdk` from the catalog, signing from env, R8 on release |
| `sojourn.koin` | Koin core/compose/viewmodel deps |
| `sojourn.sqldelight` | SQLDelight plugin, `SojournDatabase` config, drivers per target |
| `sojourn.screenshot` | Roborazzi plugin, Robolectric `androidHostTest` setup, `recordRoborazziDebug`/`verifyRoborazziDebug`, baseline directory |
| `sojourn.tokens` | `generateDesignTokens` task reading `design/tokens.json`, wired into `compileKotlin` |
| `sojourn.detekt` | detekt with Compose rules and the project config |
| `sojourn.dokka` | Dokka for `core:*` public API |

## Adding a module

1. Decide its layer; if it does not fit a row above, it probably needs an ADR.
2. Create `path/build.gradle.kts` with only `plugins { id("sojourn.…") }` and its own dependencies.
3. Add it to `settings.gradle.kts`, to the Konsist module graph test, and to this page.
4. Add a `README.md` in the module and a Koin module if it provides anything.
