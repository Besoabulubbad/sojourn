# core:ui

The presentation building blocks every screen uses (ADR 0006).

| Thing | Role |
|---|---|
| `ScreenState<T>` | Loading / Content / Empty / Error / Offline (with cached data). Sealed, so `when` is exhaustive (I-05). `ScreenState.fromError()` maps `AppError.Offline` to Offline and everything else to Error. |
| `SojournViewModel<S, I, E>` + `UiState` / `UiIntent` / `UiEffect` | The MVI base: one `state: StateFlow<S>`, `onIntent(I)`, one-off `effects`. Subclasses implement `handle(intent)` with `setState { }` and `sendEffect()`. |
| `CollectEffects(effects) { }` | Collects effects in an `XxxRoute` for as long as it is composed. |
| `SojournScaffold` | Frame and inset owner (I-01): measures the docked bars and hands content the padding it needs (I-03). Screens never read insets. |

## Tests

- `SojournViewModelTest`: intents reduce state, effects arrive once, error-to-state mapping.
- `host_text` screenshot case: an English and an Arabic host title keep their own direction in all four variants. `HostText` itself now lives in `core:designsystem` (the cards render host titles); the case stays here until `core:ui` has cases of its own.

**May depend on:** core:designsystem, core:model and core:common (`api`, they appear in the public types), lifecycle-viewmodel, coroutines.
