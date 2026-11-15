# 0006 — MVI / unidirectional data flow for presentation

- **Status:** accepted
- **Date:** 2026-11-15
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** architecture, presentation

## Context and problem statement

Ten screens, each with loading/content/empty/error/offline states, two languages and two themes, must be unit-testable, screenshot-testable per state, and uniform enough that a reviewer learns the pattern once. The engineering standards require `ScreenState<T>` to be exhaustive (I-05). We need one presentation pattern for every screen.

## Decision drivers

- Deterministic, exhaustively testable state.
- One state object per screen that screenshot tests can render directly.
- Identical shape on every screen.
- Works with Navigation 3 scoping and Koin ViewModels on both platforms.

## Options considered

1. **MVI / UDF** — `SojournViewModel<State, Intent, Effect>`; one `StateFlow<State>`, sealed `Intent`s, one-off `Effect`s.
2. **MVVM (plain)** — ViewModel exposing several flows/callbacks per concern.
3. **Circuit** (Slack) — Compose-driven presenters and UI factories, with its own navigation.

## Decision outcome

**Chosen: Option 1, MVI / UDF**, implemented as a small base class in `core:ui`:

```kotlin
abstract class SojournViewModel<S : UiState, I : UiIntent, E : UiEffect>(initial: S) : ViewModel() {
    val state: StateFlow<S>
    val effects: Flow<E>
    fun onIntent(intent: I)
    protected fun setState(reduce: S.() -> S)
    protected fun sendEffect(effect: E)
}
```

Conventions: `XxxUiState` is an immutable data class (data-bearing screens embed `ScreenState<T>`); `XxxIntent` and `XxxEffect` are sealed; `XxxRoute` is the stateful composable (Koin, effects, navigation) and `XxxScreen(state, onIntent)` is stateless and previewable. Effects carry navigation requests, snackbars and haptics — never state.

### Consequences

- Good: tests read "given state and intent, expect state"; every screenshot is one `UiState` value.
- Good: a new engineer (or agent) reads one screen and knows all ten.
- Good: `ScreenState<T>` makes the five states a compile-time obligation.
- Bad: more types per screen than plain MVVM. Mitigated by the base class and a feature template in the guide.
- Bad: naive MVI can over-centralise; large screens may need sub-state. Mitigated: `UiState` may compose smaller immutable sections; intents stay flat.
- Neutral: No third-party MVI framework (Orbit, MVIKotlin) — the base class is ~60 lines and fully ours.

### Confirmation

- Konsist: every class ending in `ViewModel` in `feature:*` extends `SojournViewModel`; each feature has `XxxUiState`, `XxxIntent`, `XxxEffect`, `XxxRoute`, `XxxScreen`.
- detekt: `MutableStateFlow` exposed publicly is forbidden.
- Screenshot tests take `UiState` fixtures from `core:testing`.

## Pros and cons of the options

### Option 1 — MVI / UDF

- Good: determinism, testability, uniformity; aligns with Compose's state-driven model.
- Bad: ceremony; risk of god-states if undisciplined.

### Option 2 — MVVM (plain)

- Good: least ceremony; familiar.
- Bad: drifts between screens; partial states are easy to leave untested; multiple flows complicate screenshot fixtures.

### Option 3 — Circuit

- Good: elegant Compose-native presenters, KMP, testing utilities, overlays.
- Bad: brings its own navigation and factories, overlapping with Navigation 3 (ADR 0005); opinionated framework to explain in a demo.

## Links

- [Android architecture — UI layer and UDF](https://developer.android.com/topic/architecture/ui-layer)
- [Compose Multiplatform — ViewModel](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-viewmodel.html)
- Related: ADR 0004, ADR 0005, ADR 0010
