# feature:details

Implements the design-system card **ScreenDetails** (with its states from **ScreenStates**): the page that earns the booking. Price always visible, location early, policies with real numbers.

- **Top:** the gallery under a clear top bar with floating back, share and save; once the title scrolls under it the bar turns solid and shows the title (`SojournTopBar` over a photo). A photo opens the full-screen gallery (`GalleryKey`, `SojournPhotoViewer`).
- **Sections, in the card's order:** area and title (the host's, through `HostText`), rating, key facts, what the price includes, where you'll stay (the stay's area on a small map, area, walk to public transport, "Show on map"; the map and the button open the full map with this stay selected), amenities ranked for longer stays, about this stay (in the host's language, "Written in French"; "Translate to Arabic" swaps to the catalogue's translation and back; long text stops at a sentence end with "Read more"), good to know (`SojournPolicyRow`s), and reviews when the rating is shown.
- **Booking bar:** docked, never disabled. With dates: the quote's total (the one the card showed, P-02) and "Reserve", which opens checkout with that quote. Without: the "from" price on the search's basis and "Choose dates", which opens the picker sheet; dates the stay cannot take (minimum stay, capacity, booked nights, the past) are refused in the stay's own numbers before the network is asked. "Price details" opens the breakdown at half height.
- **States:** the page's shape while loading; offline, the cached page under the banner; a stay the catalogue dropped says so with a way back; any other error offers "Try again".

**Navigation keys** (declared in `core:navigation`): `StayDetailsKey(stayId, quoteId?, args)` (the search it came from: its guests price a new quote), `GalleryKey(stayId, index)`.

**Shape:** `DetailsUiState` · `DetailsSheet` · `DetailsIntent` · `DetailsEffect` · `DetailsViewModel(key, …)` · `DetailsRoute` · `DetailsScreen`; `GalleryUiState` · `GalleryViewModel(key, …)` · `GalleryRoute` · `GalleryScreen` · `detailsModule` (keys come in with `parametersOf`) · `EntryProviderScope<NavKey>.detailsEntries(navigator)`. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:platform`, `core:model`, `core:common`; never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Tests:** `DetailsViewModelTest` (the card's quote and Reserve, "from" price and the picker, refused dates, priced dates, offline pricing, a removed stay, translation, navigation and the heart), `SentenceExcerptTest`, and `detailsSnapshots`: the card's three frames and every state, on Android and iOS.
