# tooling:detekt-rules

The design system's engineering standards as detekt rules (ADR 0014): a rule without enforcement is a wish. Loaded into every module's detekt run by the `sojourn.detekt` convention plugin; configured under `sojourn:` in `config/detekt/detekt.yml`.

| Rule | Standard | Flags | Applies to |
|---|---|---|---|
| `TokensOnly` | C-01 | `Color(…)`; `.dp` / `.sp` / `.em` on a number literal (`0.dp` is allowed) | `feature/*`, `core/ui` |
| `MotionTokensOnly` | M-01 | a number literal as `durationMillis` / `delayMillis`, or as `tween(…)`'s first argument | `feature/*`, `core/ui` |
| `StartEndOnly` | L-02, L-05 | `AbsoluteAlignment`, `absoluteOffset`, `absolutePadding`, absolute corner shapes, `TextAlign`/`Arrangement`/`Alignment` `.Left` / `.Right` / `.Absolute`, `scaleX = -1f` | `feature/*`, `core/ui` |
| `NoTextLiterals` | S-01, S-02 | a string literal, template or `+` concatenation passed as the text of `Text`, `BasicText` or any `Sojourn*Text` | `feature/*`, `core/ui` |
| `InsetsOnlyInOwners` | I-01, I-03 | `WindowInsets` and every inset modifier (`statusBarsPadding`, `imePadding`, `windowInsetsPadding`, …) outside `SojournScaffold`, `SojournTopBar`, `SojournBottomNav`, `SojournBookingBar`, `SojournSheet` | `app/*`, `core/*`, `feature/*` |

`core:designsystem` is exempt from the token, motion and direction rules: it is where tokens become components and where `SojournIcon` does the one sanctioned mirroring. Test source sets are exempt everywhere.

## Working on the rules

- The rules are PSI-only (no type resolution), so they run in the normal `detekt` task and stay fast.
- `SojournRulesTest` lints a failing and a passing snippet per rule: `./gradlew :tooling:detekt-rules:test`.
- detekt runs the rules on its own Kotlin 2.0 runtime, so this module compiles with `apiVersion = 2.0`.
- The detekt Gradle plugin caches its classloader per daemon. After changing a rule, run `./gradlew --stop` before checking it against the repository, or detekt keeps the previous rule classes. CI always starts fresh.
- Adding a rule: implement it in `Rules.kt`, register it in `SojournRuleSetProvider`, add its defaults to `src/main/resources/config/config.yml` and its scope to `config/detekt/detekt.yml`, add a test, and add a row here and in ADR 0014.
