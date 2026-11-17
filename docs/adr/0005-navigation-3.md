# 0005 — Navigation 3 (JetBrains multiplatform artifacts)

- **Status:** accepted
- **Date:** 2026-11-15
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** architecture, navigation

## Context and problem statement

The app has four tabs with independent histories, a full-screen search flow, bottom-sheet destinations (Filters, price breakdown), a dialog or two, container-transform transitions that mirror in RTL, and deep links in the future. The navigation library decides how the back stack is modelled, how ViewModels are scoped, how sheets/dialogs are expressed and how this works on iOS.

## Decision drivers

- Back stack as inspectable state (multiple stacks, process-death restoration).
- Native support for sheet/dialog/adaptive "scenes" rather than one-destination-at-a-time.
- Type-safe keys with `kotlinx.serialization`.
- Stable multiplatform artifacts for Compose Multiplatform 1.12.
- Direction of the ecosystem (what Google and JetBrains are investing in).

## Options considered

1. **Navigation 3** via JetBrains' multiplatform `navigation3-ui`.
2. **Navigation Compose (Nav2)** via JetBrains' `navigation-compose` 2.10.
3. **Decompose** (third-party KMP, component-based).

## Decision outcome

**Chosen: Option 1, Navigation 3.** Verified on 2026-11-15 against Maven Central and the official JetBrains page:

| Artifact | Version | Note |
|---|---|---|
| `org.jetbrains.androidx.navigation3:navigation3-ui` | **1.2.0** (stable) | publishes android, iOS (arm64, simulator, x64), desktop, wasm, js |
| `androidx.navigation3:navigation3-runtime` | 1.2.0 (transitive; 1.2.1 latest) | Google publishes the runtime as KMP itself |
| `org.jetbrains.androidx.navigationevent:navigationevent-compose` | 1.1.0 (transitive) | predictive back / swipe back |
| `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3` | **2.11.0** | NavEntry-scoped ViewModels; matches our lifecycle 2.11.0 |
| `io.insert-koin:koin-compose-navigation3` | 4.2.2 | Koin integration |

The 1.10-era "alpha on non-Android targets" caveat no longer applies: 1.2.0 is a stable release line with iOS variants.

Design in this codebase:

- `core:navigation` declares `@Serializable sealed interface SojournNavKey : NavKey` and one key per destination; it also builds the `SerializersModule` with explicit `polymorphic(NavKey::class)` registration — **required on iOS**, where the reflection-based `rememberNavBackStack(vararg)` overload is unavailable (per the JetBrains docs).
- Each feature contributes `EntryProviderBuilder<NavKey>.<name>Entries(navigator)`; `app:shared` composes them and owns the `NavDisplay`.
- Four tab back stacks persisted with `rememberNavBackStack(savedStateConfiguration, …)`; a `Navigator` interface (`goTo`, `back`, `switchTab`, `popTo`) is what features call.
- Scenes: bottom-sheet strategy for `SojournSheet` destinations, dialog scene for dialogs; transitions from `SojournMotion`, mirrored by `LayoutDirection`.

### Consequences

- Good: the back stack is data — testable, restorable, and multiple stacks are trivial.
- Good: scenes express sheets/dialogs/adaptive layouts in the navigation model, not as ad-hoc UI state.
- Good: aligned with Google's and JetBrains' direction; predictive/swipe back via `navigationevent`.
- Bad: newer API, fewer community examples than Nav2. Mitigated by the official recipes repositories (Android and the CMP port).
- Bad: the serializer module must be kept complete by hand for iOS. Mitigated by a test in `core:navigation` that instantiates every key through the module.
- Neutral: Deep links are not built in as in Nav2; a small parser maps URIs to keys when needed (recipe exists).

### Confirmation

- `core:navigation` unit test: every `SojournNavKey` subtype round-trips through `sojournNavSerializersModule`.
- `app:shared` test: process-death restoration of all four stacks (Robolectric).
- Konsist: `NavKey` implementations exist only in `core:navigation`.

## Pros and cons of the options

### Option 1 — Navigation 3

- Good: state-owned back stack, scenes, type-safe keys, multiplatform stable, ecosystem direction.
- Bad: younger; iOS needs explicit serializers; deep links are DIY.

### Option 2 — Navigation Compose (Nav2)

- Good: mature, hugely documented, deep links built in, type-safe routes since 2.8.
- Bad: back stack hidden in `NavController`; sheets/dialogs bolted on; Google's investment has moved to Nav3.

### Option 3 — Decompose

- Good: battle-tested KMP, lifecycle-aware components, great multi-stack and iOS support.
- Bad: another vendor abstraction to learn and explain; components model competes with ViewModel/MVI; less aligned with Compose-native direction.

**2026-11-17 (Phase 5 start).** Notes from wiring the host, decision unchanged:
- The scope type in Navigation 3 1.x is `EntryProviderScope`, not `EntryProviderBuilder`.
- Each tab's stack is decorated on its own, following the official multiple-back-stacks recipe.
- ViewModels are scoped per entry by `rememberViewModelStoreNavEntryDecorator`; `koinViewModel()` uses that entry's store, so `koin-compose-navigation3` is not needed.
- The bottom nav is placed by each tab root (`SojournTabBar()` in `core:ui`), so a single scaffold owns the bottom inset.

## Links

- [Navigation 3 in Compose Multiplatform — JetBrains docs](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html)
- [Jetpack Navigation 3 — Android docs](https://developer.android.com/guide/navigation/navigation-3)
- [Navigation 3 is stable (Nov 2025)](https://android-developers.googleblog.com/2025/11/jetpack-navigation-3-is-stable.html)
- Recipes: [android/nav3-recipes](https://github.com/android/nav3-recipes), [terrakok/nav3-recipes (CMP)](https://github.com/terrakok/nav3-recipes)
- Related: ADR 0004, ADR 0006
