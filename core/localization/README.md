# core:localization

English (`en-GB`) and Arabic (`ar`) as equals (ADR 0009). The **only** module allowed to touch platform locale APIs (Konsist).

## What is here

| Thing | Role |
|---|---|
| `AppLocale` | `EN_GB` / `AR`: tag, layout direction, first day of week. The single source for language (L-01). |
| `LocalAppLocale` | Composition local; `SojournTheme` provides it through `ProvideAppLocale`. |
| `AppLocaleController` / `SettingsAppLocaleController` | Persisted choice (multiplatform-settings); `set()` recomposes the app and asks the OS to follow via `PlatformLocaleSync`. |
| `ProvideAppLocale` (expect/actual) | Android: overrides `LocalConfiguration`/`LocalContext` so Compose resources resolve `values-ar` whatever the device language. iOS: direction only; strings follow the `AppleLanguages` override. |
| `SojournFormatter` | Money, dates, ranges, counts, counters, times, month and weekday names. Bidi isolates around every inserted value (S-04). Pure tables, identical on every platform. |
| `composeResources/values*/strings.xml` | Strings and plurals, public `Res` class in `…core.localization.resources`. |
| `localizationModule` (expect/actual) | Koin: settings store, platform sync, controller. |
| `verifyPlurals` (Gradle, in `check`) | Every locale declares the same keys; Arabic plurals carry all six forms (S-03, S-05). |

## Formatting at a glance

| | en-GB | ar |
|---|---|---|
| Money | `£1,383` · `DA 210,000` | `1,383 £` · `210,000 د.ج` |
| Range | `12–27 Mar` · `28 Feb – 4 Mar` | `12–27 مارس` |
| Counter / plus | `1 / 13` · `3+` | same, isolated |
| Week starts | Monday | Saturday |

## Adding a string

1. Reuse the glossary stem if the concept exists. 2. Add it to **both** `values/strings.xml` and `values-ar/strings.xml` in the same commit (the build fails otherwise). 3. Placeholders, never concatenation: counts and amounts arrive pre-formatted from `SojournFormatter`. 4. Use `stringResource(Res.string.key, …)` / `pluralStringResource(Res.plurals.key, n, formatter.count(n))`.

See [docs/guides/localization.md](../../docs/guides/localization.md).
