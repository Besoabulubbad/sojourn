# feature:map

Implements the design-system card **ScreenMap** — the map answers "where, and for how much": price pins, clusters, search this area.

**Navigation keys** (declared in `core:navigation`): `MapKey(query)`

**Shape:** `MapUiState` · `MapIntent` · `MapEffect` · `MapViewModel : SojournViewModel` · `MapRoute` (stateful) · `MapScreen` (stateless) · `mapModule` · `EntryProviderBuilder<NavKey>.mapEntries(navigator)`. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:platform`, `core:model`, `core:common` — never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Status:** skeleton (Phase 1); built in Phase 5. Definition of Done applies before merge.
