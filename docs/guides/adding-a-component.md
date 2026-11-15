# Adding a design-system component

Components live in `core:designsystem` and map one-to-one to the design system's component cards. A component is done when a feature can use it without knowing Material exists.

## 0. Read the card

`project/api/components/<Comp>.md` and `project/components/<Comp>/README.md` in the design system (summarised in [the mapping](../design-system/mapping.md)). Note: states, sizes, the tokens it names, what it must *not* allow (e.g. a bare number as a price), motion, and RTL behaviour.

## 1. Design the API

- Name: `Sojourn<Comp>`. One primary composable; small enum/sealed types for variants (`SojournButtonVariant.Primary`).
- Encode the card's rules in the **type system** where possible:
  - a price → `PriceDisplay`, never `Double`
  - an icon-only control → `contentDescription: String` **required**
  - an icon → `SojournIcon`, never a raw painter
  - a policy → `Policy` (carries its number)
- `modifier: Modifier = Modifier` is the first optional parameter; content slots last. Follow the Compose rules detekt enforces (parameter order, naming).
- Everything visual comes from tokens: `SojournTheme.colors.*`, `SojournTheme.type.*`, `SojournSpacing`, `SojournShapes`, `SojournSizes`, `SojournElevation`, `SojournMotion`.
- Minimum touch target `SojournSizes.touchMin` built in (`Modifier.minimumInteractiveComponentSize()` or explicit size).
- States change more than colour (C-04): weight, icon, shape or text as well.

## 2. Files

```
core/designsystem/src/commonMain/kotlin/com/abulubad/sojourn/core/designsystem/components/<comp>/
  Sojourn<Comp>.kt            public composable(s) + variant types, with KDoc
  Sojourn<Comp>Defaults.kt    (optional) sizes/colours objects, like Material's *Defaults
  Sojourn<Comp>Previews.kt    @Preview per state, using SojournVariants (EN/AR × light/dark)
core/designsystem/src/androidHostTest/kotlin/.../components/<comp>/
  Sojourn<Comp>ScreenshotTest.kt
core/designsystem/README.md   add the component section (summary from the card, API, rules)
```

Material 3 may be used **inside** the implementation; nothing Material appears in the public signature.

## 3. Behaviour

- RTL: use start/end; mark directional icons via `SojournIcon.autoMirror`; test in Arabic previews.
- Motion: durations and easings from `SojournMotion`; springs from `SojournMotion.settleSpring`/`popSpring`; honour `LocalReduceMotion`.
- Haptics (if the card says so): via `core:platform` `Haptics`, injected or passed in, never called from the composable directly.
- Accessibility: semantics roles, `contentDescription`, `stateDescription` for selected/expanded; focus ring 2dp `SojournTheme.colors.focus`.
- Text: never truncate unless the card allows it; then at word boundary with ellipsis.

## 4. Previews and screenshots

- One `@Preview` per visual state; each preview wraps content in `SojournPreview(variant)` which applies theme + locale + direction.
- The screenshot test iterates the matrix (2 languages × 2 themes × font 100/200 × width 320/412) over the same states. Record, inspect every image, commit baselines.
- Long-string previews for Arabic and 200% to prove the component grows rather than clips (S-08, I-04).

## 5. Tests beyond screenshots

Logic the card specifies gets a unit test: `SojournRating` hides under 3 reviews (N-04); `SojournPriceBreakdown` rejects lines that do not sum (P-05); `SojournDateRangePicker` first day of week follows `AppLocale` (L-04).

## 6. Documentation

- KDoc on every public symbol (Dokka publishes it): what it is, the card it implements, the rules it enforces, a usage snippet.
- Row in [design-system/mapping.md](../design-system/mapping.md) "Components → composables".
- Section in `core/designsystem/README.md`.

## 7. Checklist

- [ ] Public API uses tokens and domain types only; no Material in signatures
- [ ] 48 dp target; states distinguishable without colour
- [ ] Previews for every state in all four variants
- [ ] Screenshot baselines recorded, reviewed, committed
- [ ] RTL verified; directional icons mirror correctly
- [ ] Motion from tokens; reduce-motion respected
- [ ] Semantics complete; required `contentDescription`s
- [ ] KDoc, README section, mapping row

## Commit

`feat(designsystem): add Sojourn<Comp>` — one component per PR where practical.
