# 0011 — CI/CD on GitHub Actions with Conventional Commits and release-please

- **Status:** accepted
- **Date:** 2026-11-15
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** delivery, process

## Context and problem statement

The project wants a complete, modern application lifecycle: every PR verified, `main` always releasable, releases cut by the pipeline with generated notes, documentation published automatically, iOS compiled continuously. We need the workflow set, the branching/commit conventions it depends on, and the cost profile.

## Decision drivers

- `main` protected and always green.
- Fast PR feedback; expensive jobs where they pay off.
- Releases reproducible from a tag with no manual steps.
- Costs: macOS minutes are ~10× Linux.

## Options considered

1. **PR checks only** — build, tests, lint; no release automation, no iOS runner.
2. **Full pipeline** — PR, main (incl. iOS), release, docs; Conventional Commits; release-please.
3. **Full + distribution** — plus Firebase App Distribution / TestFlight uploads on merge.

## Decision outcome

**Chosen: Option 2, the full pipeline.** Distribution (Option 3) is deferred until store/Firebase accounts and signing certificates exist; the release workflow is designed so adding an upload step is one job.

Workflows (`.github/workflows/`):

| Workflow | Trigger | Jobs |
|---|---|---|
| `pr.yml` | `pull_request` | ubuntu: detekt → Konsist → unit tests → `verifyRoborazziDebug` → `buildHealth` → `:app:android:assembleDebug`; JUnit report comment; screenshot diffs uploaded on failure. Path filters skip Gradle jobs for docs-only PRs. |
| `main.yml` | `push` to `main`, nightly schedule | everything in `pr.yml` + macOS: `:app:shared:linkDebugFrameworkIosSimulatorArm64` and `xcodebuild` of the simulator app; emulator job: Compose UI tests and Maestro flows. |
| `release.yml` | tag `v*` | signed AAB/APK (secrets), iOS archive (unsigned until certs), GitHub Release with artefacts. |
| `release-please.yml` | `push` to `main` | maintains the release PR (version bump, CHANGELOG) from Conventional Commits; merging it creates the tag. |
| `docs.yml` | `push` to `main` (docs paths), `pull_request` (build only) | MkDocs Material strict build → GitHub Pages; Dokka into `/api` from Phase 7. |
| `claude-review.yml` | `pull_request` | automated review (ADR 0013). |

Conventions: trunk-based, squash merges, PR title = Conventional Commit, branch protection with required checks, conversation resolution and linear history (one approval once a second maintainer exists — an author cannot approve their own PR), `concurrency` groups cancel superseded runs, `gradle/actions/setup-gradle@v6` for caching, Gradle configuration cache on.

Secrets (added when available): `ANTHROPIC_API_KEY`, `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`; iOS signing later.

### Consequences

- Good: the whole lifecycle is visible in the repo; a reviewer can read the release process.
- Good: release notes and versions are a by-product of commit discipline.
- Good: iOS is compiled on every merge even without local Xcode.
- Bad: macOS minutes cost. Mitigated: iOS job only on `main`/nightly, not on PRs; path filters.
- Bad: Conventional Commits require discipline. Mitigated by the PR template, squash-merge, and a PR-title check.
- Neutral: The repository is public from day one, so Actions minutes and GitHub Pages are free.

### Confirmation

- Branch protection requires `pr.yml` jobs and the Claude review to complete.
- `release.yml` runs only from tags created by release-please.

## Pros and cons of the options

### Option 1 — PR checks only

- Good: cheapest and simplest.
- Bad: no release or lifecycle story; iOS untested.

### Option 2 — Full pipeline

- Good: complete, reproducible, demonstrable.
- Bad: more YAML to maintain; macOS cost.

### Option 3 — Full + distribution

- Good: stakeholders get builds on their phones automatically.
- Bad: needs accounts, certificates and secrets up front; adds moving parts before there is an app to distribute.

## Implementation notes

**2026-11-16.** `pr.yml` has a second job, **iOS tests and screenshots (macOS)** on `macos-latest`, in parallel with the Linux job: `./gradlew iosSimulatorArm64Test` runs every common and iOS unit test on the simulator plus the iOS screenshot verify, with a JUnit report and diff images on failure. It is a required check, decided with the maintainer, so an iOS regression cannot merge. macOS minutes are free for public repositories.

## Links

- [GitHub Actions — workflow syntax](https://docs.github.com/actions/using-workflows/workflow-syntax-for-github-actions)
- [gradle/actions](https://github.com/gradle/actions) · [release-please-action](https://github.com/googleapis/release-please-action) · [Conventional Commits](https://www.conventionalcommits.org/)
- Related: ADR 0010, ADR 0012, ADR 0013
