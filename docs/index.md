# Sojourn — documentation

Everything about how this app is designed, built, tested and shipped lives here, next to the code it describes. Start with the page that matches your question.

| You want to… | Read |
|---|---|
| Understand what we are building and in what order | [Plan](plan.md) |
| Understand how the code is organised and why | [Architecture overview](architecture/overview.md), then [Modules](architecture/modules.md) |
| Know why a technology was chosen (or rejected) | [Decision records](adr/README.md) |
| See which library versions are in use and where they were verified | [Tech stack](tech-stack.md) |
| Map a design-system card to its Kotlin API | [Design system → code](design-system/mapping.md) |
| Add a screen, a component, a test, a translation, or cut a release | [Guides](guides/local-setup.md) |
| Work on this repo with an AI agent | [`CLAUDE.md`](https://github.com/Besoabulubbad/sojourn/blob/main/CLAUDE.md) at the repository root |

## Principles behind the documentation

1. **Decisions are written down before they are implemented.** Every library and pattern has an ADR with the options that were considered and their trade-offs. Code that contradicts an accepted ADR is a bug.
2. **Rules are enforced, not remembered.** Where a document says "never", there is a Konsist rule, a detekt rule, a type or a test behind it. The [architecture overview](architecture/overview.md#enforcement) lists each one.
3. **Official documentation wins.** Library usage follows the vendor's documentation for the pinned version. The [tech stack](tech-stack.md) links to each.
4. **Docs ship with the change.** A PR that changes behaviour, a module boundary or a public API updates the matching page in the same PR. The automated review checks for it.

## Source of truth for the product

The design system — tokens, 26 components, 11 screens, motion, engineering standards and the design rationale behind them — is authored separately and consumed here. `design/tokens.json` is a copy of its token file (only the name field changed) and is the input to generated Kotlin; the mapping page explains how every card becomes code.
