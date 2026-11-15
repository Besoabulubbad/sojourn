# 0002 — Full-breadth scope, built foundations → design system → screens

- **Status:** accepted
- **Date:** 2026-11-15
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** scope, process

## Context and problem statement

The app is a public showcase meant to signal class and modern engineering. The design system defines eleven screens (Explore, SearchFlow, Results, Filters, Details, Checkout, Map, Saved, Bookings, Profile, States) and 26 components. A demo can win on depth (a flawless golden path) or breadth (every screen). Breadth risks spreading polish thin; depth risks looking like a prototype. We must pick, and then arrange the work so the choice does not undermine quality.

## Decision drivers

- A reviewer should be able to open any tab and find the same standard.
- The design rationale's lessons are only fully demonstrated across all screens.
- Finite effort: polish per screen must not degrade as screen count grows.

## Options considered

1. **Polished vertical slice** — Explore → Search → Results → Details → Checkout, others stubbed.
2. **Slice + hero extras** — the slice plus Map and long-stay pricing.
3. **Full breadth** — all eleven screens.

## Decision outcome

**Chosen: Option 3, full breadth — with a strict build order that makes it safe.** Screens are composition of finished parts, so polish is paid for once, in the foundations:

1. **Foundations**: build system, quality gates, `core:*` modules (Phases 1–2).
2. **Design system**: all 26 components with previews, READMEs and a full screenshot matrix (Phase 3).
3. **Data**: database, repositories, use cases with the pricing invariants proven (Phase 4).
4. **Screens**: one feature module per screen, each merged only when it meets the ten-point Definition of Done (Phase 5).

The "States" screen is realised as the `ScreenState` variants of Results and Details rather than a separate destination.

### Consequences

- Good: every screen inherits the same components, states, motion and localization; uniformity is structural.
- Good: the full set of findings (navigation priorities, map pins, filters basis, policies with numbers…) is demonstrable.
- Bad: more total work than a slice; the first visible screen arrives later. Mitigated by the component previews and screenshot matrix, which are themselves demonstrable artefacts from Phase 3, and by wiring `app:shared` at the start of Phase 5 so the app runs end to end from the first feature.
- Bad: temptation to ship a "good enough" tenth screen. Mitigated by the Definition of Done being a PR gate, not a guideline.

### Confirmation

- Each `feature:*` PR uses the Definition of Done checklist in [`guides/adding-a-feature.md`](../guides/adding-a-feature.md); the automated review flags unchecked items.
- The screenshot matrix exists for every screen state before the feature is merged (Roborazzi verify in `pr.yml`).

## Pros and cons of the options

### Option 1 — Vertical slice

- Good: highest craft per hour; clearest narrative.
- Bad: tabs that lead to stubs read as unfinished; many findings not shown.

### Option 2 — Slice + hero extras

- Good: shows the differentiators (map, long-stay) with manageable scope.
- Bad: still leaves Saved/Bookings/Profile stubbed — the "second visit" screens where quality is judged.

### Option 3 — Full breadth

- Good: complete product story; uniform quality if the build order is respected.
- Bad: most effort; needs discipline to keep the bar.

## Links

- Design system: `docs/design-system/rationale.md`, `docs/design-system/engineering-standards.md` (Definition of done for a screen)
- [Plan](../plan.md)
