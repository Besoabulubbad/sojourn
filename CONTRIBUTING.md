# Contributing

This repository is run like a product team's: small reviewed changes, enforced rules, written decisions. The process is deliberately light; the gates are deliberately strict.

## Branching and merging

- **Trunk-based.** `main` is always releasable and protected: no direct pushes, PRs only, required checks green, conversations resolved, linear history. One approval is required as soon as there is a second maintainer; a PR author cannot approve their own PR, so a solo repository relies on the required checks and the automated review, and every merge is still a deliberate human action.
- Branches are short-lived and named `type/scope-short-description`, e.g. `feat/details-gallery-states`, `fix/localization-arabic-plurals`, `docs/adr-0015-analytics`.
- Squash-merge. The squash commit message is the PR title and must be a valid Conventional Commit (see below) — release notes are generated from it.
- Rebase on `main` before requesting review; no merge commits in branches.

## Commits — Conventional Commits

```
<type>(<scope>): <summary in imperative, lower case, no period>

[optional body: what and why, not how]
[optional footer: BREAKING CHANGE:, Refs #123]
```

| type | use for |
|---|---|
| `feat` | user-visible behaviour or a new component/screen |
| `fix` | a bug fix |
| `refactor` | code change with no behaviour change |
| `test` | tests only |
| `docs` | documentation only (ADRs included) |
| `build` | Gradle, convention plugins, dependencies |
| `ci` | workflows |
| `chore` | anything else that is not code (tooling config, renames) |
| `perf` | performance |

Scope is the module name without its prefix: `explore`, `designsystem`, `navigation`, `build-logic`, `docs`. Breaking changes carry `!` after the scope and a `BREAKING CHANGE:` footer.

## Pull requests

Fill in the PR template. A PR is ready for review when:

1. `./gradlew check` passes locally (same as CI).
2. The description says **what** changed and **why**, and links the issue, ADR or design-system card it implements.
3. Screenshots or a recording are attached for any visual change, in light **and** dark, English **and** Arabic.
4. New or changed screenshot baselines are listed explicitly, with the reason.
5. Documentation duties in `CLAUDE.md` are done (ADR, module doc, mapping row, KDoc).

Reviewers check against `docs/architecture/overview.md` and the relevant ADRs first, code second. The automated Claude review posts a first pass within minutes; treat it as a colleague's comments — address or answer each one.

## Definition of Done

- A **screen**: see the ten-point checklist in [`docs/guides/adding-a-feature.md`](docs/guides/adding-a-feature.md) (it mirrors the design system's engineering standards).
- A **component**: see [`docs/guides/adding-a-component.md`](docs/guides/adding-a-component.md).
- A **library or architectural change**: an accepted ADR.

## Architecture Decision Records

Decisions live in `docs/adr/` in [MADR](https://adr.github.io/madr/) format. To propose one:

1. Copy `docs/adr/0000-template.md` to `docs/adr/NNNN-short-title.md` with the next number.
2. Status `proposed`; fill in context, options considered (with real trade-offs), decision, consequences.
3. Open a PR with `docs(adr): propose NNNN ...`. Discussion happens on the PR.
4. On merge the status becomes `accepted`. Superseding an ADR means a new ADR that names the old one; the old one's status becomes `superseded by NNNN`. ADRs are never edited after acceptance except for that status line.

## Running checks locally

```bash
./gradlew check                 # the full PR gate
./gradlew detekt                # style and Compose rules
./gradlew :tooling:konsist:test # architecture rules
./gradlew verifyRoborazziDebug  # screenshots; recordRoborazziDebug to re-baseline
./gradlew buildHealth           # dependency analysis
```

Setup requirements are in [`docs/guides/local-setup.md`](docs/guides/local-setup.md).

## Reporting issues

Use the issue templates. For a visual bug include device, OS, app language, theme and font scale — the matrix matters here.
