# feature:results

Implements the design-system card **ScreenResults** (with its states from **ScreenStates**): the stays for a search, each with the price every later surface shows.

- **Header:** the search summary (`SojournSearchBar` with back; tap it to change the search) beside the filter button with the active-filter count, which opens the filter sheet (`FiltersKey`) over the list. Then the quick-filter chips: Studio (the sheet's Bedrooms "Studio"), Workspace, Washer, Free cancellation. Chips and sort write the persisted `Filters`, so the filter sheet and its badge agree.
- **List:** the count ("3 stays in London") and Sort (Recommended, Price low to high, Rating, Distance to centre), then `SojournStayCard`s. With dates each card shows the all-in total and carries its quote id to Details (P-02); without, "from … a month" and what it excludes (P-03).
- **Map:** the floating `SojournViewToggle` opens `MapKey` with the same search.
- **Refresh:** pull to refresh (`SojournPullToRefresh`) runs the search again past the refresh policy; the list stays on screen.
- **States:** skeleton cards while loading; offline, the banner above cached stays, and a dated price older than an hour says its age; empty offers one way forward that changes the query (clear the filters, or change the search); an error replaces only the list.
- **Not yet:** paging (see ADR 0007; the catalogue answers in one call) and the card-to-Details container transform (with Details).

**Navigation keys** (declared in `core:navigation`): `ResultsKey(args)`. A search finished from here replaces these results (`Navigator.goToReplacing`), so back never walks through old lists.

**Shape:** `ResultsUiState` · `ResultsIntent` · `ResultsEffect` · `QuickFilter` · `ResultsViewModel(key, …)` · `ResultsRoute` (stateful) · `ResultsScreen` (stateless) · `resultsModule` (the key comes in with `parametersOf`) · `EntryProviderScope<NavKey>.resultsEntries(navigator)`. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:platform`, `core:model`, `core:common`; never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Tests:** `ResultsViewModelTest` (search, chips and sort, empty, offline price age, navigation, save and refresh) and `resultsSnapshots`: every state, on Android and iOS.
