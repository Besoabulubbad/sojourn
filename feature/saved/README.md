# feature:saved

Implements the design-system card **ScreenSaved** (with its states from **ScreenStates**): the guest's shortlist. It has its own tab because comparing stays over days is how longer bookings are made.

- **Cards:** the saved stays, newest first, as `SojournStayCard`s. A stay keeps the search it was saved from (its dates and guests, stored with the save): with dates still ahead, the card shows that search's all-in total, priced like Results and cached for offline (P-02); without dates, or once they have passed, the monthly "from" price (P-03). Tapping a card opens Details for that search, with the quote the card shows.
- **Remove:** the heart takes the stay off at once; "Removed from Saved" with "Undo" shows above the tab bar for 5 seconds (`SojournSnackbarHost`). No confirmation dialog. "Undo" puts the stay back where it was, with its search.
- **Empty:** what the heart does, and one action: "Explore stays" switches to the Explore tab.
- **States:** saves are on the device, so the tab works offline and has no error page; loading is the moment the database is read.
- **Not yet:** saving signed out and syncing after sign-in (ScreenSaved card) arrives with accounts (Profile). Saves are local until then.

**Navigation keys** (declared in `core:navigation`): `SavedKey`, the Saved tab's root.

**Shape:** `SavedUiState` · `SavedIntent` · `SavedEffect` · `SavedViewModel` · `SavedRoute` · `SavedScreen` · `savedModule` · `EntryProviderScope<NavKey>.savedEntries(navigator)`. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:platform`, `core:model`, `core:common`; never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Tests:** `SavedViewModelTest` (empty and "Explore stays", a dated save's total and its Details search, remove and undo) and `savedSnapshots`: the list in the full matrix, "Undo" showing, empty and loading, on Android and iOS. The domain's `ObserveSaved`, `RemoveSaved` and `RestoreSaved` and the repository's dates and undo have their own tests.
