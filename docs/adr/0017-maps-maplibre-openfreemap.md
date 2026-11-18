# 0017 — Maps: MapLibre Compose with OpenFreeMap tiles, our own pins

- **Status:** accepted
- **Date:** 2026-11-18
- **Deciders:** project owner, with Claude Code
- **Tags:** design-system, map, dependencies, testing

## Context and problem statement

The ScreenMap card puts price pins on a real map. The map's colours must come from the map tokens (`mapLand`, `mapWater`, `mapPark`, `mapRoad`) and follow the app theme, so the map never contrasts with the app. Its attribution must stay visible, and the map itself is not mirrored in RTL. The ScreenDetails card's "Where you'll stay" shows a small map of the stay's area, which waited for this decision (#25). Phase 5 builds the Map screen now, so the library has to be chosen now.

## Decision drivers

- One map for Android and iOS from shared Compose code (ADR 0001); no per-platform map screens.
- Map colours from our tokens in light and dark, not a vendor's preset styles.
- No API key or account: the repository is public and must build and run from a clone.
- Pins and clusters must be the design system's `SojournPricePin` / `SojournClusterPin`: same look, motion (1.12 on select) and screen-reader labels as everywhere else.
- Screenshot tests (Robolectric and iOS) must still cover every Map state, though a live map cannot render there.

## Options considered

1. **MapLibre Compose with OpenFreeMap tiles**: one Compose Multiplatform map; vector tiles from OpenFreeMap, styled by us.
2. **Google Maps on Android, Apple MapKit on iOS**: `maps-compose` and MapKit through `UIKitView`.
3. **Our own drawn map**: simplified shapes bundled with the app, drawn on a `Canvas`.

## Decision outcome

**Chosen: option 1, MapLibre Compose (`org.maplibre.compose:maplibre-compose`) with OpenFreeMap tiles.** It is the only option that gives one shared map *and* a style we write, so the map's colours are the map tokens in both themes. OpenFreeMap serves OpenStreetMap vector tiles with no key. The library is wrapped by one design-system component, `SojournMap`, so no feature sees a MapLibre type and the library can be replaced behind it.

- **Style:** built in code from `SojournTheme.colors` (land, water, parks, roads, labels in the app's language) over OpenFreeMap's OpenMapTiles source. It is rebuilt when the theme or language changes.
- **Pins:** our own composables placed at geographic positions over the map. Clustering is ours (`sojournPinGroups`, pure and unit-tested): pins whose labels would overlap at the current zoom merge into a count, and tapping a cluster zooms to it.
- **Attribution:** "© OpenStreetMap contributors · OpenFreeMap", always visible, placed above whatever the screen docks at the bottom.
- **Tests:** in previews and screenshots, `SojournMap` draws a stand-in: the `mapLand` colour, with the overlays placed by the same Web Mercator projection the map uses. Every state is covered on both platforms; the live map is checked on the emulator and simulator.

### Consequences

- Good: one implementation; the map matches the app in light, dark, English and Arabic.
- Good: no secrets, accounts or billing; a clone builds and shows real maps.
- Good: pins, clusters and the card are the design system's, with their animation and accessibility.
- Bad: a native dependency (MapLibre Native) adds to app size. Accepted for a real map.
- Bad: OpenFreeMap is a free public service with no SLA. The style's source URL is one constant; a self-hosted or commercial OpenMapTiles-compatible source replaces it.
- Bad: Compose pins scale to tens, not thousands; the catalogue has eight to a city. If that changes, the pins move to a MapLibre symbol layer behind the same `SojournMap` API.
- Neutral: iOS needs MapLibre's system libraries in the app target's Other Linker Flags (MapLibre Compose platform setup); iOS 15.5 and Apple-silicon simulators only.

### Confirmation

- Material and maps stay inside `core:designsystem`: `maplibre-compose` is a dependency of that module only; `buildHealth` fails if a feature declares it.
- `SojournMapStyleTest` checks the style is built from the theme's map tokens; `SojournPinGroupsTest` checks clustering.
- Map screenshots (Android and iOS) cover each state over the stand-in.

## Pros and cons of the options

### MapLibre Compose with OpenFreeMap

- Good: Compose Multiplatform API (Android, iOS); camera, gestures, overlays at geographic positions.
- Good: style is ours, so tokens and both themes apply; labels in the app's language.
- Bad: young library (0.x); wrapped behind `SojournMap` so upgrades touch one module.

### Google Maps and Apple MapKit

- Good: familiar native maps.
- Bad: two implementations; a Google API key in CI secrets for a public repo.
- Bad: MapKit offers light and dark but not our colours, which breaks the card's rule.

### Our own drawn map

- Good: no dependency or network.
- Bad: not a real map: no streets beyond what we bundle, no real zoom.

## Links

- MapLibre Compose docs: getting started, styling, camera, overlays, platform setup (Context7 `/maplibre/maplibre-compose`, release 0.20.0)
- OpenFreeMap (tiles, fonts, attribution requirements)
- ScreenMap and PricePin cards; ADR 0001 (shared UI), ADR 0008 (tokens), ADR 0010 (testing)
