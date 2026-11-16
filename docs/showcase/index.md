# Showcase

The visual record of the app as it is built — every component and screen in English/Arabic × light/dark, recordings of each flow, and the hero reel. Everything here is produced by the pipeline from the same screenshot tests that gate pull requests, so what you see is what CI verified.

!!! info "Filling in as the phases land"
    Assets appear in the order of the [plan](../plan.md): wordmark and theme swatches (Phase 2), the component contact sheet (Phase 3), per-screen grids and recordings (Phase 5), the hero reel and Android/iOS side-by-side (Phase 6). Until an asset exists its row below says *pending*.

## How the assets are produced

| Asset | Source | Command |
|---|---|---|
| Brand: lockup, mark, app icons | `design/tokens.json` + Alexandria | `python3 -I tooling/scripts/make_brand.py` |
| Token sheet | Roborazzi baselines in `core/designsystem` | copied from `src/androidHostTest/screenshots` (automated by `exportShowcase` in Phase 3) |
| Component contact sheet | Roborazzi baselines in `core/designsystem` | `./gradlew exportShowcase` |
| Screen grids (412 dp, 4 variants) | Roborazzi baselines in each `feature:*` | `./gradlew exportShowcase` |
| Flow recordings (GIF / WebP) | emulator screen recording while a Maestro flow drives the app, converted with ffmpeg | `scripts/record-flow.sh <flow>` |
| Hero reel | the booking golden path with a mid-flow theme and language switch | `scripts/record-flow.sh hero` |
| Social preview (1280 × 640) | composed from the hero frames and the wordmark | `scripts/social-preview.sh` |

Exports are committed under `docs/showcase/` so the README and this page work offline and in forks.

## Brand

The mark is a doorway arch with a lamp: a home base. It is drawn from the tokens alone (`brand`, `bg`, `accent`) by `tooling/scripts/make_brand.py`, which also writes the Android adaptive icon and the iOS light and dark app icons.

| Light | Dark |
|---|---|
| ![Sojourn lockup, light](brand/sojourn-lockup.svg){ width="320" } | ![Sojourn lockup, dark](brand/sojourn-lockup-dark.svg){ width="320" } |
| ![App icon, light](brand/sojourn-mark.svg){ width="96" } | ![App icon, dark](brand/sojourn-mark-dark.svg){ width="96" } |

### Theme tokens

Every token rendered by `SojournTheme`, exported from the `TokenSheetScreenshotTest` baselines in `core:designsystem` (English light and Arabic dark shown; all four variants are verified on every PR). The Arabic sheets show the whole layout mirrored, and the price in the type samples staying `£1,383` thanks to the formatter's bidi isolates.

| Family | EN · light | AR · dark |
|---|---|---|
| Colour | ![](tokens/color-en-light.png){ width="300" } | ![](tokens/color-ar-dark.png){ width="300" } |
| Type | ![](tokens/type-en-light.png){ width="300" } | ![](tokens/type-ar-dark.png){ width="300" } |
| Spacing | ![](tokens/spacing-en-light.png){ width="300" } | ![](tokens/spacing-ar-dark.png){ width="300" } |
| Radius · elevation | ![](tokens/shape-en-light.png){ width="300" } | ![](tokens/shape-ar-dark.png){ width="300" } |
| Icons | ![](tokens/icons-en-light.png){ width="300" } | ![](tokens/icons-ar-dark.png){ width="300" } |

## Components

Components land one per pull request, in dependency order. Each row shows English light and Arabic dark; every state is verified in all four variants, and the default state in the full 16-cell matrix.

| Component | EN · light | AR · dark |
|---|---|---|
| Price, dated | ![](components/price-dated-en-light.png){ width="300" } | ![](components/price-dated-ar-dark.png){ width="300" } |
| Price, from | ![](components/price-from-en-light.png){ width="300" } | ![](components/price-from-ar-dark.png){ width="300" } |
| Rating | ![](components/rating-en-light.png){ width="300" } | ![](components/rating-ar-dark.png){ width="300" } |

The Arabic dated price at 200 % font and 320 dp, the hardest cell of the matrix: ![](components/price-dated-ar-dark-f200-w320.png){ width="240" }

## Android and iOS

Every case renders on both platforms from the same Kotlin, and both sets of baselines are verified on every pull request (Linux and macOS jobs).

| | Android | iOS |
|---|---|---|
| Price, Arabic dark | ![](platforms/price-dated-ar-dark-android.png){ width="300" } | ![](platforms/price-dated-ar-dark-ios.png){ width="300" } |
| Type, English light | ![](platforms/type-en-light-android.png){ width="300" } | ![](platforms/type-en-light-ios.png){ width="300" } |

## Screens

*pending — Phase 5.* One grid per screen (Explore, Search, Results, Filters, Details, Checkout, Map, Saved, Bookings, Profile) in the four variants, plus each `ScreenState`.

## Recordings

*pending — Phase 5/6.* Per-feature flows, the hero reel, and Android/iOS side by side.
