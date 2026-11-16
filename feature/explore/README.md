# feature:explore

Implements the design-system card **ScreenExplore** — the first screen: one compact search bar, stay-length shortcuts and the guest's own context, so the longer-stay offer is visible before any tap.

**Navigation keys** (declared in `core:navigation`): `ExploreKey`

**Shape:** `ExploreUiState` · `ExploreIntent` · `ExploreEffect` · `ExploreViewModel : SojournViewModel` · `ExploreRoute` (stateful) · `ExploreScreen` (stateless) · `exploreModule` · `EntryProviderBuilder<NavKey>.exploreEntries(navigator)`. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:platform`, `core:model`, `core:common` — never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Status:** skeleton (Phase 1); built in Phase 5. Definition of Done applies before merge.
