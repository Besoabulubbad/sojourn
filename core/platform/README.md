# core:platform

The places where Android and iOS differ (brand book, Platform differences), behind small interfaces so shared code and tests never touch a platform API.

| Interface | Android | iOS |
|---|---|---|
| `Haptics` (`tick`, `success`, `error`) | predefined vibration effects (API 29+, silent below) | `UIImpactFeedbackGenerator` light / `UINotificationFeedbackGenerator` |
| `ShareSheet` | `ACTION_SEND` chooser | `UIActivityViewController` |
| `MapsLauncher` | `geo:` intent | Apple Maps URL |
| `AccessibilitySettings.reduceMotion()` | animator duration scale is 0 | `UIAccessibilityIsReduceMotionEnabled()` |
| `PaymentSheet` | `StubPaymentSheet` (Checkout shows the demo wallet, ADR 0018) | `StubPaymentSheet` |
| `CalendarSaver` (`add(CalendarStay)`) | the calendar app's `ACTION_INSERT` screen, all-day; needs the `<queries>` entry in the app manifest (package visibility) | `EKEventEditViewController` (EventKitUI), all-day; no calendar access needed on iOS 17+ |

`StubPaymentSheet` authorizes every request with a deterministic token so the checkout flow can be shown end to end. No money moves in this demo. Checkout shows a labelled demo wallet before it (ADR 0018); a real wallet would need a merchant account and replaces both.

`platformModule` (expect/actual) registers the implementations with Koin.

**May depend on:** core:model (`api`), Koin.
