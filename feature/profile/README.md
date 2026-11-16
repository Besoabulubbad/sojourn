# feature:profile

Implements the design-system card **ScreenProfile** — account, rewards and settings: language, theme.

**Navigation keys** (declared in `core:navigation`): `ProfileKey`, `LanguageKey`, `AppearanceKey`

**Shape:** `ProfileUiState` · `ProfileIntent` · `ProfileEffect` · `ProfileViewModel : SojournViewModel` · `ProfileRoute` (stateful) · `ProfileScreen` (stateless) · `profileModule` · `EntryProviderBuilder<NavKey>.profileEntries(navigator)`. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:platform`, `core:model`, `core:common` — never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Status:** skeleton (Phase 1); built in Phase 5. Definition of Done applies before merge.
