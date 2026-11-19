# core:navigation

Navigation 3 keys and the navigation state (ADR 0005).

| Thing | Role |
|---|---|
| `SojournNavKey` | Sealed `@Serializable` hierarchy of every destination: `ExploreKey`, `SearchKey(step, args)`, `ResultsKey`, `MapKey`, `FiltersKey`, `StayDetailsKey(stayId, quoteId?)`, `GalleryKey`, `CheckoutKey(quoteId)`, `BookingConfirmedKey`, `SavedKey`, `BookingsKey`, `BookingDetailsKey`, `ProfileKey`, `LanguageKey`, `AppearanceKey`, `RewardsKey`, `HelpKey`. Keys carry ids and primitives only; a key that shows a price carries the quote id (P-02). The only `NavKey`s in the codebase (Konsist). |
| `SearchArgs` | Where / When / Who flattened to primitives (ISO dates) so the back stack survives process death. |
| `Tab` | The four bottom-nav destinations and their root keys. |
| `Navigator` | What features call: `goTo`, `back`, `switchTab`, `popTo`. |
| `TabbedNavigator` | The implementation: one `NavBackStack` per tab plus the selected tab, as observable state. `app:shared` creates the stacks with `rememberNavBackStack(sojournSavedStateConfiguration, tab.root)`. Back pops the tab, then returns to Explore, then lets the platform handle it. |
| `sojournNavSerializersModule`, `sojournSavedStateConfiguration` | Explicit polymorphic registration of every key, required on iOS. |

Scene strategies (sheet, dialog) and the transition specs built from `SojournMotion` land with the first screens in Phase 5, together with the `navigation3-ui` dependency.

## Tests

`NavigationTest`: every key round-trips through the serializers module, the round-trip covers every sealed subtype (read from the serializer descriptor, so it works on iOS too), tabs keep separate histories, back and re-select behave as above.

**May depend on:** Navigation 3 runtime, savedstate, kotlinx-serialization (all `api`).
