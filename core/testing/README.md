# core:testing

Test support shared by every module. Test-scoped only: production code never depends on it.

| Thing | Source set | Role |
|---|---|---|
| `SojournScreenshotTest` | `androidMain` | Base class for Roborazzi tests: `snapshot("name") { … }` renders a state through `SojournPreview` for each variant (default: the four shipped variants; `SojournVariants.Matrix` for all 16) and records/verifies `src/androidHostTest/screenshots/<name>_<variant>.png`. Robolectric API 37, native graphics, fixed device. |
| `StayFixtures`, `QuoteFixtures` | `commonMain` | Sample catalogue data; the £1,383 fortnight the brand book uses everywhere. |
| `FixedAppClock` | `commonMain` | A clock that never moves. |
| `TestDispatcherProvider`, `runSojournTest` | `commonMain` | Test dispatchers everywhere and `Dispatchers.Main` set for `viewModelScope`. |
| re-exported (`api`) | | kotlin-test, coroutines-test, Turbine, koin-test; Roborazzi, Robolectric, JUnit 4 on Android. |

Fake repositories and the Koin test module arrive with the data layer (Phase 4).

**May depend on:** every `core:*` module and the test libraries.
