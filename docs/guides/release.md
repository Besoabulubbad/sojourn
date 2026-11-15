# Release

Releases are produced by the pipeline from `main`; nobody builds a release on a laptop. The design is in [ADR 0011](../adr/0011-ci-cd-github-actions.md).

## Versioning

Semantic versioning, driven by Conventional Commits:

| Commit type | Bump |
|---|---|
| `fix`, `perf` | patch |
| `feat` | minor |
| `!` or `BREAKING CHANGE:` | major (pre-1.0: minor) |
| `docs`, `build`, `ci`, `chore`, `refactor`, `test` | none (listed in notes where relevant) |

`release-please` keeps a **release PR** open against `main` that bumps `versionName`/`versionCode` (Android), `MARKETING_VERSION`/`CURRENT_PROJECT_VERSION` (iOS) and `CHANGELOG.md`. Merging that PR creates the tag `vX.Y.Z` and the GitHub Release draft.

## Flow

1. Work merges to `main` via PRs with Conventional Commit titles.
2. `release-please.yml` updates the release PR after every merge.
3. When ready, a maintainer merges the release PR → tag `vX.Y.Z`.
4. `release.yml` runs on the tag:
   - Android: `:app:android:bundleRelease` and `assembleRelease`, signed from secrets; R8 enabled; mapping file attached.
   - iOS: `:app:shared:linkReleaseFrameworkIosArm64`, `xcodebuild archive` (unsigned until certificates exist; signed export when they do).
   - Attach artefacts and the changelog section to the GitHub Release; publish.
5. `docs.yml` publishes the docs site (with Dokka) for the new version.

## Secrets

| Secret | Used by |
|---|---|
| `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD` | `release.yml` Android signing |
| `ANTHROPIC_API_KEY` | `claude-review.yml` |
| (later) iOS certificate/profile, App Store Connect API key | `release.yml` iOS signing, TestFlight |

Set with `gh secret set NAME`. Never in the repo, never in logs.

## Pre-release checklist

- [ ] `main.yml` green, including iOS and emulator jobs
- [ ] Screenshot baselines reviewed for the changes since last release
- [ ] CHANGELOG in the release PR reads correctly for a human
- [ ] Manual pass on a real Android device and an iOS simulator/device: EN/AR × light/dark on Explore, Details, Checkout
- [ ] Accessibility spot check (TalkBack/VoiceOver on the booking path)
- [ ] Docs site builds strictly; mapping and modules pages reflect the code

## Rollback

Releases are immutable. To roll back, revert the offending commits on `main` (a `fix:` or `revert:` commit) and let release-please cut the next patch. Store rollbacks (when stores are involved) use the store's own mechanisms.

## Public repository

The repository is public from day one: GitHub Pages serves the docs site, Actions minutes are free, and every release is visible. Secrets live only in GitHub Actions secrets — never in the repo, PR descriptions or logs.
