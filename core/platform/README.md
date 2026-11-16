# core:platform

The places where Android and iOS differ (brand book, Platform differences), behind small interfaces so shared code and tests never touch a platform API.

| Interface | Android | iOS |
|---|---|---|
| `Haptics` (`tick`, `success`, `error`) | predefined vibration effects (API 29+, silent below) | `UIImpactFeedbackGenerator` light / `UINotificationFeedbackGenerator` |
| `ShareSheet` | `ACTION_SEND` chooser | `UIActivityViewController` |
| `MapsLauncher` | `geo:` intent | Apple Maps URL |
| `AccessibilitySettings.reduceMotion()` | animator duration scale is 0 | `UIAccessibilityIsReduceMotionEnabled()` |
| `PaymentSheet` | `StubPaymentSheet` until Phase 6 | `StubPaymentSheet` until Phase 6 |

`StubPaymentSheet` authorizes every request with a deterministic token so the checkout flow can be shown end to end. No money moves in this demo; the real Google Pay and Apple Pay sheets need merchant accounts.

`platformModule` (expect/actual) registers the implementations with Koin.

**May depend on:** core:model (`api`), Koin.
