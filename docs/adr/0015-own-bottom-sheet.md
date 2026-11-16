# 0015 — Our own bottom sheet on AnchoredDraggable, not Material's ModalBottomSheet

- **Status:** accepted
- **Date:** 2026-11-16
- **Deciders:** project owner, with Claude Code
- **Tags:** design-system, navigation, testing

## Context and problem statement

The Sheet card asks for three fixed stops (peek 96 dp, half, full), a title and close button on every sheet, `scrim` behind modal sheets, system back closing the sheet first, and a footer pinned to the bottom that owns the navigation and keyboard insets. Filters, price details and pickers are modal; the map's results sheet is not and rests at peek. Material 3's `ModalBottomSheet`, the default choice, has two stops (partially expanded and expanded) and opens in its own window.

## Decision drivers

- The card's three detents, including peek for the map.
- Screenshot tests of the whole sheet on Android and iOS (I-04: floating and docked controls are tested at 320 dp and 200 % text).
- Material stays inside `core:designsystem` and out of its public API.
- Back behaves the same on both platforms (Android back and predictive back, iOS edge swipe).

## Options considered

1. **Own sheet** on Compose foundation's `AnchoredDraggable`, drawn in the screen's own layer, back through `navigationevent`.
2. **Wrap `ModalBottomSheet`** and style its contents.

## Decision outcome

**Chosen: our own sheet**, because it is the only option that gives the card's three stops and lets the screenshot tests capture the sheet itself rather than its contents. `AnchoredDraggable` is stable foundation API on every target, so the gestures, fling and anchor logic are not ours to maintain; we own about two hundred lines of layout, scrim, nested scrolling and semantics.

### Consequences

- Good: peek, half and full exactly as specified; one component for modal and non-modal sheets.
- Good: `sheet_filters` and `sheet_peek` are rendered in the full screenshot matrix on Android and iOS.
- Good: no Material type in the API; `SojournSheetState` and `SojournSheetDetent` are ours.
- Bad: the sheet draws in the caller's layer, so it must be the last child of a full-screen box. The app's Navigation 3 overlay scene for sheet destinations does this once for every sheet.
- Bad: we own the accessibility details Material would give us. The handle exposes expand and collapse to screen readers, the close button is always present, and a Robolectric test (`SojournSheetBehaviourTest`) covers both paths.
- Neutral: `LocalNavigationEventDispatcherOwner` is absent in previews and screenshot hosts, so the back handler is installed only when a dispatcher exists.

### Confirmation

Konsist requires a screenshot case for `SojournSheet`; `SojournSheetBehaviourTest` checks dismissal and the handle's action; detekt's `InsetsOnlyInOwners` keeps inset handling inside `SojournSheet.kt`.

## Pros and cons of the options

### Own sheet

- Good: every requirement of the card; full-sheet screenshots; Material-free API.
- Bad: more code to own and review.

### Wrap `ModalBottomSheet`

- Good: less code; Material's gestures and semantics.
- Bad: no peek stop, so the map would need a second component.
- Bad: a separate window, so screenshots cover only the contents.
