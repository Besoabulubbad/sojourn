# Architecture decision records

Every significant decision about this codebase, in [MADR](https://adr.github.io/madr/) format: the context, the options actually considered with their trade-offs, the choice, and its consequences. An ADR is immutable once accepted; a change of mind is a new ADR that supersedes it.

**Rule:** code that contradicts an accepted ADR is a defect, whoever wrote it. If the ADR is wrong, fix the ADR first.

## How to propose one

1. Copy [`0000-template.md`](0000-template.md) to `NNNN-short-kebab-title.md` with the next number.
2. Status `proposed`. Fill every section; "Options considered" must contain at least two real alternatives with honest negatives.
3. Open a PR titled `docs(adr): propose NNNN <title>`. The discussion lives on the PR.
4. On merge, status becomes `accepted` and the row below is added.

## Index

| # | Title | Status | Date |
|---|---|---|---|
| [0001](0001-kotlin-multiplatform-shared-compose-ui.md) | Kotlin Multiplatform with a shared Compose Multiplatform UI | accepted | 2026-11-15 |
| [0002](0002-scope-and-build-order.md) | Full-breadth scope, built foundations → design system → screens | accepted | 2026-11-15 |
| [0003](0003-modularization-and-convention-plugins.md) | `core/*` + `feature/*` modularization with `build-logic` convention plugins | accepted | 2026-11-15 |
| [0004](0004-dependency-injection-koin.md) | Dependency injection with Koin | accepted | 2026-11-15 |
| [0005](0005-navigation-3.md) | Navigation 3 (JetBrains multiplatform artifacts) | accepted | 2026-11-15 |
| [0006](0006-mvi-presentation.md) | MVI / unidirectional data flow for presentation | accepted | 2026-11-15 |
| [0007](0007-offline-first-sqldelight.md) | Offline-first data with SQLDelight and a Ktor-ready API seam | accepted | 2026-11-15 |
| [0008](0008-design-tokens-generated.md) | Design tokens generated from `tokens.json` | accepted | 2026-11-15 |
| [0009](0009-localization-applocale.md) | Localization: `AppLocale` as the single source for language and direction | accepted | 2026-11-15 |
| [0010](0010-testing-pyramid.md) | Testing pyramid: unit, architecture, screenshot matrix, UI, end-to-end | accepted | 2026-11-15 |
| [0011](0011-ci-cd-github-actions.md) | CI/CD on GitHub Actions with Conventional Commits and release-please | accepted | 2026-11-15 |
| [0012](0012-docs-as-code.md) | Docs as code: MkDocs Material + Dokka on GitHub Pages | accepted | 2026-11-15 |
| [0013](0013-ai-agents-dev-loop.md) | AI agents in the development loop (not in the product) | accepted | 2026-11-15 |
| [0014](0014-static-analysis-dependency-hygiene.md) | Static analysis and dependency hygiene: detekt, Konsist, dependency-analysis, Renovate | accepted | 2026-11-15 |
| [0015](0015-own-bottom-sheet.md) | Our own bottom sheet on AnchoredDraggable, not Material's ModalBottomSheet | accepted | 2026-11-16 |
| [0016](0016-seed-catalogue-hosting.md) | Seed catalogue: bundled JSON, photos hosted on the docs site | accepted | 2026-11-17 |
| [0017](0017-maps-maplibre-openfreemap.md) | Maps: MapLibre Compose with OpenFreeMap tiles, our own pins | accepted | 2026-11-18 |
| [0018](0018-demo-wallet-payment.md) | Payment: a labelled demo wallet, no real wallet brands | accepted | 2026-11-19 |

## How these were decided

Three rounds of question-and-answer on 2026-11-15, each presenting options with their differences, a recommendation, and the benefits and negatives of each. Round 1 fixed platform, scope, data and the role of AI; round 2 the architecture internals; round 3 the engineering layer. The ADRs record the options as presented and the reasons for each choice so the reasoning survives the conversation.
