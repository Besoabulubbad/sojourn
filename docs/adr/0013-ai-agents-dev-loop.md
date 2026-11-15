# 0013 — AI agents in the development loop (not in the product)

- **Status:** accepted
- **Date:** 2026-11-15
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** process, ai

## Context and problem statement

This repository is a public showcase of **AI-first mobile software engineering**. "AI" could mean AI features in the shipped app, AI as engineering leverage in the repository, or both — and within the second, there is a spectrum from "agents can read the rules" to "agents open pull requests on their own". The choice affects product scope, secrets, CI permissions, and how the codebase is written: an agent-operable repo is a different artefact from a human-only one. It also has to stand comparison with the best agent-ready repositories of 2026.

## Decision drivers

- Demonstrate current agent practice credibly, in detail, in a way a reviewer can verify in the repo.
- Keep the product story focused on the product's differentiators, not on AI.
- Safety: agents acting on the repo get the least privilege that does the job; a human approves every merge.
- "Follow the documentation": agents must implement against version-exact library docs, not memory.
- Repeatability: an agent or a new engineer works inside the architecture without being told the rules twice.

## Options considered

1. **Both** — dev-workflow automation plus one in-app AI feature (natural-language search / concierge).
2. **Dev-workflow automation only.**
3. **In-app AI feature only.**

Within dev-workflow automation, these integrations were weighed individually: agent contract file, MCP servers, automated PR review, docs & release automation, cross-tool `AGENTS.md` with repo skills/subagents/hooks, `@claude` on-demand (issue → PR), and a `docs/ai` section with specs and agent-ready issue templates.

## Decision outcome

**Chosen: Option 2 — dev-workflow only**, with the following integrations:

| Integration | Decision | How |
|---|---|---|
| **Agent contract** | adopt | `CLAUDE.md` at the root encodes modules, rules, patterns, commands, conventions and documentation duties; the guides in `docs/guides/` are written as playbooks an agent can execute. |
| **MCP servers** | adopt | `.mcp.json` (project-scoped, no secrets): **Context7** for version-exact library documentation — agents fetch the docs for the versions pinned in the catalog before using an API; **GitHub MCP** for issues, PRs and checks. Authentication is per-developer (`/mcp` in Claude Code, env vars); nothing committed. |
| **Automated PR review** | adopt | `claude-review.yml` with `anthropics/claude-code-action@v1` on `pull_request` (opened, synchronize, ready_for_review, reopened); the prompt reviews against `CLAUDE.md`, the architecture overview and accepted ADRs (layering, tokens, strings, states, tests, docs duties, showcase exports); `use_sticky_comment: true`; tools limited to inline comments and `gh pr comment/diff/view`; permissions `contents: read`, `pull-requests: write`, `id-token: write`. Required check on `main`. |
| **Docs & release automation** | adopt | release-please generates versions and `CHANGELOG.md` from Conventional Commits; a docs-sync check (from Phase 2) flags `core:*` public API changes without matching KDoc/README changes; the "new dependency" path in CONTRIBUTING asks for an ADR draft. |
| `AGENTS.md` + `.claude/` skills, subagents, hooks | **not now** | The cross-tool contract file and project skills are cheap, additive and the direction of the ecosystem; deferred to keep Phase 0 focused. Adding them is a small PR that references this ADR. |
| `@claude` on-demand (issue → PR) | **not now** | Mention-triggered implementation can push commits; we prefer humans (with Claude Code locally) to open PRs during the build. May be enabled later behind branch protection and required review. |
| `docs/ai` section, feature specs, agent-ready issue templates | **not now** | The ADRs, guides and design-system cards already serve as specs; revisit once features are landing. |
| AI in the shipped app | **no** | The product thesis is honest pricing and bilingual craft; an in-app AI feature would need a backend, keys in the client and a product rationale the design system does not have. |

Transparency: agent-authored commits carry a `Co-Authored-By` trailer; the README states what is automated and that a human merges every pull request.

### Consequences

- Good: every PR gets a consistent first review against the written rules within minutes.
- Good: implementation follows version-exact docs through Context7, which is the mechanism behind the "follow documentation" rule rather than a hope.
- Good: `CLAUDE.md` doubles as the most accurate short description of the repo for humans.
- Good: no secrets or model calls in the app; no new product risk.
- Bad: requires an `ANTHROPIC_API_KEY` secret and spends tokens per PR. Mitigated by sticky comments and path filters for docs-only PRs.
- Bad: automated review can be wrong. Mitigated: advisory only; every merge is a human decision (and requires a second maintainer's approval once one exists); reviewers answer or address each comment.
- Bad: without `AGENTS.md`, non-Claude agents do not pick up the contract automatically. Mitigated: `CLAUDE.md` is plain Markdown any agent can be pointed at; the deferred PR fixes it properly.
- Neutral: The action's `restrict_untrusted_prs` default stays on; forks get no secrets.

### Confirmation

- `claude-review.yml` is a required check on `main`.
- `.mcp.json` is committed and documented in `docs/guides/local-setup.md`.
- `CLAUDE.md` is reviewed whenever an ADR is added or superseded.

## Pros and cons of the options

### Option 1 — Both

- Good: shows product and process innovation.
- Bad: in-app AI needs a backend and keys, dilutes the product thesis, and adds a demo failure mode.

### Option 2 — Dev-workflow only

- Good: pure leverage, low risk, verifiable in the repo.
- Bad: no user-facing AI showcase.

### Option 3 — In-app only

- Good: strong product demo moment.
- Bad: conventional engineering story; backend dependency.

## Links

- [Claude Code GitHub Action](https://github.com/anthropics/claude-code-action) — `docs/solutions.md` "Automatic PR Code Review", `docs/usage.md`, `docs/security.md`
- [Claude Code — CLAUDE.md memory](https://docs.anthropic.com/en/docs/claude-code/memory) · [Claude Code — MCP](https://docs.anthropic.com/en/docs/claude-code/mcp)
- [Context7 MCP](https://github.com/upstash/context7) · [GitHub MCP server](https://github.com/github/github-mcp-server)
- [AGENTS.md](https://agents.md) — deferred cross-tool contract
- Related: ADR 0011, ADR 0012
