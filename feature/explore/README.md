# feature:explore

Implements the design-system card **ScreenExplore**, the first screen: one compact search bar, stay-length shortcuts and the guest's own context, so the longer-stay offer is visible before any tap.

| Part | Built from | Goes to |
|---|---|---|
| Greeting (display, wraps rather than clips) | `BasicText` + `SojournTheme.type.display` | — |
| Search bar, prompt form, the only search entry | `SojournSearchBar(query = null)` | `SearchKey()` |
| "How long are you staying?" shortcuts | `SojournChip` × 4 | `SearchKey(args.length = Weekend / Week / Month / Longer)` |
| "Continue your search" or "Your next stay" | `SojournContinueRow` | `ResultsKey(last search)` or `BookingDetailsKey` |
| Cities, "from … a month" | `SojournCityCard` row; `SojournSkeleton` while loading | `ResultsKey(cityId)`; "See all" → `SearchKey(step = Where)` |
| Bottom nav | `SojournTabBar()` (appears once a second tab exists) | — |

**States** (`ExploreUiState.cities` is a `ScreenState`, built with `ScreenState.from`):
- **Loading:** skeleton cards.
- **Content.**
- **Offline with cached cities:** `SojournOfflineBanner` with retry above the screen.
- **Offline with nothing cached / error:** `SojournEmptyState` with retry in the cities section. Search and shortcuts stay usable.
- **Empty.**

The "Continue" row is absent on first run, and an upcoming or active stay replaces the last search.

**Motion:** city cards fade in on first load with the card's 40 ms stagger (at most five); none under reduce motion. The search bar's container transform comes with the search flow.

**Decisions**
- The card asks for a one-line greeting, but at display size it needs two lines on a phone, so it wraps rather than clips (I-04).
- The shortcut labels are the card's ("A few nights", "1 week", …), in their own `explore_length_*` keys. They map to `StayLength`.

**Tests**
- `ExploreViewModelTest`: first run, offline cache and retry, last search, next stay, every navigation.
- `exploreSnapshots`: five states on Android and iOS; content in the full matrix (EN/AR × light/dark × 100/200 % × 320/412 dp).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:model`, `core:common` (the `sojourn.kmp.feature` set). Never `core:data` (tests use the fakes from `core:testing`), `core:database`, `core:network` or another feature (Konsist).
