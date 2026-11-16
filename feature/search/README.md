# feature:search

Implements the design-system card **ScreenSearchFlow** — a full-screen, three-step flow: Where, When, Who.

**Navigation keys** (declared in `core:navigation`): `SearchKey(step)`

**Shape:** `SearchUiState` · `SearchIntent` · `SearchEffect` · `SearchViewModel : SojournViewModel` · `SearchRoute` (stateful) · `SearchScreen` (stateless) · `searchModule` · `EntryProviderBuilder<NavKey>.searchEntries(navigator)`. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:platform`, `core:model`, `core:common` — never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Status:** skeleton (Phase 1); built in Phase 5. Definition of Done applies before merge.
