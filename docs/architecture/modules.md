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
| `:core:localization` | `AppLocale`, `LocalAppLocale`, `ProvideAppLocale`, `AppLocaleController` + `PlatformLocaleSync`, `SojournFormatter` (money, dates, ranges, counts, bidi isolates), `strings.xml` per locale with plurals (Compose Resources, public `Res`), `localizationModule`. The **only** module allowed to touch platform locale APIs. | core-base, Compose Resources, multiplatform-settings | `sojourn.kmp.library` + `sojourn.compose` + `sojourn.koin` + `sojourn.localization` |
| `:core:designsystem` | Generated tokens (`SojournColors`, `SojournType`, `SojournSpacing`, `SojournShapes`, `SojournElevation`, `SojournSizes`, `SojournMotion`), `SojournTheme`, fonts, `SojournIcon`, `SojournVariant(s)` + `SojournPreview`, the token sheet, and from Phase 3 all 26 `Sojourn*` components. The **only** module allowed to import Material 3. | `:core:localization` (`api`), core-base, Compose, Material 3, Coil | `sojourn.kmp.library` + `sojourn.compose` + `sojourn.tokens` + `sojourn.screenshot` |
| `:core:ui` | `ScreenState<T>`, `UiState`/`UiIntent`/`UiEffect`, `SojournViewModel`, `CollectEffects`, `SojournScaffold`, `HostText`; common empty/error/offline screen content from Phase 3. | `:core:designsystem`, core-base, lifecycle-viewmodel | `sojourn.kmp.library` + `sojourn.compose` + `sojourn.screenshot` |
| `:core:navigation` | `SojournNavKey` sealed hierarchy, `SearchArgs`, `Tab`, `Navigator` + `TabbedNavigator` (per-tab back stacks), `sojournNavSerializersModule` / `sojournSavedStateConfiguration`; scene strategies and `SojournMotion` transitions from Phase 5. | Navigation 3 runtime, savedstate, kotlinx-serialization; `:core:designsystem` from Phase 5 | `sojourn.kmp.library` + `sojourn.compose` + serialization |
| `:core:platform` | Interfaces `Haptics`, `ShareSheet`, `MapsLauncher`, `PaymentSheet` (`StubPaymentSheet` until Phase 6), `AccessibilitySettings` (reduce motion), with Android and iOS implementations registered by `platformModule` (expect/actual). | core-base | `sojourn.kmp.library` + `sojourn.koin` |
| `:core:database` | SQLDelight `.sq` schema and migrations, `SojournDatabase`, `DatabaseDriverFactory` (`expect`/`actual`), `SeedLoader`, typed adapters. | core-base, SQLDelight | `sojourn.kmp.library` + `sojourn.sqldelight` |
| `:core:network` | `SojournApi` interface, DTOs + mappers, `LocalSojournApi` (bundled JSON, `FakeNetworkConditions`), `KtorSojournApi` (configured, flag-gated), `HttpClient` factory. | core-base, Ktor, kotlinx-serialization | `sojourn.kmp.library` |
| `:core:data` | Repository interfaces **and** implementations: `StayRepository`, `CityRepository`, `SearchRepository`, `PricingRepository`, `SavedRepository`, `BookingRepository`, `PreferencesRepository`. Offline-first sync policy. Koin `dataModule`. | `:core:database`, `:core:network`, core-base, multiplatform-settings | `sojourn.kmp.library` + `sojourn.koin` |
| `:core:domain` | Use cases, one class each with `operator fun invoke`: `SearchStays`, `GetStayDetails`, `GetQuote`, `ToggleSaved`, `ObserveSaved`, `ListBookings`, `CreateBooking`, `ObserveFilters`, `UpdateFilters`, `GetCities`. Business rules and their tests. Koin `domainModule`. | `:core:data` (interfaces), core-base | `sojourn.kmp.library` + `sojourn.koin` |
| `:core:testing` | Fixtures (`StayFixtures`, `QuoteFixtures`, `FixedAppClock`), `TestDispatcherProvider` + `runSojournTest`, `SojournScreenshotTest` (parameterized Roborazzi + Robolectric base); fake repositories and the Koin test module from Phase 4. Test-scoped only. | everything in `core`, test libraries | `sojourn.kmp.library` + `sojourn.compose` |

## Tooling and build

| Path | Owns |
|---|---|
| `build-logic/` | Convention plugins (`sojourn.*`), the token generator (`GenerateDesignTokensTask`) and the resource check (`VerifyPluralsTask`) with their unit tests, shared detekt and Roborazzi configuration. Included build; its tests run in the root `check`. |
| `tooling/scripts` | `lucide_to_vector.py` (icon set → vector drawables) and `make_brand.py` (mark, wordmark, Android and iOS app icons from the tokens). |
| `tooling/konsist` | Architecture tests over the whole repository (module graph, naming, forbidden imports). Runs in `check`. |
| `tooling/detekt-rules` | The `sojourn` detekt rule set: `TokensOnly`, `MotionTokensOnly`, `StartEndOnly`, `NoTextLiterals`, `InsetsOnlyInOwners`, with tests. Applied to every module via `detektPlugins` in `sojourn.detekt`; scopes in `config/detekt/detekt.yml`. |
| `design/tokens.json` | Verbatim copy of the design system's token file. Input to `sojourn.tokens` and `make_brand.py`. |
| `design/brand/` | Generated mark, wordmark and lockup (light and dark). |
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
| `sojourn.koin` | `koin-core`; modules that test a graph add `koin-test` themselves |
| `sojourn.sqldelight` | SQLDelight plugin, `SojournDatabase` config, drivers per target |
| `sojourn.screenshot` | Roborazzi plugin on the AGP KMP library's `androidHostTest` (`recordRoborazziAndroidHostTest` / `verifyRoborazziAndroidHostTest`), baselines in `src/androidHostTest/screenshots`, `core:testing` for the base class, an Espresso 3.7 constraint for the API 37 runtime |
| `sojourn.tokens` | `generateDesignTokens` task reading `design/tokens.json` into `build/generated/tokens`, added to `commonMain` so every compile depends on it |
| `sojourn.localization` | `verifyPlurals` in `check`: every locale declares the same string and plural keys; Arabic plurals carry all six forms |
| `sojourn.detekt` | detekt with Compose rules and the project config |
| `sojourn.dokka` | Dokka for `core:*` public API |

## Adding a module

1. Decide its layer; if it does not fit a row above, it probably needs an ADR.
2. Create `path/build.gradle.kts` with only `plugins { id("sojourn.…") }` and its own dependencies.
3. Add it to `settings.gradle.kts`, to the Konsist module graph test, and to this page.
4. Add a `README.md` in the module and a Koin module if it provides anything.
