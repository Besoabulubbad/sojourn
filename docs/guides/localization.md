# Localization

English (`en-GB`) and Arabic (`ar`) are equals. The mechanism is in [ADR 0009](../adr/0009-localization-applocale.md); this page is the how-to.

## Model

- `AppLocale` (`core:localization`) is the single source: `EN_GB`, `AR`. Persisted; exposed as `LocalAppLocale`; `SojournTheme` sets `LayoutDirection` from it.
- Nothing outside `core:localization` touches platform locale APIs (Konsist).
- `SojournFormatter` is the only formatter for money, dates, ranges, counts and counters.
- Strings and plurals are Compose Multiplatform resources in `core:localization/src/commonMain/composeResources/`.

## Adding a string

1. Pick a key from the glossary stem if the concept exists (`stay`, `saved`, `bookings`, `filters`, `wifi`, `washer`, `workspace`, `studio`, `lift`, `refundable_deposit`, `reserve`). One term per concept per language; a second word for the same thing is a bug.
2. Add to `values/strings.xml` (en-GB) **and** `values-ar/strings.xml` in the same commit. `verifyPlurals` fails the build if a key exists in one locale and not the other.
3. Sentence case. Buttons are verb + object: "Show 14 stays", "Pay £1,383", "Choose dates". No exclamation marks, no emoji. Address the guest as "you".
4. Placeholders, never concatenation: `<string name="results_show_count">Show %1$s</string>` where `%1$s` is a formatted count from `SojournFormatter`.
5. Use in code: `stringResource(Res.string.results_show_count, formatter.count(n))`.

## Plurals

Every count uses a plural resource. Arabic needs **all six** CLDR forms; the `verifyPlurals` task fails the build otherwise. Plurals sit in the same `strings.xml` as the strings (Compose Multiplatform reads `<plurals>` from any file in `values*`).

```xml
<!-- values/strings.xml -->
<plurals name="nights">
  <item quantity="one">%1$s night</item>
  <item quantity="other">%1$s nights</item>
</plurals>
<!-- values-ar/strings.xml -->
<plurals name="nights">
  <item quantity="zero">لا ليالٍ</item>
  <item quantity="one">ليلة واحدة</item>
  <item quantity="two">ليلتان</item>
  <item quantity="few">%1$s ليالٍ</item>
  <item quantity="many">%1$s ليلة</item>
  <item quantity="other">%1$s ليلة</item>
</plurals>
```

Use `pluralStringResource(Res.plurals.nights, n, formatter.count(n))`.

## Numbers, dates, money

Through `SojournFormatter` only:

| Thing | en-GB | ar | Note |
|---|---|---|---|
| Money | `£1,383` · `DA 210,000` | `1,383 £` · `210,000 د.ج` | Western digits both; minor units only when non-zero; wrapped in a bidi isolate |
| Date range | `12–27 Mar` | `12–27 مارس` | en dash; month names from the locale |
| Counter | `1 / 13` | `1 / 13` | isolated so it does not reverse |
| Count | `3+` | `3+` | isolated |
| Week starts | Monday | Saturday | `AppLocale.weekStart` (L-04) |
| Time | `15:00` | `15:00` | 24-hour clock in both |

Month and weekday names are tables inside the formatter rather than platform calls, so the output is identical on Android, iOS and in JVM tests.

Bidi isolates: the formatter wraps inserted values in U+2068 (FSI) … U+2069 (PDI). Tests assert the characters are present.

## Layout direction

- Use `start`/`end` padding, alignment and arrangement. Never `left`/`right`, `absoluteOffset`, or `scaleX = -1f` (detekt).
- Icons: only `SojournIcon`s with `autoMirror = true` (Back, chevrons) mirror. Call sites cannot flip icons.
- Never mirror: the map, photos, phone numbers, booking references, the clock.
- Motion: forward moves toward the end edge; `SojournTransitions` read `LayoutDirection`.

## Host-written text

Titles and descriptions written by hosts render through `HostText` in their own language and direction, with a translate action. They are never forced to the UI direction or machine-translated silently.

## Changing the language

`AppLocaleController.set(AppLocale.AR)` updates the persisted value, recomposes the app, and asks the OS to set the per-app language through `PlatformLocaleSync` (Android `LocaleManager` on API 33+; iOS `AppleLanguages`, with a relaunch prompt if required) so keyboards and system dialogs follow. Inside the app, `ProvideAppLocale` (called by `SojournTheme`) makes resources resolve the chosen language. Compose Multiplatform 1.12 resolves `stringResource` from Compose's `Locale.current`, which reads the platform default, so `ProvideAppLocale` sets that default as JetBrains' resource-environment guide does: `Locale.setDefault` on Android, plus the composition's `Configuration` and `Context`, and the `AppleLanguages` default on iOS. `AppLanguagePairsTest` proves the four device/app pairs.

## In composables

- `rememberSojournFormatter()` gives the formatter for the current `LocalAppLocale`.
- `localizedList(names)` joins localized names from the `list_pair` / `list_series` resources: "cleaning fee and city tax", "رسوم التنظيف وضريبة المدينة".
- `formatter.moneyAverage(total, nights)` is the all-in nightly figure under a total, rounded to whole units (`£1,383` over 15 nights is `£92`). Use it only where the text says it is an average. The Profile screen lists languages by their own names — "English", "العربية" — no flags.

## Testing

- `SojournFormatterTest` for both locales.
- Screenshot matrix includes Arabic for every component and screen.
- The four device/app pairs (EN/EN, EN/AR, AR/EN, AR/AR) in Robolectric with device locale set and `AppLocale` set independently.
- Long Arabic strings and 200% font in previews to prove growth, not clipping.
