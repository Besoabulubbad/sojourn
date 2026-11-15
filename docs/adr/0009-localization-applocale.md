# 0009 — Localization: `AppLocale` as the single source for language and direction

- **Status:** accepted
- **Date:** 2026-11-15
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** localization, architecture

## Context and problem statement

The catalogue spans London, Paris, Lisbon and three Algerian cities; the design system treats Arabic and English as equals and its design rationale lists the failures common in rental apps: direction ignoring the in-app language, half-translated Arabic, reversed numbers, unmirrored icons, fixed-string plurals, flags for languages. Standards L-01…L-06 and S-01…S-08 specify the fix. We need the mechanism.

## Decision drivers

- One source of truth for language that drives text, direction, digits, dates, money and plurals together (L-01).
- Works identically on Android and iOS, whatever the device language.
- Six Arabic plural forms, bidi-safe insertion of prices and counters.
- Enforceable: nothing outside the localization module reads the device locale.

## Options considered

1. **Device locale only**, relying on OS per-app language settings.
2. **`AppLocale` in `core:localization` as the single source**, synced to the OS per-app-language APIs so system UI follows, with `SojournFormatter` for all formatting and Compose Resources for strings/plurals.
3. **In-app language only**, ignoring OS settings entirely.

## Decision outcome

**Chosen: Option 2.**

- `AppLocale` (`EN_GB`, `AR`) is persisted (`multiplatform-settings`) and exposed as `LocalAppLocale`. `SojournTheme` sets `LayoutDirection` from it.
- Strings and plurals are Compose Multiplatform resources in `core:localization` (`values/strings.xml`, `values-ar/strings.xml`, plurals with `zero|one|two|few|many|other` for `ar`). A Gradle verification task fails the build if any `ar` plural is missing a form (S-03).
- `SojournFormatter` is the only formatter: money (`£1,383` / `1,383 £`), date ranges with en dash, counts, "1 / 13" counters, all wrapped in bidi isolates (S-04); Western digits in both languages.
- `HostText` renders host-written text in its own language and direction with a translate affordance (S-06).
- Changing the language calls the OS per-app-language API (Android `LocaleManager`; iOS `AppleLanguages` override + relaunch prompt where required) so keyboards and system dialogs follow, while the app itself reads only `AppLocale`.
- Language choices are shown as names in their own script; no flags (L-06).

### Consequences

- Good: those failures become structurally impossible; the four device/app language pairs are tested.
- Good: Both platforms share the formatter and resources.
- Bad: Compose Resources plural/format features must be verified for the 1.12 version during Phase 2 (e.g. `pluralStringResource`, placeholders). Mitigated by the official resources docs and unit tests.
- Bad: iOS per-app language behaviour differs from Android (system setting vs in-app). Mitigated: the in-app switch is the primary path on both; OS sync is best-effort.
- Neutral: Catalog text (cities, amenities, policies) is localized by id (S-05); seed data carries keys, not names.

### Confirmation

- Konsist: `Locale`, `NSLocale`, `java.text`/`java.util` locale APIs only in `core:localization`.
- Gradle task `verifyPlurals` in `check`.
- `SojournFormatter` tests for `en-GB` and `ar` including isolate characters.
- Instrumented/Robolectric test over EN/EN, EN/AR, AR/EN, AR/AR (L-03).
- Screenshot matrix includes Arabic for every component and screen.

## Pros and cons of the options

### Option 1 — Device locale only

- Good: no custom state.
- Bad: exactly the classic failure mode; inconsistent between platforms; cannot preview Arabic in English devices easily.

### Option 2 — `AppLocale` single source, OS-synced

- Good: deterministic, testable, shared; system UI still follows.
- Bad: small sync layer per platform.

### Option 3 — In-app only

- Good: simplest control.
- Bad: keyboards, system dialogs and share sheets in the wrong language; feels broken.

## Links

- [Compose Multiplatform resources — strings and plurals](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-resources-usage.html)
- [Android per-app language preferences](https://developer.android.com/guide/topics/resources/app-languages)
- [Unicode bidi isolates (UAX #9)](https://www.unicode.org/reports/tr9/)
- Design system: `docs/design-system/engineering-standards.md` (L-*, S-*), brand book glossary
- Related: ADR 0008, ADR 0010
