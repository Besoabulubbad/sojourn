# 0007 — Offline-first data with SQLDelight and a Ktor-ready API seam

- **Status:** accepted
- **Date:** 2026-11-15
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** architecture, data

## Context and problem statement

The demo has no live backend. It still has to show loading, empty, error and offline states convincingly (I-05, the States screen), keep "one price everywhere" consistent across surfaces (P-02), and be deterministic on stage. We need to decide where data lives, what the source of truth is, and how a real API would plug in later.

## Decision drivers

- Deterministic, offline, fast demo — no network flakiness on stage.
- Real persistence and offline-first patterns worth showing.
- A clean seam so a real API is a configuration change.
- Multiplatform drivers that are stable on Android and iOS.

## Options considered

1. **Mock repository over bundled JSON** — in-memory, no persistence.
2. **Local database (SQLDelight), seeded, as source of truth** with an API seam (`SojournApi`) implemented locally for now.
3. **Real Ktor API** against a hosted or mock server.

## Decision outcome

**Chosen: Option 2.** SQLDelight 2.4.1 is the source of truth; repositories in `core:data` expose `Flow`s from the database; a refresh pulls from the `SojournApi` seam and writes back. `SojournApi` is implemented by `LocalSojournApi` (bundled JSON with simulated latency and injectable failures) today and `KtorSojournApi` (configured, flag-gated) tomorrow. Features never know which.

Why SQLDelight over Room (which also supports KMP now): SQL-first schema and queries we can read in review, mature `native-driver` on iOS, migrations as `.sqm` files, `coroutines-extensions` for `Flow`, and a JVM `sqlite-driver` for fast tests. Room KMP is a reasonable alternative and would be a contained change inside `core:database`.

### Consequences

- Good: the offline state is a real state (cached content + banner), not a spinner.
- Good: loading/error/offline are reproducible in tests and demos via `FakeNetworkConditions`.
- Good: the Ktor client, serialization and DTO mappers are real; swapping to a backend touches `core:network` only.
- Good: pricing invariants (breakdown sums to total; same quote id across card, pin, bar, checkout, pay) are tested at the repository/use-case level.
- Bad: more infrastructure than a mock. Mitigated by the `sojourn.sqldelight` convention plugin and a small schema.
- Bad: seed data has to be authored (cities, ~40 stays, photos, policies, quotes). Mitigated by generating it once from the design system's sample data and committing it.
- Neutral: SQLDelight generates code at build time; IDE needs a Gradle sync to see it.

### Confirmation

- `core:data` tests: reads come from the database; a network failure leaves cached content visible with `Offline` state.
- `core:domain` tests: P-03 (`PriceDisplay.From` requires exclusions), P-05 (breakdown lines sum), N-04 (ratings hidden under 3 reviews).
- Konsist: only `core:data` depends on `core:database` and `core:network`.

## Pros and cons of the options

### Option 1 — Mock repository over JSON

- Good: fastest, simplest, fully deterministic.
- Bad: no persistence story; offline is fake; saved/bookings do not survive restart.

### Option 2 — SQLDelight + API seam

- Good: real offline-first; deterministic; swappable; demonstrates data layer craft.
- Bad: more setup; seed authoring.

### Option 3 — Real Ktor API

- Good: most "real".
- Bad: no real backend; a hosted mock adds infrastructure and live-demo risk.

**2026-11-18 (Results).** Notes, decision unchanged:
- Quotes store when they were priced (`quote.priced_at`, schema version 3). Offline, Results labels a cached dated price older than an hour with its age, as the ScreenStates card requires.
- Results does not page yet. The bundled catalogue answers a city in one call (under ten stays) and the list is lazy, so skeleton rows for data already on the device would be theatre. The card's "20 at a time with skeleton rows at the end" lands when `SojournApi` gains pages.

**2026-11-18 (Details).** Notes, decision unchanged:
- Schema version 4 adds what a stay's price includes (`stay_inclusion`), the server's translations of its description (`description_translation`) and its latest reviews (`review`). Reviews come with the stay's own refresh, like blocked dates; the catalogue carries all three (`make_catalog.py`, from a second seed so no existing value moved).
- Schema version 5 keeps the search a stay was saved from (`saved.check_in`, `check_out`, `adults`, `children`), so the Saved tab shows that search's total; saves from before it are undated, one adult.
- Schema version 7 keeps a booking's address (`booking.address`, the flat and building, sent once booked) and the stay's Wi‑Fi (`wifi_network`, `wifi_password`, shown from arrival day). The demo engine issues them with the booking; the buildings are made up and there is no street or number, so a demo address never points at a real home.
- Schema version 6 keeps a booking's self check-in key code (`booking.key_code`). A stay's blocked nights now include the guest's own bookings there that are not cancelled (`takenFor`), so the picker, quotes and search never offer nights the guest holds, even though the demo API forgets its bookings between launches.
- A stay row is written as insert-if-absent then update, never `INSERT OR REPLACE`. A replace deletes the row first, and the cascade dropped the children a list refresh does not carry: blocked dates and reviews disappeared whenever a city refreshed. `clearChildren` no longer clears blocked dates for the same reason; the stay's own refresh replaces them.

## Links

- [SQLDelight 2.x — Multiplatform SQLite](https://sqldelight.github.io/sqldelight/2.4.1/multiplatform_sqlite/)
- [Ktor Client — multiplatform](https://ktor.io/docs/client-create-multiplatform-application.html)
- [Android — offline-first guidance](https://developer.android.com/topic/architecture/data-layer/offline-first)
- Related: ADR 0003, ADR 0010
