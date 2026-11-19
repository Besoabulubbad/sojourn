# feature:profile

Implements the design-system card **ScreenProfile** (with its states from **ScreenStates**): the guest, rewards and settings. The last Phase 5 screen.

- **Profile tab:** a guest header, with no sign-in ("Your bookings and saved stays are kept on this device"). Below it, one panel of rows, each showing its current value:
  - **Rewards** first, with the balance (the card's rule: Rewards is a row, not a tab).
  - **Language**, named in its own script ("English", "العربية"; no flags anywhere).
  - **Appearance** ("System", "Light", "Dark").
  - **Help**.
  - No sign-out (there is no account) and no currency (prices stay in each city's own currency; converting needs a rates service).
- **Rewards** (`RewardsKey`): the balance, how points are earned, that spending them comes later, and the completed stays that earned them. The rule is `RewardsRules` in `core:domain`: a point per whole pound or euro paid, or per 100 DA, once a stay is completed; never for upcoming or cancelled stays. Before the first completed stay it says so and offers "Explore stays".
- **Language** (`LanguageKey`): choosing applies at once, the whole UI recomposing in that language and direction, and returns to Profile (the card: "lands back on this screen"). On Android the per-app language also recreates the activity; the tab and back stacks are held above the theme (`rememberSojournNavState`), so they survive it.
- **Appearance** (`AppearanceKey`): System (the default, following the device), Light, Dark. It applies at once and stays on the page. The choice is kept in `PreferencesRepository`; `App` gives the theme the result, and each platform's system bars follow it (Android `enableEdgeToEdge` styles, iOS the window's interface style).
- **Help** (`HelpKey`): five short answers (the price, cancelling, the deposit, the key code, offline), each question a heading. Static text, so it has no ViewModel.
- **States:** everything here is on the device, so it all works offline.

**Navigation keys** (declared in `core:navigation`): `ProfileKey` (the tab's root), `RewardsKey`, `LanguageKey`, `AppearanceKey`, `HelpKey`.

**Shape:** `ProfileUiState` · `ProfileIntent` · `ProfileEffect` · `ProfileViewModel` · `ProfileRoute` · `ProfileScreen`. The same shape for `Language…`, `Appearance…` and `Rewards…`. `HelpRoute` · `HelpScreen`. Also `profileModule` and `EntryProviderScope<NavKey>.profileEntries(navigator)`. See [adding a feature](../../docs/guides/adding-a-feature.md).

**May depend on:** `core:ui`, `core:designsystem`, `core:localization`, `core:navigation`, `core:domain`, `core:model`, `core:common`; never `core:data`, `core:database`, `core:network` or another feature (Konsist).

**Tests:** `ProfileViewModelTest`:
- The balance, language and appearance, and each row's page.
- An upcoming stay earns nothing yet.
- A language choice applies and returns.
- An appearance choice applies and stays.
- Rewards empty, then listed.

`profileSnapshots` render on Android and iOS: Profile and Rewards in the full matrix; Language, Appearance, Rewards before any points, and Help in the four variants. `RewardsRules` and `ObserveRewards` are tested in `core:domain`, and the appearance setting in `core:data`.
