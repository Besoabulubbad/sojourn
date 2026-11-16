# Testing

What we test, with which tool, where it lives and when it runs. The reasoning is in [ADR 0010](../adr/0010-testing-pyramid.md); the matrix comes from the design system's engineering standards.

## The layers

| Layer | Tool | Lives in | Runs |
|---|---|---|---|
| Unit | kotlin-test · Turbine · kotlinx-coroutines-test | `src/commonTest` of each module | PR |
| Architecture | Konsist | `tooling/konsist/src/test` | PR |
| Screenshot | Roborazzi: Robolectric (Android), Skiko test runtime on the simulator (iOS) | cases in `src/commonTest` of `core:designsystem`, `core:ui`, each `feature:*`; baselines in `screenshots/` | PR (Linux job and macOS job) |
| Compose UI | Compose UI Test | `src/androidDeviceTest` | `main`, nightly (emulator) |
| End-to-end | Maestro | `.maestro/` | `main`, nightly (emulator) |
| Static | detekt · dependency-analysis | — | PR |

## Commands

```bash
./gradlew check                       # everything on the PR gate
./gradlew allTests                    # unit tests for all available targets
./gradlew :feature:details:allTests   # one module
./gradlew :tooling:konsist:test
./gradlew verifyRoborazziAndroidHostTest    # compare Android screenshots with baselines (check does this too)
./gradlew iosSimulatorArm64Test             # iOS unit tests and iOS screenshot verify (needs Xcode)
./gradlew connectedAndroidTest        # Compose UI tests on a connected device/emulator
maestro test .maestro/
```

## Unit tests

- ViewModels: `given state and intent, expect state`. Use `MainDispatcherRule` and fakes from `core:testing`; assert with Turbine on `state` and `effects`.
- Use cases and repositories: fixtures from `core:testing`; the pricing invariants (P-03, P-05) and content rules (N-01, N-04) live in `core:domain` tests.
- Formatters: `SojournFormatter` tests for `en-GB` and `ar` assert exact strings including bidi isolate characters.
- Navigation: every `SojournNavKey` round-trips through `sojournNavSerializersModule`.
- DI: `checkModules()` in `app:shared`.

Name tests by behaviour: `` `offline with cached stays shows Offline state with content` ``.

## Architecture tests (Konsist)

Live in `tooling/konsist`. They assert, over the whole repo:

- the allowed-dependency matrix from [modules.md](../architecture/modules.md);
- no Material imports outside `core:designsystem`; no platform locale APIs outside `core:localization`; no `KoinComponent` outside `app:shared`; no `NavKey` outside `core:navigation`;
- naming: `*ViewModel : SojournViewModel`, `*UiState`, `*Intent`, `*Effect`, `*Route`, `*Screen`, `Sojourn*` components;
- every `*ViewModel` has a `*ViewModelTest`; every public `Sojourn*` composable has a `SnapshotCase`.

Add a rule when a convention is written down anywhere in `docs/`.

## Screenshot tests (Roborazzi, Android and iOS)

Every visual state is written **once**, as a `SnapshotCase` in the module's `commonTest`, and rendered on both platforms:

| | Android | iOS |
|---|---|---|
| Runner | `SojournScreenshotSuite` (`core:testing`, Robolectric) | `verifySnapshots` (`core:testing`, Compose's Skiko test runtime on the simulator) |
| Variants per state | the four shipped variants; the default state in the full 16-cell matrix | the four shipped variants |
| Device | `w412dp-h915dp-xxhdpi`, Robolectric API 36 | 412 × 915 dp at 3× |
| Baselines | `<module>/screenshots/androidHostTest/` | `<module>/screenshots/iosSimulatorArm64/` |
| Runs | `check` on the Linux PR job | `iosSimulatorArm64Test` on the macOS PR job |

The matrix dimensions:

| Dimension | Values |
|---|---|
| Language | `en-GB`, `ar` (sets `LayoutDirection`) |
| Theme | light, dark |
| Font scale | 1.0, 2.0 (Android, default state) |
| Width | 320 dp, 412 dp (Android, default state) |
| State | each `ScreenState`, each component state |

Writing cases (`commonTest`):

```kotlin
val priceSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("price_dated", fullMatrix = true) { SojournPrice(SojournPriceSamples.dated) },
    SnapshotCase("price_from_month") { SojournPrice(SojournPriceSamples.fromMonth) },
)
```

Each module lists its cases in one place (`designSystemSnapshots`, `uiSnapshots`) and has a two-line runner per platform: a `SojournScreenshotSuite` subclass in `androidHostTest` and a test calling `verifySnapshots(…)` in `iosTest`. Every case renders through `SojournPreview` (theme, language, direction, font scale, width) and is captured from its node, as `<name>_<variant>.png`. Plain test runs verify against the committed baselines because `gradle.properties` sets `roborazzi.test.verify=true`. A changed image fails `check` on Android and `iosSimulatorArm64Test` on iOS, and the iOS runner lists every mismatch, not just the first.

```bash
./gradlew recordRoborazziAndroidHostTest        # re-record Android baselines
./gradlew recordRoborazziIosSimulatorArm64      # re-record iOS baselines (needs Xcode)
./gradlew compareRoborazziAndroidHostTest       # diff images without failing
./gradlew iosSimulatorArm64Test                 # iOS unit tests + iOS screenshot verify
```

**Platform notes.** Robolectric renders on the API 36 runtime: Robolectric 4.18's API 37 runtime intermittently dropped Compose frames before capture (blank `#FAFAFA` images, reproduced with a plain coloured box). Roborazzi's iOS support is experimental; it needs an explicit file name per capture, which the runner derives from the case and variant. Elevation shadows render fainter on iOS than on Android; the baselines record each platform as it is.

**Baseline policy.** A PR that changes any baseline must say so and why; the reviewer looks at the diff images (uploaded as CI artefacts on failure, `screenshot-diffs` and `ios-screenshot-diffs`, or regenerate locally with the compare tasks). Recording new baselines to make CI pass without a reviewed visual change is not allowed (`CLAUDE.md`).

### Device and app language

`AppLanguagePairsTest` (`core:designsystem`) sets the device language with Robolectric qualifiers and `Locale.setDefault`, sets the app language through `SojournTheme`, and asserts the text and layout direction for EN/EN, EN/AR, AR/EN and AR/AR (L-03). Tests are the one place outside `core:localization` allowed to touch platform locale APIs.

## Compose UI tests

For interactions screenshots cannot prove: sheet drag detents, gallery swipe, stepper taps, date range selection, back handling. Keep them few and valuable; they run on an emulator in `main.yml`.

## End-to-end (Maestro)

Flows in `.maestro/`:

- `booking-golden-path.yaml` — Explore → search London for a month → Results → Details → Checkout → confirmation; asserts the same price text on card, booking bar and pay button (P-02).
- `language-switch.yaml` — switch to Arabic in Profile; assert direction and a translated title; switch back.

Run locally against an emulator; CI runs them after the Android build on `main`.

## Flakiness policy

A flaky test is a bug. Quarantine by `@Ignore("flaky: #issue")` with an issue, fix within the sprint, never retry-loop in CI for unit/screenshot layers. Emulator jobs may retry once.
