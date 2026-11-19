# 0014 — Static analysis and dependency hygiene: detekt, Konsist, dependency-analysis, Renovate

- **Status:** accepted
- **Date:** 2026-11-15
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** quality, build

## Context and problem statement

The design system's standards are explicit that "a rule without enforcement is a wish" and name lint as the enforcer for many rules (no raw colours/dp/sp, no left/right, no string literals into text, no inset modifiers in features, no literal durations). We also need code style, Compose best practices, dependency hygiene and a way to keep ~40 libraries current. We need the tool set and how the rules are divided among them.

## Decision drivers

- One linter, one configuration; no overlapping tools.
- Custom rules for the design-system standards, operating on Kotlin PSI in `commonMain`.
- Architecture rules at the module/declaration level.
- Dependencies declared correctly and kept current with minimal toil.

## Options considered

1. **detekt (with formatting + Compose rules + custom rule set) · Konsist · dependency-analysis · Renovate.**
2. **ktlint + Android Lint custom checks** for the standards.
3. **Spotless + detekt + Dependabot.**

## Decision outcome

**Chosen: Option 1.**

| Concern | Tool | Notes |
|---|---|---|
| Style and formatting | detekt 1.23.8 with `detekt-formatting` | single config in `config/detekt/detekt.yml`; empty baseline |
| Compose best practices | `io.nlopez.compose.rules:detekt` 0.6.8 | modifier ordering, parameter order, preview naming, etc. |
| Design-system standards (C-01, M-01, L-02, S-01/S-02, I-01) | **custom detekt rule set** in `tooling/detekt-rules` | PSI rules: literal `Color(`/hex/`.dp`/`.sp`/ms in `feature:*` and `core:ui`; `left`/`right`/`absoluteOffset`/negative `scaleX`; string literals or templates passed to text parameters; `windowInsetsPadding`/`statusBarsPadding`/`navigationBarsPadding`/`imePadding` outside the five inset owners |
| Module graph, naming, forbidden imports, "every ViewModel has a test" | Konsist 0.18.1 in `tooling/konsist` | declaration-level assertions across the repo |
| Dependency correctness | dependency-analysis 3.9.0 `buildHealth` | applied per module by the convention plugins (3.x no longer auto-applies from the root); fails on undeclared (transitively used) or misconfigured dependencies; "unused" warns while modules are skeletons and flips to fail at the end of Phase 2. A `compose-multiplatform` bundle stops it asking for the `androidx.compose.*` twins of the `org.jetbrains.compose.*` coordinates we declare. Tested by its authors with AGP ≤ 9.1.0; runs with 9.3.1 under a warning |
| Dependency updates | Renovate (GitHub App) | weekly, grouped by ecosystem (Kotlin/Compose/AndroidX together), no auto-merge initially |

Division of labour: detekt sees expressions (literals, call arguments); Konsist sees declarations and files (imports, class hierarchies, module membership). Both run in `check`.

### Consequences

- Good: every standard that says "Lint" has an executable home; violations fail the PR with a message that names the rule (e.g. `SojournTokensOnly: raw dp literal; use SojournSpacing`).
- Good: a single linter and config; formatting handled by the same tool.
- Good: `buildHealth` keeps module boundaries honest (no accidental transitive reliance).
- Bad: custom detekt rules are code to maintain (~6 rules). Mitigated by tests in `tooling/detekt-rules` with positive and negative snippets.
- Bad: Renovate PR volume. Mitigated by weekly schedule and grouping; the version catalog keeps bumps in one file.
- Neutral: Android Lint still runs for Android-specific checks in `app:android` via AGP defaults.

### Confirmation

- `pr.yml` runs `detekt`, `:tooling:konsist:test`, `buildHealth`; all are required checks.
- `tooling/detekt-rules` tests cover each rule.

## Pros and cons of the options

### Option 1 — detekt + Konsist + dependency-analysis + Renovate

- Good: one linter; PSI-level custom rules in common code; declaration-level architecture rules; proven tools.
- Bad: custom rules to write.

### Option 2 — ktlint + Android Lint custom checks

- Good: Android Lint integrates with Android Studio inspections.
- Bad: Android Lint does not run on `commonMain` of KMP modules in the same way and is Android-centric; ktlint is formatting only, so a second tool is needed anyway.

### Option 3 — Spotless + detekt + Dependabot

- Good: Spotless is a good formatter driver; Dependabot is built in.
- Bad: two tools where `detekt-formatting` covers it; Dependabot lacks Renovate's grouping and version-catalog awareness.

## Implementation notes

**2026-11-16 (end of Phase 2).** "Unused" now fails like every other category. Modules that are still skeletons warn instead; they are listed explicitly as `skeletonModules` in the root `build.gradle.kts` and each leaves the list in the PR that gives it code. Two more bundles, `lifecycle` and `savedstate`, cover the JetBrains multiplatform forks that resolve to Google's AndroidX artifacts on Android, as the Compose bundle already did. Konsist rules now scope to product code (`app/`, `core/`, `feature/`): the build logic and tooling may use JDK APIs.

**2026-11-16 (design-system rules).** `tooling/detekt-rules` ships five rules in the `sojourn` rule set: `TokensOnly` (C-01), `MotionTokensOnly` (M-01), `StartEndOnly` (L-02, L-05), `NoTextLiterals` (S-01, S-02) and `InsetsOnlyInOwners` (I-01, I-03). Each has a failing and a passing snippet in `SojournRulesTest`, and all five were seen firing in a real feature module before merge. Two choices: hex strings are not matched separately because a colour in Compose is always a `Color(…)` call, and `0.dp` is allowed because zero is the absence of a value. Scopes live in `config/detekt/detekt.yml`; `core:designsystem` is exempt from the token, motion and direction rules because it is where tokens become components.

**2026-11-19 (end of Phase 5).** With Profile, every feature module has code, so `skeletonModules` is empty and every module fails on every category. The list stays, for a module added before its code.

## Links

- [detekt](https://detekt.dev/) · [Writing custom detekt rules](https://detekt.dev/docs/introduction/extensions) · [Compose rules](https://mrmans0n.github.io/compose-rules/)
- [Konsist](https://docs.konsist.lemonappdev.com/) · [dependency-analysis-gradle-plugin](https://github.com/autonomousapps/dependency-analysis-gradle-plugin) · [Renovate](https://docs.renovatebot.com/)
- Design system: `docs/design-system/engineering-standards.md`
- Related: ADR 0003, ADR 0008, ADR 0010
