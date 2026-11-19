# core:testing

Test support shared by every module. Test-scoped only: production code never depends on it.

| Thing | Source set | Role |
|---|---|---|
| `SnapshotCase` | `commonMain` | One visual state (name, `fullMatrix`, content), written once in a module's `commonTest`. |
| `SojournScreenshotSuite` + `screenshotParameters` | `androidMain` | Android runner: one Robolectric test per case and variant (4, or 16 with `fullMatrix`), API 36, native graphics, fixed device; `screenshots/androidHostTest/`. |
| `verifySnapshots` | `iosMain` | iOS runner: every case in the four shipped variants on Compose's Skiko test runtime, 412 × 915 dp at 2×; `screenshots/iosSimulatorArm64/`; lists every mismatch. |
| `StayFixtures`, `QuoteFixtures` | `commonMain` | Sample catalogue data; the £1,383 fortnight the brand book uses everywhere. |
| `FixedAppClock` | `commonMain` | A clock that never moves. |
| `TestDispatcherProvider`, `runSojournTest` | `commonMain` | Test dispatchers everywhere and `Dispatchers.Main` set for `viewModelScope`. |
| exposed (`api`) | | coroutines-test, core:designsystem; Robolectric, JUnit 4 and the compose test rule on Android. |

| `FakeCityRepository`, `FakeStayRepository`, `FakePricingRepository`, `FakeBookingRepository`, `FakeSavedRepository`, `FakeSearchRepository`, `FakePreferencesRepository` | `commonMain` (`testing.data`) | In-memory `core:data` repositories for use case and ViewModel tests. Each keeps the cache (`cache`) separate from what the network returns (`remote`); `refreshResult`, `offline` and `latency` drive the offline-first states. They live outside `core.data` so feature tests use them without importing data infrastructure (Konsist). |

**May depend on:** every `core:*` module and the test libraries.
