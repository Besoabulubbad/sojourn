# feature:filters

Implements the design-system card **ScreenFilters**: a modal sheet over Results whose price filter uses the same basis as the cards.

- **Price:** `SojournPriceRange` labelled with the cards' basis ("Total price for 15 nights" with dates, "Price per month" without, P-04). The histogram shows where the stays the other filters keep sit; the bounds are whole steps around them (£10 a night, £100 a month or a stay). The full bounds store no range.
- **Rooms:** Bedrooms is exact (Any, Studio, 1, 2, 3+; never "0 bedrooms", N-01). Beds and Bathrooms are minimums; 1 means any.
- **Amenities:** all twelve, each with its glossary name and icon (`Amenity.label`, `Amenity.icon`).
- **Booking:** Free cancellation (the same flag as the Results chip) and Instant book. Not on the card; added so every filter the badge counts can be seen and changed here.
- **Apply:** "Show 14 stays" counts the draft with the same search Results runs (`PreviewFilters`), 300 ms after the last change, crossfading `fast`. At zero it reads "No stays match" and is disabled. "Clear all" resets the sheet (not the sort) and keeps it open. Only the button applies; any other way out discards the draft.

**Navigation keys** (declared in `core:navigation`): `FiltersKey(args)`, the search Results shows. The entry is an overlay (`SojournScenes.overlay()`), so Results stays visible under the scrim (ADR 0005).

**Shape:** `FiltersUiState` · `FiltersIntent` · `FiltersEffect` · `ApplyButton` · `FiltersViewModel(key, …)` · `FiltersRoute` (stateful) · `FiltersScreen` (stateless) · `filtersModule` (the key comes in with `parametersOf`) · `EntryProviderScope<NavKey>.filtersEntries(navigator)`. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:platform`, `core:model`, `core:common`; never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Tests:** `FiltersViewModelTest` (opening count, debounce and apply, no match, price basis, steppers and clear all, dismiss) and `filtersSnapshots`: the card's state in the full matrix, undated, no match and counting, on Android and iOS.
