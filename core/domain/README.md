# core:domain

Use cases: one class each, with one `operator fun invoke`. Pure Kotlin over `core:data`'s repository interfaces. Features call these and never a repository (Konsist enforces it). Reads are offline-first (ADR 0007): they return `Flow<Loadable<T>>`, which holds the cached data, whether a refresh is running, and the last error. `core:ui`'s `ScreenState.from` renders it the same way on every screen.

| Use case | Returns | Rule it owns |
|---|---|---|
| `GetCities(force)` | `Flow<Loadable<List<City>>>` | Cached cities at once; an empty list means Empty only after a successful refresh. |
| `SearchStays(query, filters, force)` | `Flow<Loadable<SearchResults>>` | No dates: "from" prices, per month by default and per night for a weekend or week (P-03). With dates: each matching stay is quoted, stays the dates don't work for are dropped, and each card carries its quote id (P-02). A price range applies only on the cards' basis (P-04). Sorting by rating ranks hidden ratings last (N-04). |
| `GetStayDetails(id, force)` | `Flow<Loadable<StayDetails>>` | Stay, blocked dates, saved state, rating shown only from 3 reviews (N-04). A stay gone from the catalogue ends as `NotFound`. |
| `GetQuote(stay, dates, guests)` | `AppResult<Quote>` | Checks on the device first, so they work offline: dates in the past, minimum stay, capacity, booked dates. Then prices through the API. |
| `ObserveQuote(id)` | `Flow<Quote?>` | One quote for every surface that shows the total (P-02). |
| `CreateBooking(quoteId)` | `AppResult<Booking>` | Charges the total the guest saw. If the quote expired, re-prices it and books only if the total is unchanged; otherwise returns `Validation("price_changed")`. |
| `ListBookings()` | `Flow<List<BookingItem>>` | Status follows the calendar. Order: active, upcoming (soonest first), past (latest first). |
| `ToggleSaved(id)`, `ObserveSaved()` | `Boolean`, `Flow<List<StayResult>>` | Local and works offline. Newest saved first, with monthly "from" prices. |
| `ObserveFilters()`, `UpdateFilters(filters)` | `StateFlow<Filters>` | Filters survive a restart. |
| `ObserveRecentSearches(limit)`, `RecordSearch(query)` | `Flow<List<SearchQuery>>` | Repeats move to the top. |

`StayRules` holds the shared pure rules:
- The card price basis.
- A monthly "from" price: 30 nights less the long-stay discount, the same estimate the catalogue's city prices use.
- Visible ratings (N-04).
- Booking problems checked on the device, which use the API's reason ids.

`domainModule` binds every use case as a factory; it needs `dataModule` and an `AppClock`. `app:shared`'s `SojournModulesTest` verifies the whole graph with Koin's `verify()`.

## Tests

The tests run use cases over the fake repositories in `core:testing` (`testing.data`), on the Android host and iOS.

- `StayRulesTest`: the basis per query, monthly prices with and without a discount, N-04, booking problems including an adjacent booked range.
- `OfflineFirstTest`: first launch, a cache with a failed refresh, offline with no cache, a confirmed-empty result, retry.
- `SearchStaysTest`: P-03, P-04 and P-02 on cards, capacity and filters, dropped stays, offline with and without stored quotes, sorting, hearts.
- `StayAndBookingUseCasesTest`: details, quote checks, booking at the shown total and at an expired quote's price (same or changed), the bookings calendar, saved.

**May depend on:** core:data (interfaces), core:model, core:common.
