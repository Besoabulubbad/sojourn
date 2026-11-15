# 0001 — Kotlin Multiplatform with a shared Compose Multiplatform UI

- **Status:** accepted
- **Date:** 2026-11-15
- **Deciders:** Mohammad Abu Lubad (owner), Claude (advisor)
- **Tags:** platform, ui

## Context and problem statement

Sojourn targets guests on Android and iOS. The design system is written as "one Kotlin Multiplatform codebase with shared Compose UI" and specifies every screen in four variants (English/Arabic × light/dark) with platform-specific touches (Google Pay vs Apple Pay, predictive back vs edge swipe, haptics). The project skeleton already targets Android, `iosArm64` and `iosSimulatorArm64` with Compose Multiplatform 1.12. We need to decide how much UI is shared, because every later decision (navigation, DI, testing) depends on it.

## Decision drivers

- One source of truth for a design system that is explicitly shared.
- Demo effort: eleven screens, each in four variants, must reach the same polish.
- Credible native behaviour where the design system calls for it.
- Show a modern multiplatform engineering story.

## Options considered

1. **Shared Compose Multiplatform UI** — one Compose UI for both platforms; `expect`/`actual` adapters for native touches.
2. **KMP business logic + native UI** — share data/domain in Kotlin; Compose on Android, SwiftUI on iOS.
3. **Android first, KMP-ready** — build Android only now, keep boundaries clean for iOS later.

## Decision outcome

**Chosen: Option 1, shared Compose Multiplatform UI.** It matches the skeleton and the design system, gives maximum reuse for a breadth-first scope, and Compose Multiplatform for iOS has been stable since 1.8 (May 2025). Native differences are small and well-bounded (payment sheets, haptics, back gesture, share), so they fit `core:platform` adapters rather than justifying a second UI.

### Consequences

- Good: 26 components and 10 screens are written once; the screenshot matrix runs once on the JVM and covers both platforms' shared UI.
- Good: Navigation, state and DI are decided once (ADRs 0004–0006).
- Good: iOS gets Arabic/RTL, theming and motion for free from the same code.
- Bad: iOS feel is "Compose on iOS", not SwiftUI. Mitigated by platform adapters, iOS-specific gestures via `navigationevent`, and testing on real devices in Phase 6.
- Bad: Local iOS builds need Xcode (currently not installed). Mitigated: CI macOS runner compiles the framework from Phase 1; Xcode is being installed before Phase 1.
- Neutral: Some Jetpack libraries are consumed through JetBrains' multiplatform forks (`org.jetbrains.androidx.*`). The tech-stack page tracks the pairing.

### Confirmation

- `main.yml` compiles `:app:shared:linkDebugFrameworkIosSimulatorArm64` and builds the iOS simulator app on every merge.
- Konsist forbids Android-only imports in `commonMain` of every module.

## Pros and cons of the options

### Option 1 — Shared Compose Multiplatform UI

- Good: one UI codebase, one design-system implementation, fastest to full breadth.
- Good: aligns with the skeleton and the design system's own module shape.
- Bad: not pixel-identical to SwiftUI idioms; some iOS platform APIs reached through interop.

### Option 2 — KMP logic + native UI

- Good: best possible iOS feel; demonstrates both UI stacks.
- Bad: the design system is implemented twice; ~2× UI effort for 11 screens × 4 variants; two screenshot suites; divergence risk.

### Option 3 — Android first

- Good: deepest Android polish per hour; smallest scope.
- Bad: ignores the multiplatform story that is already set up; iOS becomes a later rewrite of assumptions.

## Links

- [Compose Multiplatform documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform.html)
- [Compose Multiplatform 1.8 — iOS stable](https://blog.jetbrains.com/kotlin/2025/05/compose-multiplatform-1-8-0-released-compose-multiplatform-for-ios-is-stable-and-production-ready/)
- Related: ADR 0005 (navigation), ADR 0010 (testing on the JVM)
