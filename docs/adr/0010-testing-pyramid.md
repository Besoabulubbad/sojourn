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
- Neutral: Roborazzi can also render iOS Compose (`roborazzi-compose-ios`); not enabled initially.

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
- `SojournScreenshotTest` is a parameterized Robolectric test (one capture per variant per test) that renders through a compose test rule and captures the node, the pattern Roborazzi documents for Compose.

## Links

- [Roborazzi](https://takahirom.github.io/roborazzi/) · [Robolectric](https://robolectric.org/)
- [Konsist](https://docs.konsist.lemonappdev.com/)
- [Compose Multiplatform UI testing](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html)
- [Maestro](https://docs.maestro.dev/)
- Design system: `docs/design-system/engineering-standards.md` (Test matrix)
- Related: ADR 0006, ADR 0011
