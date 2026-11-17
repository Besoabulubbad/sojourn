# 0016 — Seed catalogue: bundled JSON, photos hosted on the docs site

- **Status:** accepted
- **Date:** 2026-11-17
- **Deciders:** project owner, with Claude Code
- **Tags:** data, network, showcase

## Context and problem statement

Phase 4 seeds about 40 stays across six cities, each with 5–8 photos, policies with their numbers and quotes per stay length (ADR 0007). The app must work with no backend, its loading, error and offline states must be real and reproducible, and the repository must stay reviewable. Where the catalogue's data and photos live, and how many distinct photos there are, had to be decided before writing the data layer.

## Decision drivers

- No backend, yet image loading, caching and failure must behave like a real app.
- A small app download.
- Reproducible, reviewable seed data (S-06: host text in its own language; P-03, P-05, N-03 hold for every stay).
- One pricing logic, whichever `SojournApi` is in use.

## Options considered

1. **Photos hosted on the docs site** (GitHub Pages), loaded by Coil with its disk cache; catalogue JSON bundled in the app.
2. **Photos bundled in the app.**
3. **Hybrid:** thumbnails bundled, full-size photos hosted.

For the photo set: a shared pool of ~60 curated photos, a unique set per stay (~240), or a small pool (~25).

## Decision outcome

**Chosen: option 1 with a shared pool of about 60 photos.**

- `tooling/scripts/make_catalog.py` generates `catalog.json` from hand-written titles and descriptions (in each host's language) and a seeded generator for the rest. The same file is written to `core/network/src/commonMain/catalog/` and `docs/catalog/`.
- The `sojourn.catalog` plugin embeds the first copy as `BundledCatalog`, so `LocalSojournApi` needs no file I/O on any platform.
- Photos live in `docs/catalog/photos/` and are served at `https://besoabulubbad.github.io/sojourn/catalog/photos/`. The pool is 62 photos: 56 rooms across eight categories (living, bedroom, kitchen, bathroom, dining, workspace, balcony, exterior) and one view per city.
- **Sourcing and curation.** Every photo is CC0 or public domain, found through [Openverse](https://openverse.org)'s public API (mostly StockSnap and rawpixel) and Wikimedia Commons. Each was reviewed by hand against the brand's luxury standard: designer or hotel-grade interiors, upscale façades and city views, nothing dated, run-down or industrial. A first pool drawn mostly from Commons failed that review and was replaced. `tooling/catalog/photo-pool-sources.json` lists the chosen images by id; `tooling/scripts/fetch_photo_pool.py` re-checks each licence, downloads, resizes to 1024 px and writes `docs/catalog/CREDITS.md`, which credits every author.
- `KtorSojournApi` fetches `catalog/catalog.json` from the same site and prices quotes with the same `CatalogEngine` as `LocalSojournApi`.

### Consequences

- Good: the app stays small; placeholder, loaded and failed photo states happen for real; Coil's cache is exercised.
- Good: one engine prices quotes for both APIs, so totals match whichever is active (P-02).
- Good: the catalogue is a reviewable diff, regenerated deterministically.
- Bad: first launch with no network shows photo placeholders and "Photo did not load" with retry; the brand book designs for exactly this, and Coil's disk cache covers later launches.
- Bad: the photo URLs depend on the docs site. A move updates `LocalSojournApi.DEFAULT_PHOTO_BASE_URL` in one place.
- Neutral: stays share photos from the pool; each stay draws a different mix of rooms, so lists do not repeat at a glance.

- Bad: some CC0 interiors exist on several providers; a duplicate is caught by reviewing the pool's contact sheet before a pool change merges.

### Confirmation

`BundledCatalogTest` checks every stay maps to the model, has 5–8 distinct photos and prices a week, a month and three months with lines summing to the total. `KtorSojournApiTest` fetches through a mock engine.
