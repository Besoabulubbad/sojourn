# core:navigation

`SojournNavKey` sealed hierarchy (the only `NavKey`s in the codebase), `Tab`, `Navigator`, `sojournNavSerializersModule` (explicit polymorphic registration — required on iOS), scene strategies (sheet, dialog), transition specs from `SojournMotion` (ADR 0005).

**May depend on:** core:designsystem (motion tokens), core:model, core:common, Navigation 3.

**Status:** skeleton (Phase 1). Contract: [docs/architecture/modules.md](../../docs/architecture/modules.md).
