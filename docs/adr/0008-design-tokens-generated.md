# 0008 — Design tokens generated from `tokens.json`

- **Status:** accepted
- **Date:** 2026-11-15
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** design-system, build

## Context and problem statement

The design system publishes `tokens.json` (colours in two themes, type styles, spacing, radii, shadows, sizes, durations, easings) and its engineering standards say tokens "are generated into Kotlin … nobody edits the generated files" and "no raw `Color(…)`, hex, `dp` or `sp` literals in feature code" (C-01, M-01). We need the mechanism that turns the JSON into Kotlin and keeps the two in lock-step.

## Decision drivers

- One source of truth shared with design; a token change is a JSON diff.
- Impossible to drift: a renamed or removed token must break the build.
- Contrast guarantees (C-02) checked automatically.
- Simple for contributors: a build, not a manual script.

## Options considered

1. **Hand-written theme objects** maintained to match the JSON.
2. **Generate Kotlin at build time** from `design/tokens.json` via a `build-logic` task; generated files not committed.
3. **Generate and commit** the Kotlin, regenerating via a Gradle task and failing CI if stale.

## Decision outcome

**Chosen: Option 2.** The `sojourn.tokens` convention plugin registers `generateDesignTokens`, which reads `design/tokens.json` and writes `SojournColors`, `SojournType`, `SojournSpacing`, `SojournShapes`, `SojournElevation`, `SojournSizes` and `SojournMotion` into `core:designsystem`'s generated source set before compilation. Names map kebab → lowerCamel. A unit test asserts one property per token, and a contrast test evaluates every text pair named in the tokens' usage notes in both themes.

### Consequences

- Good: zero manual sync; `git diff design/tokens.json` is the complete record of a visual change.
- Good: generated code is just another build output (like SQLDelight's), with no risk of a stale committed copy.
- Good: the generator is ~200 lines of Kotlin in `build-logic`, reviewable like any code.
- Bad: IDE symbol resolution requires a Gradle sync after a token change. Acceptable; identical to SQLDelight/KSP.
- Bad: shadows do not map 1:1 to Compose; we express them as elevation + ambient tint (`SojournElevation`). Documented in the mapping page.
- Neutral: Type styles reference font resources (`SojournFonts`) that are hand-maintained in `core:designsystem`; only the numbers are generated.

### Confirmation

- `generateDesignTokens` is wired into `compileKotlin*` for `core:designsystem`; CI compiles from a clean checkout.
- `core:designsystem` tests: token count/name parity; WCAG contrast (4.5:1 text, 3:1 large text/graphics) per usage pair, both themes.
- detekt custom rule: literal `Color(`, hex strings, `.dp`, `.sp`, and millisecond literals in `feature:*` and `core:ui` are errors.

## Pros and cons of the options

### Option 1 — Hand-written

- Good: no build machinery.
- Bad: drifts silently; violates the design system's own standard.

### Option 2 — Generate at build time

- Good: always in sync; no committed artefacts.
- Bad: IDE sync step; a little build-logic code.

### Option 3 — Generate and commit

- Good: browsable generated code; no sync step.
- Bad: two sources that can disagree; needs a staleness check in CI; noisy diffs.

## Implementation notes

**2026-11-16 (Phase 2).** `sojourn.tokens` runs `GenerateDesignTokensTask` (build-logic, ~250 lines, parsed with kotlinx-serialization's `JsonElement` tree) into `core/designsystem/build/generated/tokens`. Shapes of the generated API: colours and elevation are classes with `lightSojourn*()` / `darkSojourn*()` factories; `SojournType` is built by `sojournType(display, text)` from the two font families; spacing, sizes and shapes are objects; `SojournMotion` holds `const` durations (`instant` … `slower`) and `CubicBezierEasing`s (`standard`, `emphasized`, `exit`), with springs and reduce-motion fallbacks as hand-written extensions. Each object exposes `all` (name → value). The generator has its own unit test against the real `tokens.json` in the build-logic test suite, and `ContrastTest` checks 28 usage pairs per theme. All pass on the current tokens.

## Links

- Design system: `docs/design-system/engineering-standards.md` (Module shape; C-01, C-02, M-01)
- [Gradle — custom tasks and generated sources](https://docs.gradle.org/current/userguide/custom_tasks.html)
- Related: ADR 0014
