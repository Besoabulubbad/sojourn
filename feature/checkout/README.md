# feature:checkout

Implements the design-system card **ScreenCheckout** (with its states from **ScreenStates**): one review screen before payment, and the confirmation after it. The total here is the total on the card, the pin and the booking bar (P-02).

- **Page, in the card's order:** the stay (`SojournStaySummary`), the trip (dates and guests, each with "Edit"), the price (`SojournPriceBreakdown` open, never behind a link: every line of the total, and the refundable deposit apart from it), then the cancellation terms (`SojournPolicyRow`).
- **Edits in place:** "Edit" opens the dates picker or the guests steppers over the page. Dates the stay cannot take are refused in its own numbers before the network is asked, and the guest steppers stop at what the stay sleeps. "Update the total" prices the change; the page changes only once the new total is known, and says what it was ("Updated for your changes. It was £1,383.").
- **Pay:** "Pay £1,383" spans the docked bar (`SojournActionBar` with `fillPrimary`) and shows progress while paying. Payment goes through the platform payment sheet (`PaymentSheet`, `core:platform`), then `CreateBooking`. Success plays the success haptic and the confirmation takes checkout's place, so back never returns to a paid checkout.
- **When it does not book**, the guest stays here with the page intact and the reason above the button, each saying nothing was charged: the payment was declined; the device is offline; the booking could not be confirmed; or the total changed while paying (the quote had expired), in which case the new breakdown and total are shown and the guest pays again. Closing the payment sheet is not an error.
- **States:** the page's shape while loading; offline, the cached page under the banner; a quote the device no longer has says the price expired, with a way back; any other error offers "Try again".
- **Confirmation** (`BookingConfirmedKey`): not on the card yet, built from its components: "You're booked", the reference as written, the stay, dates, guests and the total paid; "View booking" opens `BookingDetailsKey` and "Done" returns to the start of the tab. It reads the booking from the device, so it works offline.
- **Not yet:** the real Google Pay / Apple Pay sheets and card entry (Phase 6; `StubPaymentSheet` approves every request until then), and sign-in before paying (with Profile). "View booking" opens the Bookings screen once that feature lands.

**Navigation keys** (declared in `core:navigation`): `CheckoutKey(quoteId)`, `BookingConfirmedKey(bookingId)`.

**Shape:** `CheckoutUiState` · `CheckoutSheet` · `CheckoutNotice` · `CheckoutIntent` · `CheckoutEffect` · `CheckoutViewModel(key, …)` · `CheckoutRoute` · `CheckoutScreen`; `ConfirmationUiState` · `ConfirmationViewModel(key, …)` · `ConfirmationRoute` · `ConfirmationScreen` · `checkoutModule` (keys come in with `parametersOf`) · `EntryProviderScope<NavKey>.checkoutEntries(navigator)`. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:platform`, `core:model`, `core:common`; never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Tests:** `CheckoutViewModelTest` (the card's quote, an expired quote, paying and booking, no double payment, re-priced dates, refused guests, a declined payment, a closed payment sheet, a price changed while paying, booking offline), `ConfirmationViewModelTest`, and `checkoutSnapshots`: every state, each failure, both editors and the confirmation, on Android and iOS.
