# core:data

Repositories: the only module that sees `core:database` and `core:network` (ADR 0007). **SQLDelight is the source of truth.** Reads are `Flow`s from the database that re-emit on change; a refresh pulls from `SojournApi` and writes back in one transaction. A screen never waits on the network to show what is cached, and a failed refresh never clears it.

| Repository | Reads | Writes |
|---|---|---|
| `CityRepository` | `observeCities()` (most stays first), `observeCity(id)` | `refresh(force)` replaces the cities |
| `StayRepository` | `observeStays(cityId?)`, `observeStays(ids)`, `observeStay(id)`, `observeBlockedDates(id)`, `hasCached(cityId?)` | `refresh(cityId?, force)` replaces the stays in scope; `refreshStay(id, force)` stores one stay and its blocked dates, and removes a stay the API reports `NotFound` |
| `PricingRepository` | `observeQuote(id)`: every surface reads the same quote by id (P-02) | `quote(stay, dates, guests)` stores what the API issues. **Offline**, it returns the stored quote for the same request if there is one |
| `BookingRepository` | `observeBookings()` (latest check-in first), `observeBooking(id)` | `book(quoteId)` needs the network and stores the booking; `NotFound` means the quote expired, so re-quote |
| `SavedRepository` | `observeSavedIds()` (newest first) | `toggle(id)` returns the new state; `setSaved(id, saved)`. Local only |
| `SearchRepository` | `observeRecent(limit)` | `record(query)` moves a repeat to the top and keeps the newest 10; `clear()`. Local only |
| `PreferencesRepository` | `filters: StateFlow<Filters>` | `setFilters(filters)`, persisted with multiplatform-settings so filters survive a restart |

## Refresh policy

`RefreshPolicy` decides when a refresh goes to the network. Screens ask for a refresh every time they appear; a scope refreshed successfully in the last 15 minutes is skipped unless `force` is set (pull to refresh, retry). Refreshes run one at a time, so two screens appearing together cause one call. Freshness is kept in memory, so each launch refreshes once.

A refresh returns `AppResult<Unit>`. The caller combines it with what is cached: a failure with cached content shows the content with the offline banner, a failure with nothing cached shows the offline or error state (I-05). Use cases in `core:domain` do this.

There is no separate seed loader. On first launch the first refresh fills the database from `LocalSojournApi`'s bundled catalogue.

## Storage rules

All model ↔ row conversion is in `local/RowMapping.kt`:

- Enums are stored by their stable ids, never ordinals.
- Dates are ISO-8601 text; money is minor units plus a currency code.
- A row the model would reject is dropped rather than crashing a screen, and the next refresh rewrites it. Examples: an unknown amenity or currency, or a quote whose lines do not sum to its total (P-05).

`dataModule` binds every repository. It needs `SojournDatabase` (`databaseModule`), `SojournApi` (`networkModule`), an `AppClock` and a `DispatcherProvider`. `app:shared` provides the clock and dispatchers; each platform provides the settings store.

## Tests

The tests run each repository over a real in-memory database and the real bundled catalogue (`LocalSojournApi` with no latency), on the Android host and iOS. `DataTestEnv` builds the stack the way `dataModule` does.

- `StayRepositoryTest`
  - All 40 stays round-trip unchanged.
  - City scopes.
  - Offline and server errors keep the cache.
  - The 15-minute policy.
  - Stays gone from the catalogue are removed.
  - Blocked dates are stored.
  - Lists re-emit (Turbine).
- `CityRepositoryTest`: round trip, order, offline.
- `PricingRepositoryTest`
  - The same quote reads back by id (P-02).
  - Every catalogue stay stores a quote that sums (P-05).
  - The offline fallback.
  - Validation errors.
  - A corrupted quote reads as missing.
- `BookingRepositoryTest`: the booking total equals the quote total (P-02), order, expired quote, offline.
- `LocalRepositoriesTest`: saved order and survival across refreshes, recent-search dedupe and cap, filters persisting across restarts.

**May depend on:** core:database, core:network, core:model, core:common, SQLDelight coroutines extensions, multiplatform-settings.
