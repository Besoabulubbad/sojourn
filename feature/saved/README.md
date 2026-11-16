# feature:saved

Implements the design-system card **ScreenSaved** — the guest's shortlist.

**Navigation keys** (declared in `core:navigation`): `SavedKey`

**Shape:** `SavedUiState` · `SavedIntent` · `SavedEffect` · `SavedViewModel : SojournViewModel` · `SavedRoute` (stateful) · `SavedScreen` (stateless) · `savedModule` · `EntryProviderBuilder<NavKey>.savedEntries(navigator)`. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:platform`, `core:model`, `core:common` — never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Status:** skeleton (Phase 1); built in Phase 5. Definition of Done applies before merge.
