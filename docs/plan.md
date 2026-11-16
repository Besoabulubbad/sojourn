# Plan

How Sojourn goes from an empty Kotlin Multiplatform skeleton to a release-grade, public showcase. Phases are ordered by dependency, each with an **exit criterion** that is observable in the repository — a green workflow, a published page, a passing test matrix — not a feeling of done.

The order is deliberate: **foundations → design system → data → screens**. Screens are then composition of finished parts, which is how ten of them reach the same polish bar. A **showcase workstream** runs alongside so the GitHub page sells the work as it lands.

## What this repository is for

A public demonstration of **AI-first mobile software engineering**: a product-grade app built with coding agents inside a documented, tool-enforced architecture, where a reader can inspect every decision, every rule's enforcement, and every screen in four variants. The product (Sojourn, a premium flexible-stay reservation app) is fictional; the engineering is real.

## Decisions this plan rests on

Locked on 2026-11-15 through three rounds of options and trade-offs; each has an ADR.

| Area | Decision | ADR |
|---|---|---|
| Platform & UI | Kotlin Multiplatform, shared Compose Multiplatform UI for Android + iOS | [0001](adr/0001-kotlin-multiplatform-shared-compose-ui.md) |
| Scope | Full breadth — all design-system screens, each in EN/AR × light/dark | [0002](adr/0002-scope-and-build-order.md) |
| Modules | `core/*` + `feature/*`, thin `app/*`, `build-logic` convention plugins | [0003](adr/0003-modularization-and-convention-plugins.md) |
| DI | Koin 4 | [0004](adr/0004-dependency-injection-koin.md) |
| Navigation | Navigation 3 (JetBrains multiplatform artifacts) | [0005](adr/0005-navigation-3.md) |
| Presentation | MVI / unidirectional data flow | [0006](adr/0006-mvi-presentation.md) |
| Data | Offline-first SQLDelight; Ktor-ready `SojournApi` seam over bundled data | [0007](adr/0007-offline-first-sqldelight.md) |
| Design tokens | Generated Kotlin from `design/tokens.json` | [0008](adr/0008-design-tokens-generated.md) |
| Localization | `AppLocale` single source; EN + AR; bidi-safe formatters | [0009](adr/0009-localization-applocale.md) |
| Testing | Unit · Konsist · Roborazzi matrix · Compose UI · Maestro | [0010](adr/0010-testing-pyramid.md) |
| CI/CD | GitHub Actions: PR, main (incl. iOS), release, docs; Conventional Commits + release-please | [0011](adr/0011-ci-cd-github-actions.md) |
| Docs | MkDocs Material + Dokka on GitHub Pages; ADRs | [0012](adr/0012-docs-as-code.md) |
| AI | Agent contract, MCP doc servers, automated PR review, release/docs automation; no AI in-app | [0013](adr/0013-ai-agents-dev-loop.md) |
| Hygiene | detekt + Compose rules + custom standards rules, dependency-analysis, Renovate | [0014](adr/0014-static-analysis-dependency-hygiene.md) |

Setup facts: public repository `sojourn` from day one; package root `com.abulubad.sojourn`; MIT license.

## Phases

### Phase 0 — Repository and documentation foundation

Make the repo a place where every later change has a home and a rule — and a page worth landing on.

- `git init`, `.gitignore`, public GitHub repository, branch protection on `main` (PR-only, required checks, conversation resolution, linear history; approvals required once a second maintainer exists), MIT `LICENSE`.
- This documentation set: README (landing page), `CLAUDE.md`, CONTRIBUTING, architecture, modules, tech stack, design-system mapping + rebranded brand book/standards/motion/rationale, 14 ADRs, guides.
- `.mcp.json` (Context7, GitHub) and the AI-tooling section of the local-setup guide.
- `mkdocs.yml`; `docs.yml` workflow publishing the site to GitHub Pages on every merge.
- PR and issue templates, `CODEOWNERS`, `.editorconfig`.
- `claude-review.yml`: automated PR review against `CLAUDE.md` and the ADRs from the very first PR.
- `design/tokens.json` (renamed copy of the design system's tokens).

**Exit:** repository public with the docs site live from `main`; the first PR received an automated review; ADRs 0001–0014 `accepted`; README renders with placeholders clearly marked.

### Phase 1 — Build system and quality gates

Make the rules executable before there is code to break them.

- `build-logic/` convention plugins: `sojourn.kmp.library`, `sojourn.kmp.feature`, `sojourn.compose`, `sojourn.android.application`, `sojourn.koin`, `sojourn.sqldelight`, `sojourn.screenshot`, `sojourn.detekt`, `sojourn.dokka`, `sojourn.tokens`.
- Version catalog completed with the verified versions in [tech-stack.md](tech-stack.md); Gradle 9.5.1 → 9.7.1 and AGP 9.1.1 → 9.3.1 — the newest pair inside Kotlin 2.4.21's documented compatibility window (AGP 9.5 would need Gradle 9.6+ and sits outside KGP's tested range). `sojourn.screenshot` is wired in Phase 2 with the first screenshot test.
- detekt with Compose rules; `tooling/detekt-rules` custom rule set (tokens only, start/end only, no literals into text, insets only in owners, motion tokens only) with tests.
- `dependency-analysis` (`buildHealth`); Renovate config.
- `tooling/konsist`: module-dependency rules, naming rules, "no Material outside designsystem".
- Empty module skeleton for every `core:*`, `feature:*` and `app:*` module, each with a README, so the rules are exercised from day one.
- `pr.yml` (ubuntu): detekt → konsist → unit tests → screenshot verify → buildHealth → assembleDebug, with JUnit report comments and diff-image artifacts.
- `main.yml`: `pr.yml` + macOS job compiling the iOS framework and simulator app.

**Exit:** `./gradlew check` is green in CI on the skeleton; a deliberate rule violation on a branch fails the PR; iOS compiles on the macOS runner.

### Phase 2 — Core foundations

The parts every screen depends on.

- `core:model` — domain types (`Stay`, `City`, `Quote`, `PriceDisplay`, `Booking`, `SearchQuery`, `Filters`, `Policy`, `Amenity`).
- `core:common` — dispatchers, `Clock`, `Result`/error types, Kermit logging facade.
- `core:localization` — `AppLocale` (en-GB, ar), `LocalAppLocale`, `SojournFormatter` (money, dates, counts, bidi isolates), string and plural resources with all six Arabic forms, CI check for missing plural forms.
- `sojourn.tokens` generator → `SojournColors`, `SojournType`, `SojournSpacing`, `SojournShapes`, `SojournElevation`, `SojournSizes`, `SojournMotion`; token-level contrast test (4.5:1 text, 3:1 graphics, both themes).
- `core:designsystem` — `SojournTheme`, font resources (Alexandria, Readex Pro), `SojournIcon` sealed set with auto-mirroring, theme previews.
- `core:ui` — `ScreenState<T>`, `SojournViewModel<S, I, E>`, `SojournScaffold` (owns insets and docked-bar padding), `HostText`, preview and screenshot parameter providers for the 4-variant matrix.
- `core:navigation` — `SojournNavKey` hierarchy, `Navigator`, `SerializersModule` assembly, tab back-stack holder, scene strategies.
- `core:platform` — `expect/actual` for haptics, share sheet, open-in-maps, payment sheet abstraction, reduce-motion setting.
- `core:testing` — fixtures, fake repositories, screenshot test base, Turbine/coroutines helpers, Koin test rules.
- **Showcase:** wordmark and app icon drawn from the tokens (`design/brand/`), applied to both platforms; theme swatch sheet exported to `docs/showcase/`.

**Exit:** `SojournTheme` renders every token in 4 variants in screenshot tests; contrast test passes; `AppLocale` switching flips direction and formatting in unit tests; app icon visible on both launchers; `buildHealth` switched from warn to fail on unused dependencies now that modules carry code.

**Status (2026-11-16): done.** Evidence: `TokenSheetScreenshotTest` renders every token family in the four variants; `ContrastTest` passes in both themes; `AppLocaleControllerTest` and `SojournFormatterTest` cover switching, direction and formatting; the generated adaptive icon (Android) and light/dark app icons (iOS) are in the shell projects; `buildHealth` fails on unused dependencies for every module with code. Deviations, each documented: navigation scene strategies and `SojournMotion` transitions move to Phase 5 with the first `NavDisplay`; `SojournScaffold` is in, the docked bars that own the other insets arrive with their components in Phase 3; the 16-cell screenshot matrix is defined (`SojournVariants.Matrix`) and first used by components in Phase 3; screenshots render on Robolectric's API 36 runtime (ADR 0010 notes).

### Phase 3 — Design system components

All 26 components, each with KDoc, a preview per state, a README mirroring its design-system card, and a screenshot matrix (EN/AR × light/dark × 100%/200% font × 320/412 dp).

Order (dependencies first): `Price`, `Rating`, `Chip`, `Button`, `IconButton`, `Skeleton`, `Snackbar`, `EmptyState`, `TopBar`, `BottomNav`, `SearchBar`, `SegmentedControl`, `Stepper`, `KeyFacts`, `PolicyRow`, `PricePin`, `CityCard`, `StayCard`, `Gallery`, `DateRangePicker`, `RangeSlider`, `Sheet`, `ViewToggle`, `BookingBar`, `PriceBreakdown`, plus the `Motion` helpers (durations, easings, springs, reduce-motion fallbacks).

- **Showcase:** `exportShowcase` Gradle task composes a component contact sheet from the screenshot baselines into `docs/showcase/components.png`; the showcase gallery page lists every component in every variant.

**Exit:** every component's screenshots approved; [mapping.md](design-system/mapping.md) has a row per component with its API; Konsist confirms no Material leaks; the gallery page is live.

### Phase 4 — Data

- `core:database` — SQLDelight schema (`stay`, `city`, `photo`, `amenity`, `policy`, `quote`, `booking`, `saved`, `search_recent`), `SojournDatabase`, migrations from 1, seed loader.
- `core:network` — `SojournApi` interface, DTOs, `LocalSojournApi` (bundled JSON, simulated latency and failure modes for the offline/error states), Ktor client configured but unused by default (`KtorSojournApi` behind a flag).
- `core:data` — repositories exposing `Flow`; DB as source of truth; refresh policy; pricing invariants (breakdown lines sum to total; same quote id across surfaces).
- `core:domain` — use cases (`SearchStays`, `GetStayDetails`, `GetQuote`, `ToggleSaved`, `ListBookings`, `CreateBooking`, `UpdateFilters`).
- Seed catalogue: ~40 stays across London, Paris, Lisbon, Algiers, Constantine, Oran with bundled sample photography (licensed/CC0), policies with numbers, quotes per stay length.
- Unit tests on every repository and use case with fixtures; property-style tests on pricing.

**Exit:** app-independent test suite proves P-01…P-06 of the engineering standards; `checkModules()` verifies the Koin graph.

### Phase 5 — Screens

`app:shared` and `app:android` are wired at the start of this phase so the app runs end to end from the first feature. Features land in this order, each as its own PR meeting the Definition of Done:

1. `feature:explore` (home, stay-length shortcuts, cities)
2. `feature:search` (Where / When / Who flow)
3. `feature:results` (list, states)
4. `feature:filters` (sheet, price range on the cards' basis)
5. `feature:details` (gallery, facts, policies, location, booking bar)
6. `feature:checkout` (review, breakdown, pay, confirmation)
7. `feature:map` (price pins, clusters, search this area)
8. `feature:saved`
9. `feature:bookings`
10. `feature:profile` (language, theme, rewards)

The design system's "States" screen is realised as the `ScreenState` variants of Results and Details and is covered by their screenshot matrices.

- **Showcase:** each feature PR exports its 4-variant screens to `docs/showcase/screens/` and the README grid fills in; a recording per feature (`scripts/record-flow.sh`: emulator screen recording while a Maestro flow drives it, ffmpeg → GIF/WebP) lands in `docs/showcase/recordings/`.

**Exit:** all ten features merged; the four device/app language pairs pass; screenshot matrix complete for every screen state; README grid complete.

### Phase 6 — Platform polish and end-to-end

- iOS shell: safe areas, swipe-back, per-app language, Apple Pay sheet stub, app icon; TestFlight-ready archive job (unsigned until certificates exist).
- Android shell: predictive back, splash, per-app language via `LocaleManager`, Google Pay sheet stub.
- Haptics and reduce-motion paths; 200% font scale sweep; TalkBack and VoiceOver pass.
- `.maestro/` flows for the booking golden path and language switch; run in `main.yml`.
- **Showcase:** the hero recording (golden path with a mid-flow theme and language switch), the social preview image (1280×640) set on the repository, side-by-side Android/iOS recording.

**Exit:** Maestro flows green on CI emulators; accessibility checklist signed off; hero media in the README.

### Phase 7 — Release

- `release.yml`: tag → signed AAB/APK (secrets), iOS archive, GitHub Release with generated notes; release-please manages versions and CHANGELOG.
- Dokka published under `/api` on the docs site.
- Demo script, a short "tour" page on the site, release announcement.

**Exit:** `v0.1.0` published from `main` by the pipeline alone; the README and site present a complete, recorded product.

### Continuous (from the phase noted)

| Practice | From |
|---|---|
| Automated Claude PR review | Phase 0 |
| Docs site publish on merge | Phase 0 |
| Renovate dependency PRs (weekly, grouped) | Phase 1 |
| Screenshot baselines reviewed on every visual PR | Phase 3 |
| Showcase exports with every screen/component PR | Phase 3 |
| Docs-sync check when `core:*` public API changes | Phase 2 |

## Showcase workstream — deliverables

| Asset | Source | Lands |
|---|---|---|
| Wordmark + app icon (SVG, adaptive icon, iOS icon set) | drawn from tokens | Phase 2 |
| Theme swatch sheet | screenshot test export | Phase 2 |
| Component contact sheet + gallery page | `exportShowcase` from Roborazzi baselines | Phase 3 |
| Screen grids (4 variants per screen) in README | `exportShowcase` | Phase 5, per feature |
| Per-feature recordings (GIF/WebP) | `scripts/record-flow.sh` + Maestro | Phase 5, per feature |
| Hero recording, social preview, Android/iOS side-by-side | same | Phase 6 |
| "Tour" page on the docs site | hand-written | Phase 7 |

## Out of scope for the demo

Sign-up/login, real payments, a live backend, reviews list, push notifications, analytics. Each has an obvious seam (`SojournApi`, `core:platform`, a new `feature:*`), and the design system does not cover them either.

## How to change this plan

Edit this page in a PR. If a change alters a decision in the table above, it needs a superseding ADR first.
