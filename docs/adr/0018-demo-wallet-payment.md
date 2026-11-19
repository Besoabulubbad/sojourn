# 0018 — Payment: a labelled demo wallet, no real wallet brands

- **Status:** accepted
- **Date:** 2026-11-19
- **Deciders:** maintainer, with Claude Code
- **Tags:** checkout, platform, branding

## Context and problem statement

The ScreenCheckout card says payment uses the platform sheet first (Google Pay on Android, Apple Pay on iOS), and that the pay button repeats the amount ("Pay £1,383"). The plan listed "Google Pay / Apple Pay sheet stubs" for Phase 6, and the architecture docs named a payment adapter in each shell. Sojourn is a public demo with no merchant account. The official wallet buttons carry fixed brand labels, so they cannot say "Pay £1,383", and CLAUDE.md keeps real companies' branding out of the product.

## Decision drivers

- The guest must never think money moved: the demo has no merchant account.
- "Pay £1,383" repeats the total on the page and on the step that confirms it (P-02, ScreenCheckout).
- No real company's name, logo or button style in the product (CLAUDE.md).
- The checkout flow keeps the platform-sheet step, so a real wallet can replace it later behind `PaymentSheet`.

## Options considered

1. **Official wallet buttons** (Apple's `PKPaymentButton`, Google's Compose pay button) opening a demo sheet: real look, but a new dependency, real branding, and no amount on the button.
2. **Official button plus "Pay £1,383 by card"**: as option 1, with two pay actions on one screen.
3. **Keep "Pay £1,383"; it opens our own demo wallet sheet**, which says plainly that it stands in for the phone's wallet and that no money moves.
4. **Keep the silent stub**: payment authorizes with no visible step.

## Decision outcome

**Chosen: option 3**, decided with the maintainer. It keeps the card's pay button and the platform-sheet step, uses no real brand, and adds no dependency.

- `SojournWalletSheet` (core:designsystem) shows a demo note, who is paid, a demo card, the amount through `SojournPrice`, and the same "Pay £1,383". It opens at full height.
- Checkout's "Pay" opens it (`CheckoutUiState.wallet`). Its "Pay" runs the existing `PaymentSheet` seam (still `StubPaymentSheet`), then books. Closing it any way charges nothing.

### Consequences

- Good: the guest sees the wallet step and is told it is a demo; the total appears on the page, in the wallet and on its button.
- Good: a real wallet later replaces the demo sheet and `StubPaymentSheet` behind the same intents; the screen does not change.
- Bad: it does not look like the real Google Pay or Apple Pay sheet. Accepted for a demo.
- The shells have no payment adapters for now; `docs/architecture` says so.

### Confirmation

- `CheckoutViewModelTest`: "Pay" opens the wallet without paying, closing it charges nothing, and only its "Pay" pays and books.
- Screenshots: `wallet_sheet` (design system, the full matrix) and `checkout_wallet` (Checkout), on Android and iOS.
- Review: no real wallet brand, logo or button in strings or drawables.

## Links

- Design system: ScreenCheckout card, the brand book's platform differences.
- `docs/design-system/mapping.md` (WalletSheet row); ADR 0015 (our own sheet).
