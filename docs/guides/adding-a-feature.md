# Adding a feature (screen)

A feature is one `feature:<name>` module implementing one design-system screen card. Follow the steps in order; each maps to something CI checks.

## 0. Read first

- The screen's card: `project/api/components/Screen<Name>.md` in the design system, and its guideline README.
- [Architecture overview](../architecture/overview.md) — presentation, navigation, DI sections.
- [Design system → code](../design-system/mapping.md) — which `Sojourn*` components the screen is made of. If one is missing, [add the component first](adding-a-component.md).

## 1. Create the module

```
feature/<name>/
  build.gradle.kts            plugins { id("sojourn.kmp.feature") }  + feature-specific deps only
  README.md                   purpose, the card it implements, states, open questions
  src/commonMain/kotlin/com/abulubad/sojourn/feature/<name>/
    <Name>UiState.kt          data class <Name>UiState(val content: ScreenState<...>, ...) : UiState
    <Name>Intent.kt           sealed interface <Name>Intent : UiIntent
    <Name>Effect.kt           sealed interface <Name>Effect : UiEffect
    <Name>ViewModel.kt        class <Name>ViewModel(...) : SojournViewModel<...>
    <Name>Route.kt            @Composable fun <Name>Route(navigator: Navigator)
    <Name>Screen.kt           @Composable fun <Name>Screen(state, onIntent, modifier)
    <Name>Navigation.kt       fun EntryProviderScope<NavKey>.<name>Entries(navigator: Navigator)
    <Name>Module.kt           val <name>Module = module { viewModelOf(::<Name>ViewModel) }
  src/commonMain/composeResources/   only if the feature has feature-only drawables
  src/commonTest/kotlin/...          <Name>ViewModelTest.kt
  src/androidHostTest/kotlin/...     <Name>ScreenScreenshotTest.kt
```

Add `include(":feature:<name>")` to `settings.gradle.kts`.

## 2. Navigation key

In `core:navigation`, add the key to the `SojournNavKey` hierarchy and register it in `sojournNavSerializersModule`:

```kotlin
@Serializable data class StayDetailsKey(val stayId: String, val quoteId: String? = null) : SojournNavKey
```

Keys carry ids, never domain objects. If a price is shown, the **quote id travels with the key** (standard P-02). The round-trip test in `core:navigation` must pass.

## 3. State, intents, effects

- `UiState` is immutable; data-bearing screens embed `ScreenState<T>` and `when` over all five cases.
- Intents are what the user did (`Intent.SaveToggled(stayId)`), not what the ViewModel should do.
- Effects are one-off (`Effect.NavigateTo(key)`, `Effect.ShowSnackbar(message)`, `Effect.Haptic(Light)`).

## 4. ViewModel

Constructor-inject use cases from `core:domain` (never repositories). Collect flows in `viewModelScope`; map errors to `ScreenState.Error`/`Offline`. No platform imports.

## 5. UI

- `XxxScreen` uses only `Sojourn*` components and tokens. Material imports fail Konsist; raw `dp`/`sp`/`Color`/ms fail detekt.
- Layout with `SojournScaffold`; never add inset modifiers.
- Strings via `stringResource`/`pluralStringResource` from `core:localization`; new keys go in `values/strings.xml` **and** `values-ar/strings.xml` (plurals with all six Arabic forms). Follow the glossary.
- Prices only through `SojournPrice(PriceDisplay)`.
- Start/end, never left/right. Directional motion from `SojournTransitions`.
- Add `@Preview`s for each `ScreenState` using `SojournVariants` (EN/AR × light/dark).

## 6. Wire it

- `XxxRoute`: `val vm = koinViewModel<XxxViewModel>()`, collect `state`, `collectEffects { … navigator.goTo(…) … }`, call `XxxScreen`.
- `XxxNavigation.kt`: `entry<XxxKey> { key -> XxxRoute(navigator, key) }` (sheet or dialog metadata if the card says so).
- `app:shared`: add `<name>Module` to `sojournModules`, `<name>Entries(navigator)` to `sojournEntries`, the tab (for a tab root) to `availableTabs`, and the module dependency; a tab root places `SojournTabBar()` in its scaffold's `bottomBar`.

## 7. Tests

- `XxxViewModelTest`: given state and intent → expected state; uses fakes from `core:testing`, Turbine, `runTest`.
- `XxxScreenScreenshotTest`: one test per `ScreenState` fixture, parameterised over the matrix (2 languages × 2 themes × font 100/200 × width 320/412). Record once (`recordRoborazziDebug`), review the images, commit the baselines.
- If the screen has a signature interaction (drag, swipe, stepper), add a Compose UI test in `androidDeviceTest`.
- Extend `.maestro/` if the screen is on the booking golden path.

## 8. Documentation

- Module `README.md` (what, card, states, decisions).
- Row in [design-system/mapping.md](../design-system/mapping.md) "Screens → features" if new.
- [architecture/modules.md](../architecture/modules.md) feature table.
- KDoc on `XxxRoute` and `XxxScreen`.

## 9. Definition of Done (PR checklist)

Mirrors the design system's "Definition of done for a screen". Paste into the PR:

- [ ] Built only from design-system components and tokens; detekt and Konsist clean
- [ ] All strings in resources for `en` and `ar`; plural forms complete (`verifyPlurals` passes)
- [ ] Screenshot matrix recorded for every `ScreenState`, reviewed, baselines committed
- [ ] Verified in the four device/app language pairs (EN/EN, EN/AR, AR/EN, AR/AR)
- [ ] Prices pass through `SojournPrice`; totals match across surfaces for one fixture
- [ ] Loading, empty, error and offline implemented and screenshotted
- [ ] TalkBack/VoiceOver reach and name every control, sensible order, both languages
- [ ] No text under system bars; nothing covered by floating or docked controls (320 dp, 200% checked)
- [ ] Motion uses `SojournMotion`; reduce-motion path verified
- [ ] System back / swipe back does the expected thing on both platforms
- [ ] Module README, mapping row, modules table and KDoc updated

## Commit

`feat(<name>): add <screen> screen` — one PR per feature; keep it reviewable (split components out first if large).
