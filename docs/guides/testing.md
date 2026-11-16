# Testing

What we test, with which tool, where it lives and when it runs. The reasoning is in [ADR 0010](../adr/0010-testing-pyramid.md); the matrix comes from the design system's engineering standards.

## The layers

| Layer | Tool | Lives in | Runs |
|---|---|---|---|
| Unit | kotlin-test · Turbine · kotlinx-coroutines-test | `src/commonTest` of each module | PR |
| Architecture | Konsist | `tooling/konsist/src/test` | PR |
| Screenshot | Roborazzi + Robolectric | `src/androidHostTest` of `core:designsystem`, `core:ui`, each `feature:*` | PR |
| Compose UI | Compose UI Test | `src/androidDeviceTest` | `main`, nightly (emulator) |
| End-to-end | Maestro | `.maestro/` | `main`, nightly (emulator) |
| Static | detekt · dependency-analysis | — | PR |

## Commands

```bash
./gradlew check                       # everything on the PR gate
./gradlew allTests                    # unit tests for all available targets
./gradlew :feature:details:allTests   # one module
./gradlew :tooling:konsist:test
./gradlew verifyRoborazziAndroidHostTest    # compare with baselines (check does this too)
./gradlew recordRoborazziAndroidHostTest    # re-record baselines
./gradlew compareRoborazziAndroidHostTest   # produce diff images without failing
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
- every `*ViewModel` has a `*ViewModelTest`; every public `Sojourn*` composable has a screenshot test.

Add a rule when a convention is written down anywhere in `docs/`.

## Screenshot tests (Roborazzi)

The matrix for every component and every screen state:

| Dimension | Values |
|---|---|
| Language | `en-GB`, `ar` (sets `LayoutDirection`) |
| Theme | light, dark |
| Font scale | 1.0, 2.0 |
| Width | 320 dp, 412 dp |
| State | each `ScreenState`, each component state |

`SojournVariants` in `core:designsystem` defines the four shipped variants (`Four`) and the 16-cell `Matrix`. A screenshot test extends `SojournScreenshotTest` from `core:testing`, which is parameterized over the variants, so each variant is its own test with one capture:

```kotlin
class TokenSheetScreenshotTest(variant: SojournVariant) : SojournScreenshotTest(variant) {
    @Test fun colors() = snapshot("tokens_color") { SojournTokenSheet(TokenSection.Color) }
}
```

The base class is parameterized over all 16 cells of the matrix. `snapshot(name)` captures a state in the four shipped variants, and `snapshot(name, fullMatrix = true)` adds 200 % font and 320 dp width. Use `fullMatrix` for each component's and screen's default state; the other cells are skipped (ADR 0010).

`snapshot` renders the content through `SojournPreview` (theme, language, direction, font scale, width) with a compose test rule and captures the node with Roborazzi, writing `<name>_<variant>.png`. Each module's baselines live in `<module>/src/androidHostTest/screenshots/` (configured by `sojourn.screenshot`) and are committed. Plain test runs verify against them because `gradle.properties` sets `roborazzi.test.verify=true`, so `check` fails on any visual change.

Robolectric renders on the **API 36** runtime (`@Config(sdk = [36])`) with native graphics and a fixed `w412dp-h915dp-xxhdpi` device. Robolectric 4.18's API 37 runtime intermittently dropped Compose frames before capture: blank `#FAFAFA` images, reproduced with a plain coloured box and no app code. Unit tests are unaffected. Revisit when Robolectric ships its next release.

**Baseline policy.** A PR that changes any baseline must say so and why; the reviewer looks at the diff images (uploaded as a CI artefact on failure, or regenerate locally with `compareRoborazziDebug`). Recording new baselines to make CI pass without a reviewed visual change is not allowed (`CLAUDE.md`).

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
