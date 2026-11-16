# core:testing

Test support shared by every module. Test-scoped only: production code never depends on it.

| Thing | Source set | Role |
|---|---|---|
| `SnapshotCase` | `commonMain` | One visual state (name, `fullMatrix`, content), written once in a module's `commonTest`. |
| `SojournScreenshotSuite` + `screenshotParameters` | `androidMain` | Android runner: one Robolectric test per case and variant (4, or 16 with `fullMatrix`), API 36, native graphics, fixed device; `screenshots/androidHostTest/`. |
| `verifySnapshots` | `iosMain` | iOS runner: every case in the four shipped variants on Compose's Skiko test runtime, 412 × 915 dp at 3×; `screenshots/iosSimulatorArm64/`; lists every mismatch. |
| `StayFixtures`, `QuoteFixtures` | `commonMain` | Sample catalogue data; the £1,383 fortnight the brand book uses everywhere. |
| `FixedAppClock` | `commonMain` | A clock that never moves. |
| `TestDispatcherProvider`, `runSojournTest` | `commonMain` | Test dispatchers everywhere and `Dispatchers.Main` set for `viewModelScope`. |
| exposed (`api`) | | coroutines-test, core:designsystem; Robolectric, JUnit 4 and the compose test rule on Android. |

Fake repositories and the Koin test module arrive with the data layer (Phase 4).

**May depend on:** every `core:*` module and the test libraries.
