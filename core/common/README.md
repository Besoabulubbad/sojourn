# core:common

Cross-cutting plumbing with no product meaning:

- `DispatcherProvider` / `DefaultDispatcherProvider` — the only place `Dispatchers` is referenced; injected everywhere else.
- `AppClock` / `SystemAppClock` — "now" and "today" over `kotlin.time.Clock` and the app time zone; tests use a fixed clock from `core:testing`.
- `AppResult<T>` / `AppError` — success or a typed failure across the domain boundary. `AppError.Offline` maps to the offline screen state (I-05); `runCatchingApp { }` wraps unexpected exceptions and rethrows cancellation.
- `SojournLog` — the Kermit facade: `SojournLog.tagged("StayRepository")`; `configure(debug)` once at start.

**May depend on:** coroutines, kotlinx-datetime, Kermit (all re-exported as `api`).
