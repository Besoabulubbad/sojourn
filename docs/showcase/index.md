# Showcase

The visual record of the app as it is built — every component and screen in English/Arabic × light/dark, recordings of each flow, and the hero reel. Everything here is produced by the pipeline from the same screenshot tests that gate pull requests, so what you see is what CI verified.

!!! info "Filling in as the phases land"
    Assets appear in the order of the [plan](../plan.md): wordmark and theme swatches (Phase 2), the component contact sheet (Phase 3), per-screen grids and recordings (Phase 5), the hero reel and Android/iOS side-by-side (Phase 6). Until an asset exists its row below says *pending*.

## How the assets are produced

| Asset | Source | Command |
|---|---|---|
| Component contact sheet | Roborazzi baselines in `core/designsystem` | `./gradlew exportShowcase` |
| Screen grids (412 dp, 4 variants) | Roborazzi baselines in each `feature:*` | `./gradlew exportShowcase` |
| Flow recordings (GIF / WebP) | emulator screen recording while a Maestro flow drives the app, converted with ffmpeg | `scripts/record-flow.sh <flow>` |
| Hero reel | the booking golden path with a mid-flow theme and language switch | `scripts/record-flow.sh hero` |
| Social preview (1280 × 640) | composed from the hero frames and the wordmark | `scripts/social-preview.sh` |

Exports are committed under `docs/showcase/` so the README and this page work offline and in forks.

## Brand

| | |
|---|---|
| Wordmark and app icon | *pending — Phase 2* |
| Theme swatches, light and dark | *pending — Phase 2* |

## Components

*pending — Phase 3.* One row per component: default, states, EN/AR, light/dark.

## Screens

*pending — Phase 5.* One grid per screen (Explore, Search, Results, Filters, Details, Checkout, Map, Saved, Bookings, Profile) in the four variants, plus each `ScreenState`.

## Recordings

*pending — Phase 5/6.* Per-feature flows, the hero reel, and Android/iOS side by side.
