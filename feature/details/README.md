# feature:details

Implements the design-system card **ScreenDetails** — the page that earns the booking: gallery, facts, policies, location, booking bar.

**Navigation keys** (declared in `core:navigation`): `StayDetailsKey(stayId, quoteId?)`, `GalleryKey(stayId, index)`

**Shape:** `DetailsUiState` · `DetailsIntent` · `DetailsEffect` · `DetailsViewModel : SojournViewModel` · `DetailsRoute` (stateful) · `DetailsScreen` (stateless) · `detailsModule` · `EntryProviderBuilder<NavKey>.detailsEntries(navigator)`. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:platform`, `core:model`, `core:common` — never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Status:** skeleton (Phase 1); built in Phase 5. Definition of Done applies before merge.
