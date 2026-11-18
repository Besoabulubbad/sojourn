# feature:search

Implements the design-system card **ScreenSearchFlow**: a full-screen, three-step flow (Where, When, Who) in one back-stack entry, so the steps slide inside the screen and the progress bar fills (motion.md).

- **Where** lists the cities offline-first (`GetCities`) as `SojournCityRow`s; tapping one moves on to When. There is no search field while the list stays under 12 cities.
- **When** has two tabs. *Dates* is a `SojournDateRangePicker` with past days struck through. *Flexible* is one of the four stay lengths Explore's shortcuts use, plus a "Starting in" month (this month and the next three). The footer (`SojournActionBar`) shows the night count and offers "Any time", a deliberate choice whose effect Who states.
- **Who** has `SojournStepper`s for adults and children; "Search" records the search (`RecordSearch`, for Explore's "Continue" row) and replaces the flow with the results.
- System back and the top bar's button go one step back (`StepBackHandler`); on Where they close the flow.

**Navigation keys** (declared in `core:navigation`): `SearchKey(step, args)`. `step` is where the flow opens; `args` carries what is already chosen (Explore's length shortcuts open it with Flexible pre-set). `SearchArgs.toQuery()` / `SearchQuery.toArgs()` convert.

**Shape:** `SearchUiState` · `SearchIntent` · `SearchEffect` · `SearchViewModel(key, …)` · `SearchRoute` (stateful) · `SearchScreen` (stateless) · `searchModule` (the key comes in with `parametersOf`) · `EntryProviderScope<NavKey>.searchEntries(navigator)`. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:platform`, `core:model`, `core:common`; never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Tests:** `SearchViewModelTest` (steps, back, any time, flexible, restoring a key) and `searchSnapshots`: each step and every Where state, on Android and iOS.
