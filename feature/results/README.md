# feature:results

Implements the design-system card **ScreenResults, ScreenStates** — the list of stays, with loading, empty, error and offline states.

**Navigation keys** (declared in `core:navigation`): `ResultsKey(query)`

**Shape:** `ResultsUiState` · `ResultsIntent` · `ResultsEffect` · `ResultsViewModel : SojournViewModel` · `ResultsRoute` (stateful) · `ResultsScreen` (stateless) · `resultsModule` · `EntryProviderBuilder<NavKey>.resultsEntries(navigator)`. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:platform`, `core:model`, `core:common` — never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Status:** skeleton (Phase 1); built in Phase 5. Definition of Done applies before merge.
