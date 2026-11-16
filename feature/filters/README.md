# feature:filters

Implements the design-system card **ScreenFilters** — a modal sheet whose price filter uses the same basis as the cards.

**Navigation keys** (declared in `core:navigation`): `FiltersKey`

**Shape:** `FiltersUiState` · `FiltersIntent` · `FiltersEffect` · `FiltersViewModel : SojournViewModel` · `FiltersRoute` (stateful) · `FiltersScreen` (stateless) · `filtersModule` · `EntryProviderBuilder<NavKey>.filtersEntries(navigator)`. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:platform`, `core:model`, `core:common` — never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Status:** skeleton (Phase 1); built in Phase 5. Definition of Done applies before merge.
