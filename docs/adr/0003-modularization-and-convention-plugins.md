# 0003 — `core/*` + `feature/*` modularization with `build-logic` convention plugins

- **Status:** accepted
- **Date:** 2026-11-15
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** architecture, build

## Context and problem statement

The skeleton has one `shared` module. Ten screens, a 26-component design system, localization, data and navigation will not stay maintainable in one module, and the design system's engineering standards require boundaries a tool can enforce ("feature modules cannot depend on Material or platform UI directly"). We need a module structure that encodes layering, keeps build files tiny, and reads as industry-standard to a reviewer.

## Decision drivers

- Enforceable dependency rules (features cannot reach data infrastructure or Material).
- Build parallelism and compile avoidance as the codebase grows.
- Every module's `build.gradle.kts` should be a few lines; configuration lives in one place.
- Familiarity: a senior Android/KMP engineer should recognise the layout instantly.

## Options considered

1. **`core/*` + `feature/*`** (Now in Android style, adapted to KMP) with `build-logic` convention plugins.
2. **Layered only** — `designsystem`, `ui`, `domain`, `data`, `database`; all screens in `ui`.
3. **Feature `api`/`impl` split** — every feature has a public `:api` and a private `:impl`.

## Decision outcome

**Chosen: Option 1.** Layers as `core:*` modules, one `feature:*` module per screen, thin `app:android` / `app:shared` shells, and an included `build-logic` build providing `sojourn.*` convention plugins so each module declares only its plugin and dependencies. The exact module list and allowed edges are in [`architecture/modules.md`](../architecture/modules.md).

### Consequences

- Good: the dependency graph is the architecture; Konsist asserts it.
- Good: features build in parallel and do not recompile each other.
- Good: Material is physically confined to `core:designsystem`.
- Good: convention plugins make adding a module a three-line change and keep Kotlin/AGP/Compose configuration in one place.
- Bad: ~25 modules and some ceremony for a demo. Mitigated by convention plugins, module READMEs and the modules page.
- Bad: configuration time grows with module count. Mitigated by Gradle configuration cache (already on) and parallel builds.
- Neutral: Navigation keys live in `core:navigation` so features never depend on each other (see ADR 0005).

### Confirmation

- `tooling/konsist` tests: allowed-dependency matrix; `feature:*` never imports `core.data`, `core.database`, `core.network` or another feature; Material imports only in `core:designsystem`.
- `buildHealth` (dependency-analysis) fails on undeclared or unused dependencies.

## Pros and cons of the options

### Option 1 — `core/*` + `feature/*`

- Good: recognised pattern; strong boundaries; scales.
- Bad: module count; needs convention plugins to stay tidy (we add them).

### Option 2 — Layered only

- Good: five or six modules; fast to set up.
- Bad: all screens share one module, so feature boundaries blur and any screen change recompiles all UI; cannot enforce feature isolation.

### Option 3 — `api`/`impl` per feature

- Good: maximum decoupling; best compile avoidance; features can call each other via `api` only.
- Bad: roughly doubles module count; the indirection is not needed when features communicate only through navigation keys and shared use cases.

## Links

- [Now in Android — modularization learning journey](https://github.com/android/nowinandroid/blob/main/docs/ModularizationLearningJourney.md)
- [Gradle — sharing build logic with convention plugins](https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html)
- [AGP — Kotlin Multiplatform library plugin](https://developer.android.com/kotlin/multiplatform/plugin)
- Related: ADR 0005, ADR 0014
