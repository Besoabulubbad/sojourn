# feature:bookings

Implements the design-system card **ScreenBookings** — upcoming and past stays.

**Navigation keys** (declared in `core:navigation`): `BookingsKey`, `BookingDetailsKey(id)`

**Shape:** `BookingsUiState` · `BookingsIntent` · `BookingsEffect` · `BookingsViewModel : SojournViewModel` · `BookingsRoute` (stateful) · `BookingsScreen` (stateless) · `bookingsModule` · `EntryProviderBuilder<NavKey>.bookingsEntries(navigator)`. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:platform`, `core:model`, `core:common` — never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Status:** skeleton (Phase 1); built in Phase 5. Definition of Done applies before merge.
