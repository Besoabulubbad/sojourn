# 0010 — Testing pyramid: unit, architecture, screenshot matrix, UI, end-to-end

- **Status:** accepted
- **Date:** 2026-11-15
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** quality, testing

## Context and problem statement

Eleven screens × two languages × two themes × font scale × width is a matrix no human re-checks by hand. The design system's standards demand a specific test matrix and name tests as the enforcement for most rules. We need to decide which test layers exist, where they run, and what they cost.

## Decision drivers

- Catch visual regressions across the full variant matrix automatically.
- Enforce architecture rules, not just document them.
- Fast feedback on PRs; expensive jobs where they pay off.
- Multiplatform: most tests must run on the JVM against shared code.

## Options considered

1. **Full pyramid** — unit · Konsist architecture · Roborazzi screenshot matrix · Compose UI tests · Maestro end-to-end.
2. **Unit + screenshot + architecture** — no device/emulator jobs.
3. **Unit + Compose UI tests** — classic Android approach.

## Decision outcome

**Chosen: Option 1, the full pyramid.**

| Layer | Tool | Where it lives | Runs |
|---|---|---|---|
| Unit | kotlin-test, Turbine, coroutines-test | `commonTest` in every module | every PR |
| Architecture | Konsist 0.18.1 | `tooling/konsist` (JVM) | every PR |
| Screenshot | Roborazzi 1.79 + Robolectric 4.18 (API 37 runtime available) | `androidHostTest` of `core:designsystem`, `core:ui`, every `feature:*`; baselines committed per module | every PR (`verifyRoborazziDebug`) |
| Compose UI | Compose UI Test (CMP 1.12) | `androidDeviceTest` for key interactions | `main.yml` on an emulator; nightly |
| End-to-end | Maestro 2.12 | `.maestro/*.yaml` | `main.yml` on an emulator; nightly |

**Screenshot matrix** (from the design system): 2 languages × 2 themes × font scale 100%/200% × widths 320/412 dp × each `ScreenState`. Implemented as parameterised Roborazzi tests fed by `SojournVariants` in `core:ui` and `UiState` fixtures in `core:testing`. Baselines are committed; a changed baseline requires an explicit PR note and review (a designer's approval where one exists).

### Consequences

- Good: every rule in the standards has a test home; visual regressions in Arabic or dark mode are caught before review.
- Good: screenshot tests run on the JVM — no emulator for the PR gate; shared UI means one suite covers both platforms' Compose.
- Good: the screenshot set doubles as living documentation of every component and state.
- Bad: baseline churn when the design changes. Mitigated by per-module baselines, the review rule, and recording only via a reviewed re-record.
- Bad: emulator jobs are slow and occasionally flaky. Mitigated by keeping them off the PR gate (`main.yml`/nightly), retries, and a small Maestro suite (golden path + language switch).
- Bad: Robolectric images are not pixel-identical to devices. Acceptable for regression detection; device checks happen in Phase 6.
- Neutral: Roborazzi can also render iOS Compose (`roborazzi-compose-ios`); enabled from Phase 3 (see the implementation notes).

### Confirmation

- `pr.yml` fails on any test layer except UI/E2E; `main.yml` adds them.
- Coverage is not gated by percentage; instead Konsist asserts every `ViewModel` has a test class and every `Sojourn*` component has a screenshot test.

## Pros and cons of the options

### Option 1 — Full pyramid

- Good: complete safety net; demonstrates mature practice.
- Bad: most CI time; emulator maintenance.

### Option 2 — Unit + screenshot + architecture

- Good: ~80% of the value, no emulators, fast and cheap.
- Bad: no end-to-end confidence; interactions (drag, swipe, back) untested.

### Option 3 — Unit + Compose UI tests

- Good: familiar.
- Bad: no visual regression across themes/RTL; no architecture enforcement; slow device tests on the PR path.

## Implementation notes

**2026-11-16 (Phase 2).** Verified against the Roborazzi plugin source: the Gradle plugin configures the AGP KMP library plugin and registers `recordRoborazziAndroidHostTest` / `verifyRoborazziAndroidHostTest`, so screenshots live in each module's `androidHostTest` as planned. Two findings changed the details, not the decision:

- Screenshots render on the **API 36** Robolectric runtime. On Robolectric 4.18's API 37 runtime, Compose content was intermittently not drawn before capture (blank `#FAFAFA` images, reproduced with a plain coloured box). API 36 renders every probe. Unit tests keep the default runtime.
- `SojournScreenshotTest` is a parameterized Robolectric test (one capture per variant per test) that renders through a compose test rule and captures the node, the pattern Roborazzi documents for Compose. (Since replaced by `SnapshotCase` with per-platform runners; see the iOS note below.)

**2026-11-16 (Phase 3 start), matrix size.** Decided with the maintainer: every state of every component and screen is captured in the four shipped variants (EN/AR × light/dark), and each component's and screen's **default state** additionally runs the full 16-cell matrix (200 % font, 320 dp width). That catches RTL, theme and overflow problems at about a third of the images a full matrix for every state would need. A state opts into the extra 12 cells with `fullMatrix = true` (now on `SnapshotCase`).

**2026-11-16, iOS screenshots (decided with the maintainer).** iOS screenshots are in scope, replacing "`roborazzi-compose-ios` not enabled initially". Each state is written once as a `SnapshotCase` in `commonTest`. Android renders it with `SojournScreenshotSuite` (Robolectric) and iOS with `verifySnapshots` (Roborazzi's experimental iOS support on Compose's Skiko test runtime, simulator, 412 × 915 dp at 3×). iOS covers every state in the four shipped variants; 200 % font and 320 dp stay Android-only because the layout that would wrap or clip is the same shared code. A spike confirmed bundled fonts and Arabic resources load in the iOS test binary. iOS runs on **every PR** in a macOS job (`iosSimulatorArm64Test`), in parallel with the Linux job; since a later decision that day it is informational rather than required (ADR 0011 note). Baselines live in `<module>/screenshots/<androidHostTest|iosSimulatorArm64>/`, with Roborazzi's `separateOutputDirs` (which also stops the two platforms' finalizers racing over one directory under `check`).

**2026-11-16, iOS comparison tolerance.** iOS screenshots compare with a 0.5 % pixel tolerance (`changeThreshold`); Android stays exact. After a new iOS 27.2 simulator runtime appeared on the development machine, the Kotlin plugin's default simulator changed and text and icon edges anti-aliased slightly differently: 0.07–0.15 % of pixels, alpha unchanged, on baselines CI had verified exactly. Roborazzi's iOS documentation uses the same mechanism (its example is 1 %). A changed glyph, colour or layout moves far more pixels than 0.5 %.

**2026-11-16, language pairs.** `AppLanguagePairsTest` in `core:designsystem` runs the four device/app language pairs on Robolectric (L-03). It was added after the Price screenshots showed Arabic layout with English text. Compose Multiplatform resources read the platform default locale, so `ProvideAppLocale` now sets it, following JetBrains' resource-environment guide. With that line removed, the test fails the two mismatched pairs.

**2026-11-19, iOS tests run from a release test binary.** From #27 (Map) on, the iOS job was cancelled at its 60-minute limit on every PR; its tests passed, but the later modules never ran. The cause was not the map: each iOS screenshot took about 13 s, because the tests ran from a debug (unoptimised) Kotlin/Native binary, and rendering, encoding and comparing a 1236 × 2745 px image is slow there. The job grew past the hour as screens were added. Modules with screenshots (`sojourn.screenshot`) now also build a release test binary and run `iosSimulatorArm64Test` from it: Across all modules, time inside the iOS tests went from 3,780 s to 142 s (Profile's 24 screenshots: 308 s to 10 s), with every baseline passing unchanged. A release link runs the optimiser in the Gradle daemon and needs several GB; a dozen in parallel ran out of heap, so release test links take turns through one shared build service (`maxParallelUsages = 1`). The whole local iOS run now takes about 26 minutes, most of it those links.

**2026-11-19, iOS screenshots at 2× (decided with the maintainer); the release binary is reverted.** The release test binary fixed the time locally but not on CI: on the 3-core, 7 GB macOS runner one release link took 32 minutes (180 s and a 3.5 GB compiler locally), and the job was cancelled at 60 minutes again. The cost of an iOS capture in the debug binary grows with its pixels. Measured on Profile's 24 captures: 3× 308 s, 2× 39 s, 1× 14 s. iOS now renders at 2× (824 × 1830 px) from the ordinary debug binary, and every iOS baseline was re-recorded and reviewed. 2× keeps text and icons reviewable; Android stays 3× and exact. The release-binary configuration and its link service are removed.

## Links

- [Roborazzi](https://takahirom.github.io/roborazzi/) · [Robolectric](https://robolectric.org/)
- [Konsist](https://docs.konsist.lemonappdev.com/)
- [Compose Multiplatform UI testing](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html)
- [Maestro](https://docs.maestro.dev/)
- Design system: `docs/design-system/engineering-standards.md` (Test matrix)
- Related: ADR 0006, ADR 0011
