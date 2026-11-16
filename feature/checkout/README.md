# feature:checkout

Implements the design-system card **ScreenCheckout** — one review screen before payment, and the confirmation.

**Navigation keys** (declared in `core:navigation`): `CheckoutKey(quoteId)`, `BookingConfirmedKey(bookingId)`

**Shape:** `CheckoutUiState` · `CheckoutIntent` · `CheckoutEffect` · `CheckoutViewModel : SojournViewModel` · `CheckoutRoute` (stateful) · `CheckoutScreen` (stateless) · `checkoutModule` · `EntryProviderBuilder<NavKey>.checkoutEntries(navigator)`. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:platform`, `core:model`, `core:common` — never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Status:** skeleton (Phase 1); built in Phase 5. Definition of Done applies before merge.
