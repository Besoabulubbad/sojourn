# core:designsystem

The design system in code (ADR 0003, 0008): generated tokens, `SojournTheme`, fonts, the icon set, preview and screenshot variants, and from Phase 3 the 26 `Sojourn*` components. The **only** module allowed to import Material 3 (Konsist).

## What is here

| Thing | Source | Role |
|---|---|---|
| `SojournColors`, `SojournType`, `SojournSpacing`, `SojournShapes`, `SojournElevation`, `SojournSizes`, `SojournMotion` | **generated** by `sojourn.tokens` from `design/tokens.json` into `build/generated/tokens` | Every token by name; each object also exposes `all` (name → value) for parity tests and the token sheet. Never edited by hand. |
| `SojournTheme(locale, darkTheme, reduceMotion) { }` | `theme/SojournTheme.kt` | Provides colours, type and elevation for the theme, the locale environment (via `ProvideAppLocale`), `LocalReduceMotion`, and a Material theme bridged from the same tokens. Read with `SojournTheme.colors.brand`, `SojournTheme.type.price`. |
| `SojournFonts` | `theme/SojournFonts.kt` + `composeResources/font` | Alexandria (display, titles, price) and Readex Pro (text) at 400/500/600. Both cover Latin and Arabic. |
| Motion helpers | `theme/SojournMotionExtensions.kt` | `settleSpring()`, `popSpring()`, `exitDuration()`, and `enter`/`exit`/`move` specs that fall back to a 150 ms fade when reduce motion is on (M-03). |
| `SojournIcon` | `icon/SojournIcon.kt` + `composeResources/drawable` | Sealed set of 58 Lucide icons (24 dp, 1.75 stroke). Only Back, Forward and the start/end chevrons mirror, inside the composable (L-05, N-02). |
| `SojournVariant`, `SojournVariants`, `SojournPreview` | `preview/` | The 4-variant set and 16-cell matrix (language × theme × font scale × width) and the wrapper previews and screenshot tests share. |
| `SojournTokenSheet` | `showcase/` | Every token on one sheet per family; the Phase 2 screenshot exit test and the showcase swatches. |

## Components

One package per component under `components/<name>/`: `Sojourn<Comp>.kt` (the composable), its variant types and samples in their own files, `Sojourn<Comp>Previews.kt`, and a screenshot test in `androidHostTest`.

### Price — `SojournPrice(price: PriceDisplay, modifier, size = SojournPriceSize.Card)`

The only component allowed to print a price (P-01). A `PriceDisplay.Total` leads with the stay total ("£1,383 total") over the all-in nightly average ("£92 a night, all fees included"). A raw nightly rate that excludes fees is never shown. A `PriceDisplay.From` reads "from £2,450 a month" over what it excludes ("Before cleaning fee. Add dates for the exact total."), and never drops "from" (P-03). Amounts come from `SojournFormatter` inside bidi isolates and sentences from string resources (S-02, S-04, P-06). `Compact` shows the lead line only. Screen readers read both lines as one. Screenshots cover dated (full 16-cell matrix), from-a-month, from-a-night with two exclusions, and compact.

## Tests

- `TokenParityTest`: one property per token, both themes share the token set, dark is a full theme (only saffron is shared), motion ceiling is `dur-slower`, only directional icons mirror.
- `ContrastTest`: every text pair named in the token usage notes meets 4.5:1 and every graphic pair 3:1, in both themes (C-02).
- `TokenSheetScreenshotTest`: the five token-sheet sections in the four variants, baselines in `src/androidHostTest/screenshots`.
- `AppLanguagePairsTest`: the four device/app language pairs (EN/EN, EN/AR, AR/EN, AR/AR) render the app language's text and direction through `SojournTheme` (L-03).
- One screenshot test per component (Konsist fails the build if a public `Sojourn*` component has none).

## Third-party assets

Fonts (SIL OFL) and icons (ISC) with versions and licences: [THIRD_PARTY.md](THIRD_PARTY.md).

**May depend on:** core:localization (`api`: `AppLocale` is in the theme's signature), Compose, Material 3 (implementation only), Coil (Phase 3).
