# 0004 — Dependency injection with Koin

- **Status:** accepted
- **Date:** 2026-11-15
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** architecture, di

## Context and problem statement

A modular multiplatform app needs a way to assemble repositories, use cases and ViewModels across ~25 modules, on Android and iOS, with Compose Multiplatform and Navigation 3 integration. Hilt/Dagger are Android/JVM-only, so the realistic candidates are the multiplatform DI libraries.

## Decision drivers

- First-class Kotlin Multiplatform and Compose Multiplatform support, including ViewModel and Navigation 3 scoping.
- Low ceremony per module; each Gradle module contributes one DI module.
- Verifiable graph in CI.
- Documentation quality and ecosystem familiarity.
- Stability against Kotlin version upgrades.

## Options considered

1. **Koin 4** — runtime DI with DSL; `koin-compose`, `koin-compose-viewmodel`, `koin-compose-navigation3`; optional Koin Annotations (KSP) for compile-time checks.
2. **kotlin-inject (+ anvil)** — compile-time DI via KSP, Dagger-like components.
3. **Metro** — Kotlin compiler-plugin DI, KMP-first, successor to Dagger/Anvil/kotlin-inject.

## Decision outcome

**Chosen: Option 1, Koin 4.2.2.** It is the de-facto KMP DI with first-party Compose Multiplatform, ViewModel and Navigation 3 integration (verified: `io.insert-koin:koin-compose-navigation3:4.2.2` on Maven Central), excellent documentation and the least ceremony. Its main weakness — runtime resolution — is mitigated with a `checkModules()` verification test.

### Consequences

- Good: one `val <name>Module = module { … }` per Gradle module; `viewModelOf(::XxxViewModel)`; `koinViewModel()` in Route composables; NavEntry-scoped ViewModels via `koin-compose-navigation3`.
- Good: iOS initialises the same graph via `initKoin()` from the shared framework.
- Good: simple to swap implementations for tests (`core:testing` provides a fake module).
- Bad: a missing binding is a runtime error, not a compile error. Mitigated by `checkModules()` in `app:shared` tests on every PR, and the option to adopt Koin Annotations (KSP) later for compile-time graph verification without changing the architecture.
- Neutral: Service-locator style `get()` is forbidden in feature code by convention; constructor injection only (Konsist checks `KoinComponent` is not implemented outside `app:shared`).

### Confirmation

- `app:shared` test: `koinApplication { modules(allModules) }.checkModules()`.

**2026-11-17 (Phase 4).** `checkModules()` has been deprecated since Koin 4.0. Koin's verification docs point to `Module.verify()` (JVM only). The graph check is therefore `SojournModulesTest` in `app:shared`'s `androidHostTest`. It calls `module { includes(sojournModules) }.verify(extraTypes = listOf(Context::class))`, and a missing binding fails it with `MissingKoinDefinitionException` naming the type and the definition that needs it. The decision is unchanged; only the API used to confirm it changed.
- Konsist: no class outside `app:shared` implements `KoinComponent`; every `feature:*` exposes exactly one `Module` named `<feature>Module`.

## Pros and cons of the options

### Option 1 — Koin

- Good: KMP-native; CMP + ViewModel + Nav3 integration; large community; readable DSL.
- Bad: runtime resolution; slight startup cost for module loading (negligible at this scale).

### Option 2 — kotlin-inject

- Good: compile-time safety; no reflection; Dagger-familiar.
- Bad: KSP in every module slows incremental builds; smaller ecosystem; Compose/Nav3 integration is hand-rolled.

### Option 3 — Metro

- Good: compile-time, minimal boilerplate, designed for KMP; strong "bleeding edge" signal.
- Bad: compiler plugins are tied to exact Kotlin versions (we are on 2.4.21 and will keep upgrading); young ecosystem; harder to recover from an incompatible release mid-project.

## Links

- [Koin — Compose Multiplatform](https://insert-koin.io/docs/reference/koin-compose/compose)
- [Koin — Kotlin Multiplatform setup](https://insert-koin.io/docs/reference/koin-mp/kmp)
- [Koin — Navigation 3](https://insert-koin.io/docs/reference/koin-compose/navigation3) *(verify exact page for 4.2.x during Phase 2)*
- Related: ADR 0005, ADR 0006
