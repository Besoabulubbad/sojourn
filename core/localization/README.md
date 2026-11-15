# core:localization

`AppLocale`, `LocalAppLocale`, `AppLocaleController`, `SojournFormatter` (money, dates, ranges, counts, bidi isolates), string and plural resources per locale, the plural-completeness check. The **only** module allowed to touch platform locale APIs (ADR 0009).

**May depend on:** core:model, core:common, Compose resources, multiplatform-settings.

**Status:** skeleton (Phase 1). Contract: [docs/architecture/modules.md](../../docs/architecture/modules.md).
