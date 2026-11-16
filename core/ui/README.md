# core:ui

The presentation building blocks every screen uses (ADR 0006).

| Thing | Role |
|---|---|
| `ScreenState<T>` | Loading / Content / Empty / Error / Offline (with cached data). Sealed, so `when` is exhaustive (I-05). `ScreenState.fromError()` maps `AppError.Offline` to Offline and everything else to Error. |
| `SojournViewModel<S, I, E>` + `UiState` / `UiIntent` / `UiEffect` | The MVI base: one `state: StateFlow<S>`, `onIntent(I)`, one-off `effects`. Subclasses implement `handle(intent)` with `setState { }` and `sendEffect()`. |
| `CollectEffects(effects) { }` | Collects effects in an `XxxRoute` for as long as it is composed. |
| `SojournScaffold` | Frame and inset owner (I-01): measures the docked bars and hands content the padding it needs (I-03). Screens never read insets. |
| `HostText` | The only way to render host-written text: its own language and direction whatever the UI language (S-06). |

## Tests

- `SojournViewModelTest`: intents reduce state, effects arrive once, error-to-state mapping.
- `HostTextScreenshotTest`: an English and an Arabic host title keep their own direction in all four variants.

**May depend on:** core:designsystem, core:model and core:common (`api`, they appear in the public types), lifecycle-viewmodel, coroutines.
