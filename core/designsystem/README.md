# core:designsystem

The design system in code (ADR 0003, 0008): generated tokens, `SojournTheme`, fonts, the icon set, preview and screenshot variants, and from Phase 3 the 26 `Sojourn*` components. The **only** module allowed to import Material 3 (Konsist).

## What is here

| Thing | Source | Role |
|---|---|---|
| `SojournColors`, `SojournType`, `SojournSpacing`, `SojournShapes`, `SojournElevation`, `SojournSizes`, `SojournMotion` | **generated** by `sojourn.tokens` from `design/tokens.json` into `build/generated/tokens` | Every token by name; each object also exposes `all` (name → value) for parity tests and the token sheet. Never edited by hand. |
| `SojournTheme(locale, darkTheme, reduceMotion) { }` | `theme/SojournTheme.kt` | Provides colours, type and elevation for the theme, the locale environment (via `ProvideAppLocale`), `LocalReduceMotion`, and a Material theme bridged from the same tokens. Read with `SojournTheme.colors.brand`, `SojournTheme.type.price`. |
| `SojournFonts` | `theme/SojournFonts.kt` + `composeResources/font` | Alexandria (display, titles, price) and Readex Pro (text) at 400/500/600. Both cover Latin and Arabic. |
| Motion helpers | `theme/SojournMotionExtensions.kt` | `settleSpring()`, `popSpring()`, `exitDuration()`, and `enter`/`exit`/`move` specs that fall back to a 150 ms fade when reduce motion is on (M-03). |
| `SojournIcon` | `icon/SojournIcon.kt` + `composeResources/drawable` | Sealed set of 58 Lucide icons (24 dp, 1.75 stroke) plus filled variants where a card needs them (`StarFilled`, `SavedFilled`); `NoResults` (Lucide `search-x`) since the Feedback group. Only Back, Forward and the start/end chevrons mirror, inside the composable (L-05, N-02). |
| `SojournVariant`, `SojournVariants`, `SojournPreview` | `preview/` | The 4-variant set and 16-cell matrix (language × theme × font scale × width) and the wrapper previews and screenshot tests share. |
| `SojournTokenSheet` | `showcase/` | Every token on one sheet per family; the Phase 2 screenshot exit test and the showcase swatches. |

## Components

One package per component under `components/<name>/`: `Sojourn<Comp>.kt` (the composable), its variant types and samples in their own files, `Sojourn<Comp>Previews.kt`, and `Sojourn<Comp>Snapshots.kt` in `commonTest` (screenshots on Android and iOS).

### Price — `SojournPrice(price: PriceDisplay, modifier, size = SojournPriceSize.Card)`

The only component allowed to print a price (P-01). A `PriceDisplay.Total` leads with the stay total ("£1,383 total") over the all-in nightly average ("£92 a night, all fees included"). A raw nightly rate that excludes fees is never shown. A `PriceDisplay.From` reads "from £2,450 a month" over what it excludes ("Before cleaning fee. Add dates for the exact total."), and never drops "from" (P-03). Amounts come from `SojournFormatter` inside bidi isolates and sentences from string resources (S-02, S-04, P-06). `Compact` shows the lead line only. Screen readers read both lines as one. Screenshots cover dated (full 16-cell matrix), from-a-month, from-a-night with two exclusions, and compact.

### Rating — `SojournRating(rating: Rating?, modifier, showCount = true)`

A filled star in `accentInk`, the score and the review count: ★ 4.8 (32). Renders **nothing** for a missing rating or fewer than three reviews (N-04); there is no empty, zero or "new" rating. Screen readers hear one phrase, "Rated 4.8 out of 5, 32 reviews", from a plural resource with all six Arabic forms. Score and count come from `SojournFormatter` (one decimal, Western digits, bidi isolates). The 14 dp star is `SojournRatingDefaults.StarSize`, smaller than the `icon-sm` token so it sits inside `body-sm` text. `SojournRatingBehaviourTest` checks the hidden states and the spoken label on the semantics tree.

### Button — `SojournButton(text, onClick, modifier, variant, leadingIcon, loading, enabled)`

The one way to trigger an action. `Primary` (`brand`), `Secondary` (`surface` + `borderControl`), `Ghost` (inline, no fill) and `Danger` (`dangerFill`). At least `touch-min` tall; labels wrap instead of truncating. Disabled uses `surfaceSunken` with `inkMuted` text (still 4.5:1), but a working button is preferred. `loading` keeps the label and width, shows a spinner, announces "Loading" and blocks a second tap. Pressing scales to 0.98 and darkens 8 % over `dur-instant` (no scale under reduce motion); keyboard focus draws the 2 dp `focus` ring 2 dp outside.

### Chip — `SojournChip(label, selected, onClick, modifier, icon)` and `SojournFactChip(label, modifier, icon)`

A compact choice. Selected is a filled `ink` chip with a semibold label, so state is never colour alone (C-04); it is a selectable element, so screen readers hear "selected". 40 dp visual height inside a 48 dp target. `SojournFactChip` is the read-only tinted chip (`brandTint`, `brandInk`).

### IconButton — `SojournIconButton(icon, contentDescription, onClick, modifier, variant, tint, badge, enabled)`

An icon-only action with a 48 dp target; `contentDescription` is a required parameter (C-05). `Plain`, `Outline` and `Floating` (its own `surface` disc with `shadow-2`, for photos and the map). `tint = Saved` draws the filled heart in `save`. `badge` shows a count at the top-end corner, formatted by `SojournFormatter` and announced after the description.

### Skeleton — `SojournSkeleton(modifier, shape)` and `SojournStayCardSkeleton(modifier)`

Loading placeholders shaped like the content they replace; no full-screen spinners. `surfaceSunken` with a `hairline` pulse every 1.4 s, still under reduce motion. Skeletons are hidden from screen readers; `SojournStayCardSkeleton` announces "Loading" once for the whole card.

### Snackbar — `SojournSnackbar`, `SojournSnackbarHost` + `SojournSnackbarHostState`, `SojournOfflineBanner`

A brief confirmation with an optional action ("Removed from Saved · Undo"): an `ink` bar, announced politely. `SojournSnackbarHostState.show(message, actionLabel)` queues one at a time, keeps it 5 seconds unless the action is taken, and returns `ActionPerformed` or `Dismissed` (our own queue, so Material stays out of the public API). `SojournOfflineBanner` is the standing offline condition in `dangerTint`; it takes its place in the layout and never overlays content.

### EmptyState — `SojournEmptyState(icon, title, body, modifier, action)`

What a screen shows when there is nothing to list: a 64 dp `brandTint` icon disc, a heading that says what happened, a body that says what to do, and at most one `SojournEmptyStateAction` that changes the outcome (a secondary button).

## Tests

- `TokenParityTest`: one property per token, both themes share the token set, dark is a full theme (only saffron is shared), motion ceiling is `dur-slower`, only directional icons mirror.
- `ContrastTest`: every text pair named in the token usage notes meets 4.5:1 and every graphic pair 3:1, in both themes (C-02).
- Screenshots: `designSystemSnapshots` (`commonTest`) lists every case (the five token-sheet sections, each component's states). `DesignSystemScreenshots` renders them on Android and `DesignSystemScreenshotsIos` on iOS; baselines in `screenshots/androidHostTest/` and `screenshots/iosSimulatorArm64/`.
- `AppLanguagePairsTest`: the four device/app language pairs (EN/EN, EN/AR, AR/EN, AR/AR) render the app language's text and direction through `SojournTheme` (L-03).
- Every public `Sojourn*` component has `SnapshotCase`s (Konsist fails the build otherwise).

## Third-party assets

Fonts (SIL OFL) and icons (ISC) with versions and licences: [THIRD_PARTY.md](THIRD_PARTY.md).

**May depend on:** core:localization (`api`: `AppLocale` is in the theme's signature), Compose, Material 3 (implementation only), Coil (Phase 3).
