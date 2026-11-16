# Tech stack

Every library and tool, the version pinned in `gradle/libs.versions.toml`, and where it was verified. **Verified 2026-11-15, additions 2026-11-16** against Maven Central / Google Maven metadata and the vendors' documentation — not from memory. Renovate keeps this current; a bump that crosses a major version needs a note in the PR and, if it changes a pattern, an ADR update.

Rules for this table: stable releases only (no alpha/beta/RC) unless marked; every entry has official documentation we follow during implementation.

## Language, build, platform

| Tool | Version | Notes | Docs |
|---|---|---|---|
| Kotlin | 2.4.21 | Latest stable (2.5.0 at RC) | [kotlinlang.org](https://kotlinlang.org/docs/home.html) |
| Gradle | 9.7.1 | Wrapper; configuration cache, build cache and parallel on. Newest release inside KGP 2.4.21's fully supported range (≤ 9.7.0 documented; 9.7.1 is its patch) | [docs.gradle.org](https://docs.gradle.org/current/userguide/userguide.html) · [KGP compatibility](https://kotlinlang.org/docs/gradle-configure-project.html) |
| Android Gradle Plugin | 9.3.1 | Bump from 9.1.1; the newest AGP inside KGP 2.4.21's documented window (8.5.2–9.3.1). AGP 9.5.1 exists but requires Gradle ≥ 9.6 and is outside that window, so it waits for the Kotlin release that lists it. Uses `com.android.kotlin.multiplatform.library` and the AGP 9 DSL | [AGP release notes](https://developer.android.com/build/releases/gradle-plugin) |
| JDK (toolchain) | 21 (Azul, via Foojay) | `gradle/gradle-daemon-jvm.properties`; JVM target 11 for Android bytecode | — |
| compileSdk / targetSdk / minSdk | 37 / 37 / 24 | Android 17 SDK present locally | — |
| KSP | 2.3.14 | For Koin Annotations if adopted; version is independent of Kotlin since 2.x | [KSP](https://kotlinlang.org/docs/ksp-overview.html) |
| Compose Multiplatform | 1.12.2 | Gradle plugin + compiler plugin from Kotlin | [JetBrains CMP](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform.html) |
| Compose Material 3 (JetBrains) | 1.12.0-alpha03 *(pre-release)* | Already in the skeleton; the only Material 3 line published for CMP 1.12 — stable 1.13 lands with CMP 1.13 | [Material 3 CMP](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-material3.html) |
| Material 3 Adaptive (JetBrains) | 1.3.0 | List-detail / supporting pane for large screens if used | — |
| Compose Hot Reload | 1.3.0-rc01 *(pre-release)* | Developer convenience only, not on CI | [Hot Reload](https://github.com/JetBrains/compose-hot-reload) |

## Architecture libraries

| Library | Version | Artifact(s) | Docs |
|---|---|---|---|
| Lifecycle ViewModel (JetBrains) | 2.11.0 | `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel` (base class in `core:ui`), `lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`, `lifecycle-viewmodel-navigation3` | [Lifecycle CMP](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-lifecycle.html) |
| **Navigation 3 UI** (JetBrains) | **1.2.0** | `org.jetbrains.androidx.navigation3:navigation3-ui` — declares `androidx.navigation3:navigation3-runtime:1.2.0` and `org.jetbrains.androidx.navigationevent:navigationevent-compose:1.1.0` as transitive deps | [Nav3 in CMP](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html) · [Android Nav3](https://developer.android.com/guide/navigation/navigation-3) · [CMP recipes](https://github.com/terrakok/nav3-recipes) |
| Navigation 3 runtime (Google, KMP) | 1.2.1 | `androidx.navigation3:navigation3-runtime` (publishes iOS/desktop/wasm variants) | same |
| SavedState (Google, KMP) | 1.4.0 | `androidx.savedstate:savedstate` — `SavedStateConfiguration` for the iOS-safe back stack | [Nav3 save state](https://developer.android.com/guide/navigation/navigation-3/save-state) |
| Koin | 4.2.2 | `koin-core`, `koin-compose`, `koin-compose-viewmodel`, `koin-compose-navigation3`, `koin-test` | [insert-koin.io](https://insert-koin.io/docs/reference/koin-compose/compose) |
| Koin Annotations (optional) | 4.2.2 + compiler 2.3.1 | `koin-annotations`, `koin-ksp-compiler` — only if we adopt compile-time graph checks | [Koin Annotations](https://insert-koin.io/docs/reference/koin-annotations/start) |
| kotlinx.coroutines | 1.11.0 | `-core`, `-test` | [coroutines](https://kotlinlang.org/docs/coroutines-guide.html) |
| kotlinx.serialization | 1.13.0 | `-core` (navigation keys), `-json` (tests, network); build-logic parses `tokens.json` with it | [serialization](https://github.com/Kotlin/kotlinx.serialization) |
| kotlinx-datetime | 0.8.0 | `Instant`/`LocalDate`; note `kotlin.time.Instant` migration in 0.7+ | [datetime](https://github.com/Kotlin/kotlinx-datetime) |
| SQLDelight | 2.4.1 | plugin `app.cash.sqldelight`; `android-driver`, `native-driver`, `coroutines-extensions`, `primitive-adapters`; `sqlite-driver` for JVM tests | [sqldelight](https://sqldelight.github.io/sqldelight/2.4.1/multiplatform_sqlite/) |
| Ktor Client | 3.6.1 | `-core`, `-content-negotiation`, `-serialization-kotlinx-json`, `-logging`; engines `-okhttp` (Android), `-darwin` (iOS) | [ktor.io](https://ktor.io/docs/client-create-multiplatform-application.html) |
| Coil | 3.6.3 | `coil-compose`, `coil-network-ktor3` | [coil-kt.github.io](https://coil-kt.github.io/coil/compose/) |
| Kermit | 2.2.0 | `kermit`, `kermit-core` — logging facade in `core:common` | [Kermit](https://kermit.touchlab.co/) |
| multiplatform-settings | 1.3.0 | preferences (language, theme); `-test` (`MapSettings`) in tests | [multiplatform-settings](https://github.com/russhwolf/multiplatform-settings) |

## Testing

| Tool | Version | Role | Docs |
|---|---|---|---|
| kotlin-test | 2.4.21 | assertions, common tests | — |
| Turbine | 1.2.1 | `Flow` assertions | [Turbine](https://github.com/cashapp/turbine) |
| kotlinx-coroutines-test | 1.11.0 | `runTest`, dispatcher control | — |
| Robolectric | 4.18 | JVM Android runtime for screenshot tests. Screenshots render on API 36: the API 37 runtime dropped Compose frames before capture (see the testing guide) | [robolectric.org](https://robolectric.org/) |
| Roborazzi | 1.79.0 | screenshot record/verify. Its Gradle plugin supports the AGP KMP library plugin (`recordRoborazziAndroidHostTest`, verified in the plugin source and its integration sample); `roborazzi-compose-ios` available if iOS screenshots are added | [Roborazzi](https://takahirom.github.io/roborazzi/) |
| ComposablePreviewScanner | 0.10.0 | drive screenshot tests from `@Preview`s | [CPS](https://github.com/sergio-sastre/ComposablePreviewScanner) |
| Konsist | 0.18.1 | architecture tests (1.0.0-beta.1 exists; stay on stable) | [Konsist](https://docs.konsist.lemonappdev.com/) |
| Compose UI Test | from CMP 1.12.2 | `ui-test-junit4` (screenshot base class, device tests); `androidx.compose.ui:ui-test-manifest` 1.12.1 registers the test activity (the androidx version CMP 1.12.2 resolves to) | [CMP UI testing](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html) |
| Espresso | 3.7.0 | constraint only: Roborazzi's stack resolves 3.5.1, which cannot start on API 37 | — |
| Maestro | CLI 2.12.1 | end-to-end flows | [maestro.dev](https://docs.maestro.dev/) |

## Quality and tooling

| Tool | Version | Role | Docs |
|---|---|---|---|
| detekt | 1.23.8 | single linter; formatting rules via `detekt-formatting` | [detekt.dev](https://detekt.dev/) |
| Compose rules for detekt (nlopez) | 0.6.8 | `io.nlopez.compose.rules:detekt` | [mrmans0n/compose-rules](https://mrmans0n.github.io/compose-rules/) |
| Gradle dependency-analysis | 3.9.0 | `buildHealth` — unused/misdeclared deps; applied per module; Compose Multiplatform bundle configured at the root. Its authors test up to AGP 9.1.0; runs on 9.3.1 with a warning (Renovate tracks the plugin) | [dependency-analysis-gradle-plugin](https://github.com/autonomousapps/dependency-analysis-gradle-plugin) |
| Dokka | 2.3.0 | API docs for `core:*` → `/api` on the site | [Dokka](https://kotlinlang.org/docs/dokka-introduction.html) |
| MkDocs Material | 9.8.1 | docs site — EOL announced 2027-05-05; successor [Zensical](https://zensical.org) reads the same `mkdocs.yml` (see ADR 0012) | [squidfunk.github.io/mkdocs-material](https://squidfunk.github.io/mkdocs-material/) |
| Renovate | GitHub App | dependency PRs, weekly, grouped | [Renovate](https://docs.renovatebot.com/) |

## Assets

| Asset | Version | Licence | Source |
|---|---|---|---|
| Alexandria, Readex Pro | google/fonts, fetched 2026-11-16 | SIL OFL 1.1 | static 400/500/600 instances cut with fontTools ([THIRD_PARTY](https://github.com/Besoabulubbad/sojourn/blob/main/core/designsystem/THIRD_PARTY.md)) |
| Lucide icons (58) | lucide-static 1.73.0 | ISC | converted to vector drawables by `tooling/scripts/lucide_to_vector.py` |
| release-please-action | v5.0.0 | versions + CHANGELOG from Conventional Commits | [release-please](https://github.com/googleapis/release-please-action) |

## GitHub Actions (pinned major tags, verified 2026-11-15)

| Action | Tag |
|---|---|
| `actions/checkout` | v7 (7.0.1) |
| `actions/setup-java` | v6 (6.3.0) |
| `gradle/actions/setup-gradle` | v6 (6.4.1) |
| `actions/upload-artifact` | v7 (7.0.2) |
| `mikepenz/action-junit-report` | v6 (6.7.1) |
| `anthropics/claude-code-action` | v1 |
| `googleapis/release-please-action` | v5 |

## Deliberately not used

| Instead of | We use | Because |
|---|---|---|
| Hilt / Dagger | Koin | not multiplatform; see ADR 0004 |
| Navigation 2 (`navigation-compose`) | Navigation 3 | back stack as state, scenes; see ADR 0005 |
| Room | SQLDelight | SQL-first, mature KMP drivers; see ADR 0007 |
| Retrofit / OkHttp directly, Moshi / Gson | Ktor + kotlinx.serialization | multiplatform |
| ktlint standalone | detekt + formatting | one tool, one config |
| Paparazzi | Roborazzi | multiplatform-friendly, works with Robolectric and CMP |
