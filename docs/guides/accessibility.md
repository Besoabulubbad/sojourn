# Accessibility

What the app guarantees for TalkBack, VoiceOver, large text and reduced motion, how it is enforced, and how to check a screen on a device. The rules come from the engineering standards (C-03, the Definition of Done) and the design system's Layout and Type sections.

## The guarantees

- **Every control has a name.** Icons alone carry a content description; a row or card is one button read as its text (merged semantics).
- **Every tap target is at least 48 dp** (`touch-min`), including calendar days, sheet rows and text links.
- **Modal sheets are modal for screen readers too.** Our sheet draws in the page's own layer (ADR 0015), so it hides the page: while a modal `SojournSheet` is open, `SojournScaffold` removes its page from the accessibility tree (`SojournModalSheets`). Draw a modal sheet beside the scaffold, never inside its content.
- **Only what is on screen is read.** The map's card row hides the neighbours peeking in at the edges; the pager still scrolls to them.
- **Text grows to 200 %** without clipping: rows grow, values move under their labels (`TitleAndValue`), and the screenshot matrix covers 200 % at 320 dp.
- **Reduce motion** (Android "Remove animations", iOS "Reduce Motion") swaps movement for a short fade. The app reads it at launch and again each time it returns to the front.
- **Both languages.** Everything above holds in English and Arabic; Arabic layouts mirror, and codes and references stay left to right.

## How they are enforced

| Guarantee | Check |
|---|---|
| Names, merged rows | Components set them; screenshot review; the device audit below |
| 48 dp targets | `SojournSizes.touchMin` in components; the device audit below |
| Modal sheets hide the page | `SojournScaffoldModalTest` (fails if the page stays in the tree) |
| 200 % text | The screenshot matrix (`fullMatrix` cases at 200 % and 320 dp), reviewed on contact sheets |
| Reduce motion | `LocalReduceMotion` in the theme; motion code reads `SojournMotion` |

## Checking a screen on a device

`tooling/scripts/a11y_audit.py` checks one screen of the running Android app for unnamed controls and targets under 48 dp:

```bash
adb shell uiautomator dump /sdcard/u.xml && adb pull /sdcard/u.xml
python3 -I tooling/scripts/a11y_audit.py u.xml "$(adb shell wm density | awk '{print $3}')" explore 2424
```

The last argument is the screen height in pixels (`adb shell wm size`): targets cut off by the bottom edge are skipped. A chip cut off by a sheet's pinned footer still measures short; the report gives its position so it can be told apart.

For large text, set `adb shell settings put system font_scale 2.0`, relaunch, and look at each screen; set it back with `1.0`.

## The Phase 6 pass (2026-11-19)

On the Android emulator, every main screen and sheet was audited in English and Arabic at 100 % and 200 % text: Explore, the search flow, Results and its sort sheet, Filters, Details with its dates and price sheets, the map, Checkout and the demo wallet, Saved, Bookings with the check-in guide, a booking's page, and Profile with Rewards, Language, Appearance and Help. Fixed:

- Calendar days were 38 dp tall; the cell is now 48 dp, with the disc and range band unchanged.
- A modal sheet left the page beneath readable to screen readers (Back, Share, Save behind the scrim); see above.
- The sort sheet's rows were 46 dp; now 48 dp. The sheet moved beside Results' scaffold.
- The map's peeking cards were empty buttons to a screen reader; now hidden.
- Reduce motion was read only at launch.

Not checked by hand: a full TalkBack and VoiceOver walk-through (TalkBack cannot be driven reliably from adb). The tree the audit reads is the one TalkBack reads; a manual walk-through is on the release checklist.
