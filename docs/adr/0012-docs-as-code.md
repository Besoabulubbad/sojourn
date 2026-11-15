# 0012 — Docs as code: MkDocs Material + Dokka on GitHub Pages

- **Status:** accepted
- **Date:** 2026-11-15
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** documentation, process

## Context and problem statement

The project commits to documenting everything and to following documentation when implementing. Documentation that lives away from the code goes stale; documentation that is only Markdown in a repo is hard for non-engineers to browse. We need a system that keeps docs in the repo, versioned with the code, and also publishes them well.

## Decision drivers

- Docs change in the same PR as the code they describe.
- Readable both in-repo (GitHub) and as a site.
- Decisions recorded in a standard format.
- Broken links or missing pages fail CI.

## Options considered

1. **Markdown in repo only** — ADRs, READMEs, guides; no site.
2. **Docs-as-code + site** — same Markdown, built with MkDocs Material, plus Dokka API docs, published to GitHub Pages on merge.
3. **Docs-as-code + diagrams-as-code** — Option 2 plus C4 (Structurizr) and an auto-generated module graph.

## Decision outcome

**Chosen: Option 2.** `docs/` is the single source: `index`, `plan`, `tech-stack`, `architecture/`, `design-system/`, `guides/`, `adr/` (MADR). `mkdocs.yml` builds it with Material (strict mode, Mermaid via SuperFences); `docs.yml` publishes to GitHub Pages on merge to `main` and build-checks on PRs. Dokka 2.3 documents `core:*` public APIs under `/api` from Phase 7. Root `README.md`, `CONTRIBUTING.md` and `CLAUDE.md` link into the site. Module `README.md`s hold volatile detail.

Mermaid covers the diagrams we need (module graph, flows). C4/Structurizr is deferred — it adds tooling whose value appears with many services, and this is one app.

### Consequences

- Good: one place, versioned, reviewed; strict builds catch broken links.
- Good: ADRs give every decision a permanent, linkable home.
- Good: Dokka turns KDoc into browsable API docs, which rewards writing it.
- Bad: a Python toolchain (MkDocs) alongside Gradle. Mitigated: only CI and optional local preview need it; `pipx install mkdocs-material` suffices.
- Good: the repository is public from day one, so GitHub Pages is free and the site is live from the first merge.
- Neutral: Design-system reference docs are copied into `docs/design-system/` with provenance notes; the design system remains the source of truth.
- Neutral: Material for MkDocs has announced end of life on 2027-05-05, with [Zensical](https://zensical.org) (same team, reads the existing `mkdocs.yml`) as its successor. Material stays supported until then; migrating is a tooling-only change scheduled before that date and does not touch the Markdown.

### Confirmation

- `docs.yml` runs `mkdocs build --strict` on every PR touching docs.
- `CLAUDE.md` documentation duties; the automated review checks that behaviour/API/module changes come with doc changes.

## Pros and cons of the options

### Option 1 — Markdown only

- Good: zero tooling.
- Bad: no navigation, search or API docs; less discoverable.

### Option 2 — Docs-as-code + site

- Good: polished, searchable, API docs, still plain Markdown.
- Bad: a small extra toolchain.

### Option 3 — + diagrams-as-code

- Good: rigorous architecture diagrams; generated module graph.
- Bad: two more tools for diagrams Mermaid already draws well enough at this scale.

## Links

- [MkDocs Material](https://squidfunk.github.io/mkdocs-material/) · [Dokka](https://kotlinlang.org/docs/dokka-introduction.html) · [MADR](https://adr.github.io/madr/)
- [GitHub Pages](https://docs.github.com/pages)
- Related: ADR 0011, ADR 0013
