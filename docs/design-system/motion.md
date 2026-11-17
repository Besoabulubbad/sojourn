# Motion

!!! note "Provenance"
    Adapted from the original design system (`project/docs/2-motion.md`, 2026-11-15). Rebranded and genericized for this repository; the rules are unchanged.

Motion explains where things come from and confirms what just happened. It is quick, it never blocks a tap, and it follows the reading direction. The Motion card shows the six signature animations running.

## Principles

1. **Continuity over decoration.** An element that exists on both screens travels between them: the card photo becomes the Details gallery; the search bar becomes the search flow.
2. **Enter soft, leave fast.** Entering uses `ease-emphasized`. Leaving uses `ease-exit` at two thirds of the enter duration.
3. **Follow the finger.** Sheets, galleries and the back gesture track the drag 1:1 and settle with a spring. Scroll-linked effects are tied to scroll distance, never to a timer.
4. **Direction has meaning.** Forward moves toward the end edge, back toward the start edge. Both mirror in Arabic.
5. **One thing moves.** No more than one emphasized animation at a time. Lists do not stagger beyond the first 5 items, and only on first load.

## Tokens

| Token | Value | Use |
|---|---|---|
| `dur-instant` | 100ms | Press states, chip toggle |
| `dur-fast` | 150ms | Fades, pin select, count updates |
| `dur-base` | 250ms | Sheet snap, tab switch, price swap, step change |
| `dur-slow` | 350ms | Container transform, sheet enter |
| `dur-slower` | 500ms | Full-screen flow enter. The ceiling. |
| `ease-standard` | cubic-bezier(0.2, 0, 0, 1) | Moves that start and end on screen |
| `ease-emphasized` | cubic-bezier(0.05, 0.7, 0.1, 1) | Entrances |
| `ease-exit` | cubic-bezier(0.3, 0, 1, 1) | Exits |

Springs, for anything dragged or popped: stiffness 400 with damping ratio 0.8 for settling; damping ratio 0.6 for the save heart and pin pop.

## By interaction

| Interaction | Animation | Timing |
|---|---|---|
| Search bar to search flow | Container transform: the bar grows into the full screen; content fades in after 100ms | `dur-slower`, `ease-emphasized` |
| Step change in the flow | Outgoing step slides toward the start edge and fades; incoming slides in from the end edge. Progress bar fills. | `dur-base`, `ease-standard` |
| Date range pick | Start day fills; the range tint sweeps from start to end as the end day is tapped | `dur-fast` per row, capped at `dur-base` |
| Card to Details | Shared bounds on the photo; title and price fade in below | `dur-slow`, `ease-emphasized` |
| Details back to list | Reverse, following the back gesture on both platforms | two thirds of enter, `ease-exit` |
| Gallery swipe | Follows the finger; pages snap with a spring | spring |
| Top bar on Details | Fill and title opacity tied to scroll over 48dp | scroll-linked |
| List to map | Crossfade; the toggle label swaps | `dur-base` |
| Pin select | Scale 1 to 1.12, invert colours; compact card rises 24dp and fades in | `dur-fast` pin, `dur-base` card |
| Cluster tap | Camera zoom, pins fade in at their new positions | map camera 400ms |
| Sheet open | Rises from the bottom edge; scrim fades | `dur-slow` sheet, `dur-base` scrim |
| Sheet drag | 1:1 with finger; settles to peek, half or full; flings past 50% dismiss | spring |
| Filter count in button | Number crossfades, debounced 300ms | `dur-fast` |
| Save | Heart dips to 0.8, pops to 1.3, settles filled; light haptic | 250ms spring |
| Remove from Saved | Card collapses height and fades; snackbar slides up | `dur-base` |
| Price update | Old value slides up and out, new slides in from below | `dur-base`, `ease-standard` |
| Button press | Scale to 0.98 and darken 8% | `dur-instant` |
| Loading | Skeleton pulse, 1.4s loop; content crossfades in | `dur-fast` crossfade |
| Pull to refresh | Platform indicator, tinted `brand` | platform |
| Payment success | Check mark draws, then the confirmation rises | 400ms then `dur-slow` |

## Haptics

Light tick: save, stepper change, date selected, sheet reaching a detent. Success pattern: payment confirmed. Error pattern: payment failed. Nothing else vibrates.

## Reduce motion

When the system setting is on: container transforms, slides and pops become a 150ms fade; skeletons stop pulsing; scroll-linked effects remain because the guest drives them. Haptics are unaffected.

## Performance

Animations hold 60fps on a mid-range Android device, 120fps where the display supports it. Animate transforms and opacity; do not animate layout size inside lists. Images decode off the main thread and are sized to their slot.

## In code

| Spec | Where |
|---|---|
| Tokens, `enter`/`exit`/`move`, springs | `SojournMotion` and `theme/SojournMotionExtensions.kt` |
| Step change, price update | `SojournTransitions.stepForward()`, `stepBack()`, `priceSwap()` |
| Haptics | `rememberSojournHaptics()`: `tick()`, `success()`, `error()` |
| Save | `SojournStayCard` heart (dip 0.8, pop 1.3, settle over `base`) + tick |
| Pin select | `SojournPricePin` (scale 1.12 over `fast`) |
| Sheet open and drag | `SojournSheet` (`slow` enter, settle spring, tick at each stop) |
| Date range pick | `SojournDateRangePicker` (tint sweep `fast` per row, capped at `base`) + tick |
| Top bar on Details | `SojournTopBar` with `rememberTopBarSolidFraction` (scroll-linked) |
| Button press | `SojournButton` (scale 0.98, darken 8 % over `instant`) |
| Container transform, card to Details, list to map | with navigation in `app:shared` (Phase 5) |
