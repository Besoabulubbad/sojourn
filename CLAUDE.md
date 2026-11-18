# CLAUDE.md — working in Sojourn

This file is the contract between the codebase and any AI agent (Claude Code, the PR-review action, or a teammate's assistant). Read it fully before changing anything. Humans: it is also the shortest accurate summary of how this repo works.

## What this is

Sojourn is a premium flexible-stay reservation app (nights to a year): a Kotlin Multiplatform codebase with a shared Compose Multiplatform UI for Android and iOS, English and Arabic as equals, implementing the design system in `design/` and `docs/design-system/`. Architecture and every decision are documented in `docs/`; decisions are binding until an ADR supersedes them. The repository is public and is itself a showcase: code, docs and history are all on display.

## Before you change code

1. Read `docs/architecture/overview.md` and the relevant guide in `docs/guides/`.
2. Check `docs/adr/README.md` — if your change contradicts an accepted ADR, stop and propose a new ADR instead of coding around it.
3. For any library you touch, follow its **official documentation** for the version pinned in `gradle/libs.versions.toml`. Use the Context7 MCP server (`.mcp.json`) to fetch version-exact docs; do not rely on memory for APIs.

## Commands

```bash
./gradlew check                         # everything CI runs on a PR (unit, konsist, screenshots, detekt)
./gradlew :core:designsystem:allTests   # one module
./gradlew verifyRoborazziAndroidHostTest   # Android screenshot compare (check also verifies)
./gradlew iosSimulatorArm64Test            # iOS unit tests + iOS screenshot verify (needs Xcode)
./gradlew recordRoborazziAndroidHostTest   # re-record Android baselines (only after a reviewed visual change)
./gradlew recordRoborazziIosSimulatorArm64 # re-record iOS baselines (same rule)
./gradlew :core:localization:verifyPlurals # every locale has every key; Arabic plurals have six forms
./gradlew detekt                        # static analysis
./gradlew buildHealth                   # dependency analysis
./gradlew :app:android:assembleDebug
./gradlew :app:shared:linkDebugFrameworkIosSimulatorArm64   # needs Xcode
```

## Non-negotiable rules (enforced by Konsist, detekt or the type system)

detekt's `sojourn` rule set (`tooling/detekt-rules`) enforces the token, motion, direction, text and inset rules below; a finding names the rule and the standard it breaks.


- **Modules.** `feature:*` may depend on `core:ui`, `core:designsystem`, `core:localization`, `core:domain`, `core:model`, `core:navigation`, `core:platform`, `core:common` — never on `core:data`, `core:database`, `core:network` or another feature. `app:shared` is the only module that sees everything.
- **Material stays inside `core:designsystem`.** No `androidx.compose.material3` or `org.jetbrains.compose.material3` import anywhere else. Features compose `Sojourn*` components.
- **Tokens by name only.** No raw `Color(...)`, hex, `.dp`, `.sp` or millisecond literals in `feature:*` or `core:ui`. Use `SojournTheme.colors.*`, `SojournTheme.type.*`, `SojournSpacing.*`, `SojournShapes.*`, `SojournMotion.*`. Generated token files are never edited by hand — change `design/tokens.json`.
- **Strings from resources.** No user-facing literals; no concatenation for UI text; every count uses a plural resource with all six Arabic forms.
- **Start/end, never left/right.** No `Alignment.Left`, `absoluteOffset`, or `scaleX = -1f` hacks.
- **Prices only through `SojournPrice` from a `PriceDisplay`.** No composable accepts a bare number as a price.
- **Every data screen** models `ScreenState<T>` (Loading / Content / Empty / Error / Offline) exhaustively.
- **Host-written text** renders only through `HostText` (S-06); **icons** only through `SojournIcon` (no raw painters, no call-site mirroring).
- **Insets** are consumed only by `SojournScaffold`, `SojournTopBar`, `SojournBottomNav`, `SojournBookingBar`, `SojournActionBar`, `SojournSheet`.

## Patterns

- **Presentation:** one `SojournViewModel<State, Intent, Effect>` per screen; `XxxRoute` (stateful, Koin-injected) + `XxxScreen(state, onIntent)` (stateless, previewable). See `docs/guides/adding-a-feature.md`.
- **Navigation:** keys are `@Serializable` and live in `core:navigation`; each feature exposes an `EntryProviderScope` extension; `app:shared` assembles the `NavDisplay`. Non-JVM targets need explicit polymorphic serializer registration — keep `sojournNavSerializersModule` complete.
- **DI:** one Koin module per Gradle module, named `<module>Module`; ViewModels via `viewModelOf(::XxxViewModel)`; `SojournModulesTest` in `app:shared` runs Koin's `verify()` over `sojournModules` to guard the graph.
- **Data:** repositories in `core:data` expose `Flow`; SQLDelight is the source of truth; `SojournApi` is the network seam (currently `LocalSojournApi` over bundled JSON).

## Conventions

- Kotlin official style, `.editorconfig` applies; detekt is the single linter.
- Naming: `Sojourn` prefix for design-system components and tokens (`SojournButton`, `SojournColors`); `XxxViewModel`, `XxxUiState`, `XxxIntent`, `XxxEffect`, `XxxRoute`, `XxxScreen`.
- Commits follow Conventional Commits (`feat(details): add gallery failure state`); scope = module name without the `feature:`/`core:` prefix. Agent-authored commits carry a `Co-Authored-By` trailer.
- Every PR that adds or changes a screen must satisfy the Definition of Done in `docs/guides/adding-a-feature.md`.
- A visual change that alters a screenshot baseline needs an explicit note in the PR and a reviewed re-record.

## Documentation duties

- New library or pattern → new ADR (`docs/adr/`, copy `0000-template.md`).
- New module → entry in `docs/architecture/modules.md` and a module `README.md`.
- New design-system component → row in `docs/design-system/mapping.md`, KDoc on the public composable, a preview, and `SnapshotCase`s in `commonTest` (rendered on Android and iOS).
- A module gets its first code → remove it from `skeletonModules` in the root `build.gradle.kts` so "unused dependency" fails for it too.
- Public API changes in `core:*` → update KDoc; Dokka publishes it.
- New screen merged → its showcase screenshots are exported (`docs/showcase/`) in the same PR or the follow-up noted.

## Do not

- Do not add Hilt/Dagger, Navigation 2, Room, Retrofit, Moshi, RxJava or Material 2. The alternatives are decided (see ADRs 0004–0007).
- Do not call platform locale APIs outside `core:localization`.
- Do not introduce a dependency without adding it to `gradle/libs.versions.toml` and running `buildHealth`.
- Do not record new screenshot baselines to make CI pass without a reviewed visual change.
- Do not commit secrets; signing and API keys come from GitHub Actions secrets or `local.properties` (git-ignored).
- Do not reintroduce any real company's name or branding; the product is fictional by design.
