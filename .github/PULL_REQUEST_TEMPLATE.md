<!-- Title must be a Conventional Commit: type(scope): summary  — e.g. feat(details): add gallery failure state -->

## What

<!-- One or two sentences. What changed, for whom. -->

## Why

<!-- The card, finding, standard, issue or ADR this implements. Link it. -->

## How to verify

<!-- Commands, screens to open, states to check. -->

## Screenshots

<!-- Any visual change: light + dark, English + Arabic. Delete the section if not applicable. -->

| | Light | Dark |
|---|---|---|
| EN | | |
| AR | | |

## Screenshot baselines

- [ ] No baseline changed
- [ ] Baselines changed — listed here with the reason:

## Checklist

- [ ] `./gradlew check` passes locally
- [ ] Follows the relevant ADRs (`docs/adr/`) and `CLAUDE.md` rules — or this PR adds/updates an ADR
- [ ] Strings in resources for `en` and `ar`; plural forms complete (if UI text changed)
- [ ] Docs updated in this PR (module README · modules.md · mapping.md · KDoc · guides), or not applicable
- [ ] For a new screen: the Definition of Done in `docs/guides/adding-a-feature.md` is pasted below and complete
