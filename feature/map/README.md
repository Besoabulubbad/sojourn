# feature:map

Implements the design-system card **ScreenMap** (with its states from **ScreenStates**): the map answers "where, and for how much". Same search, filters and prices as Results, so a pin shows the total its card shows (P-02).

- **Map:** `SojournMap` (MapLibre with OpenFreeMap tiles, styled from the map tokens; ADR 0017). It follows light and dark, labels places in the app's language, and is never mirrored in RTL; the search bar, pill, toggle and card are. The attribution stays visible just above whatever is docked at the bottom.
- **Pins:** `SojournPricePin` at each stay's area: default, seen (its card was raised before), selected. Pins that would overlap merge into a `SojournClusterPin` with a count; tapping a cluster zooms in on its stays. The map opens fitted to every stay found.
- **Card:** selecting a pin raises one compact `SojournStayCard` (250 ms, `ease-emphasized`); swiping it selects the next stay, and the map follows when that pin is out of view. Tapping the card opens Details with the stay's quote.
- **Search this area:** appears after the guest pans or zooms, and only then; searching keeps the stays in view. An area with none says so, with "Show all stays".
- **List** is always visible above the card. It shows Results for the same search in the map's place, so list and map never stack up.
- **States:** "Loading" under the search bar while the search runs; offline, cached pins under the banner; no matches, an empty area and errors each get a note above "List" with one way forward.
- **Opened from Details** ("Show on map" or its area map): `MapKey.stayId` selects that stay and raises its card.

**Navigation keys** (declared in `core:navigation`): `MapKey(args, stayId?)`.

**Shape:** `MapUiState` · `MapIntent` · `MapEffect` · `MapViewModel(key, …)` · `MapRoute` · `MapScreen` · `mapModule` (the key comes in with `parametersOf`) · `EntryProviderScope<NavKey>.mapEntries(navigator)`. The camera is the screen's (`SojournMapState`); the ViewModel keeps what the guest chose. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:platform`, `core:model`, `core:common`; never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Tests:** `MapViewModelTest` (same stays as the list, opening on a stay, seen pins, search this area, navigation) and `mapSnapshots`: every state over the stand-in map, on Android and iOS. The live map was checked on the emulator.
