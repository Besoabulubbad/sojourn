# Engineering standards

!!! note "Provenance"
    Adapted from the original design system (`project/docs/1-standards.md`, 2026-11-15). Rebranded and genericized for this repository; the rules are unchanged.

These rules exist so that the problems common in rental apps cannot be written, merged or released. Each rule names how it is enforced. A rule without enforcement is a wish, so every one maps to the type system, a lint rule, a test or the release checklist.

## Module shape

```
shared/
  designsystem/      tokens, theme, components, icons, fonts   (commonMain)
  localization/      AppLocale, formatters, string and plural resources
  feature-explore/   screens: import designsystem + localization only
  feature-stay/
  feature-booking/
androidApp/          edge-to-edge setup, per-app language, Google Pay
iosApp/              safe-area hosting, per-app language, Apple Pay
```

- Tokens are generated into Kotlin from this system's `tokens.json` (`SojournColors`, `SojournType`, `SojournSpacing`, `SojournShapes`, `SojournMotion`). Nobody edits the generated files.
- `SojournTheme { }` wraps the app and provides colours for the active theme, type, shapes and the locale environment.
- Feature modules cannot depend on Material or platform UI directly. The dependency is declared `implementation` inside `designsystem` only.

## Locale and direction

| # | Rule | Enforced by |
|---|---|---|
| L-01 | One `AppLocale` is the single source for language. Strings, layout direction, number, date and plural formatting all read it. Nothing reads the device locale directly. | Lint: forbid platform locale APIs outside `localization`. |
| L-02 | Layout uses start and end only. No left, right, or absolute offsets, and no manual `scaleX(-1)`. | Lint: forbid `left`/`right` padding, alignment and `absoluteOffset` in feature code. |
| L-03 | Changing the in-app language switches text and direction together, on both platforms, whatever the device language is. | Instrumented test over the four device and app language pairs: EN/EN, EN/AR, AR/EN, AR/AR. |
| L-04 | First day of week, month names and digits come from `AppLocale`. | Unit tests on the formatter with `en-GB` and `ar`. |
| L-05 | Directional icons are marked auto-mirrored in `SojournIcon`. Call sites cannot flip icons. | `SojournIcon` is a sealed set; raw vectors are not accepted by components. |
| L-06 | Language choices are shown as names in their own script. No flags. | Design review; no flag assets in the repo. |

## Strings

| # | Rule | Enforced by |
|---|---|---|
| S-01 | No user-facing string literals in Kotlin or Swift. All text comes from resources. | Lint: string literal passed to a text parameter fails. |
| S-02 | No string concatenation or interpolation for UI text. Use a resource with placeholders. | Lint: `+` or `$` templates feeding a text parameter fail. |
| S-03 | Every count uses a plural resource. Arabic provides all six CLDR forms: zero, one, two, few, many, other. | CI check: any plural key missing a form for `ar` fails the build. |
| S-04 | Inserted values with their own direction (prices, counters like "1 / 13", "3+", references, host text) are wrapped in bidi isolates by the formatter. | Formatter unit tests assert the isolate characters; screenshot tests in Arabic. |
| S-05 | Catalog data is localized by id: cities, countries, amenities, policies, filter labels. The API sends ids, the app owns the names. | Contract test: catalog endpoints return ids; a missing translation key fails CI. |
| S-06 | Host-written text (titles, descriptions) is shown in its own language and direction, with a translate action. It is never forced to the UI direction. | `HostText` component is the only way to render it. |
| S-07 | One term per concept. The glossary in the brand book is the source. | String review per language before each release; duplicate-meaning keys flagged by a glossary script. |
| S-08 | Text truncates at a word or sentence boundary with an ellipsis, and only where the component allows it. | Component-level; screenshot tests with long strings. |

## Price

| # | Rule | Enforced by |
|---|---|---|
| P-01 | A price is rendered only by `SojournPrice`, from a `PriceDisplay`. No component accepts a bare number as a price. | Type system. |
| P-02 | With dates, every surface shows the same all-in total: card, pin, booking bar, checkout, pay button. | The quote id travels with the stay through navigation; UI test compares the five surfaces for one fixture. |
| P-03 | With no dates, prices read "from … a month" and state what is excluded. | `PriceDisplay.From` requires the `excludes` list. |
| P-04 | Filters use the same basis as the cards on screen. | `SojournPriceRange` requires `basis`; view-model test. |
| P-05 | The breakdown lines sum to the total. | Unit test on every quote fixture; server contract test. |
| P-06 | Money is formatted by the shared formatter only. | Lint: forbid currency symbols in resources and code outside `localization`. |

## Layout and insets

| # | Rule | Enforced by |
|---|---|---|
| I-01 | The app is edge to edge. System insets are consumed only by `SojournTopBar`, `SojournBottomNav`, `SojournBookingBar`, `SojournActionBar`, `SojournSheet` and `SojournScaffold`. | Lint: forbid inset modifiers in feature code. |
| I-02 | No text under a transparent status bar or top bar. Over photos, the status bar gets `scrim-top` and actions are floating buttons. | Screenshot tests of scrolled states on Details and Explore. |
| I-03 | Docked bars are opaque and full width. Scroll content gets bottom padding equal to the bar plus the inset. | `SojournScaffold` computes it; screens do not. |
| I-04 | Floating controls never cover text or map attribution. | Screenshot tests at the smallest supported width (320dp) and 200% font scale. |
| I-05 | Every data screen implements loading, empty, error and offline. | `ScreenState<T>` is a sealed type; `when` must be exhaustive. |

## Colour, contrast and touch

| # | Rule | Enforced by |
|---|---|---|
| C-01 | Colours, sizes, radii and type come from tokens. No raw `Color(…)`, hex, `dp` or `sp` literals in feature code. | Lint. |
| C-02 | Text pairs meet 4.5:1, large text and meaningful graphics 3:1, in both themes. | Token-level contrast test runs on every change to `tokens.json`. |
| C-03 | Every interactive element has a 48dp minimum target. | Built into components; accessibility scanner in UI tests. |
| C-04 | State is never colour alone: selected, error and disabled each change shape, weight, icon or text too. | Design review; component previews show each state. |
| C-05 | Every icon-only control has a localized content description. | Required parameter on `SojournIconButton`. |
| C-06 | Both themes are designed and tested. The map uses the theme's style. | Screenshot tests run light and dark. |

## Naming and data

| # | Rule | Enforced by |
|---|---|---|
| N-01 | A studio is "Studio". Zero is never shown as a bedroom count. | `StayFacts` formatter test. |
| N-02 | One icon per meaning, from `SojournIcon`. | Sealed icon set. |
| N-03 | Policies carry their numbers: cancellation date, deposit amount, check-in time and method. | Required fields in the API contract; contract test. |
| N-04 | Ratings are hidden under 3 reviews. | `SojournRating` logic and test. |

## Motion

| # | Rule | Enforced by |
|---|---|---|
| M-01 | Durations and easings come from `SojournMotion`. No literal milliseconds in feature code. | Lint. |
| M-02 | Nothing runs longer than `dur-slower` (500ms). Nothing blocks input while animating. | Review; UI tests tap during transitions. |
| M-03 | "Reduce motion" replaces movement with a 150ms fade. | `SojournMotion.reduced` flag read by every transition helper; test with the setting on. |
| M-04 | Directional motion mirrors in RTL. | Transition helpers take direction from layout direction. |

## Test matrix

Screenshot tests run for every screen and every component in:

- 2 languages × 2 themes
- font scale 100% and 200%
- widths 320dp and 412dp
- each `ScreenState`: content, loading, empty, error, offline

In this repository every state runs in the 2 languages × 2 themes, and each component's and screen's default state runs the full 16-cell matrix (ADR 0010).

A change that alters a screenshot needs a designer's approval on the pull request.

## Definition of done for a screen

A pull request that adds or changes a screen is mergeable when all of these are true:

1. Built only from design-system components and tokens. Lint is clean.
2. All strings are in resources for `en` and `ar`, with plural forms complete.
3. Screenshots exist for the full test matrix and have been reviewed.
4. Works in the four device and app language pairs.
5. Prices pass through `SojournPrice`; totals match across surfaces.
6. Loading, empty, error and offline states are implemented.
7. TalkBack and VoiceOver can reach and name every control, in a sensible order, in both languages.
8. No text under system bars; nothing covered by floating or docked controls.
9. Motion uses tokens and respects "reduce motion".
10. The system back gesture does the expected thing on both platforms.
