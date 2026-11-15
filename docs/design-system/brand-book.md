# Brand book

!!! note "Provenance"
    Adapted from the original design system (`project/README.md`, 2026-11-15), minus its generated consumption index; rebranded and genericized for this repository with token values and rules unchanged.

This is the design system for the Sojourn guest app on Android and iOS, built as one Kotlin Multiplatform codebase with shared Compose UI. It defines the tokens, components, screens, motion and engineering standards. Read this page first, then the card for anything you use.

The palette, typefaces and screen designs are original to this system. The name is set in plain type; a wordmark and app icon drawn from the tokens are part of the showcase workstream. Listings, prices and cities in the previews are sample data.

## The product

Sojourn is a premium reservation app for furnished, move-in-ready homes, for stays from a few nights to a year — relocations, extended business travel, and anyone who wants to live somewhere new for a while. The app's job is to get a guest from "I need a place in London for a month" to a confirmed booking without a surprise in the price.

Three ideas shape every screen:

1. **One price, everywhere.** The number on the card is the number on the pin, the booking bar and the pay button.
2. **Length of stay is a first-class input.** A month is as easy to search as a weekend, and the longer-stay saving is visible before the first tap.
3. **Arabic and English are equals.** Layout, icons, numbers and plurals follow the in-app language, in both themes.

## How to use this system

- Build screens only from the components listed here. Feature code imports the `designsystem` module and nothing from Material or the platform UI kits directly.
- Use tokens by name. A raw colour, dp or sp value in feature code fails the build (see Engineering standards).
- Every screen ships in four variants: English light, English dark, Arabic light, Arabic dark. The Screens cards show the pairs.
- When a screen needs something that is missing, add it to the system first, with its guideline and its Arabic preview.

## Content

**Voice.** Plain, calm, specific. Say what will happen and what it costs. No exclamation marks, no emoji, no hype words.

- Sentence case everywhere: "Choose dates", "Good to know". Never title case or all caps.
- Address the guest as "you". The app does not say "we" except in support copy.
- Buttons are verbs with their object: "Show 14 stays", "Pay £1,383", "Choose dates".
- Prefer the number to the adjective: "8 min walk to Paddington station", not "great location".
- British English in `en`: "lift", "cosy" in our own copy. Host-written text is left as written.
- Arabic is Modern Standard Arabic, written for the screen, not translated word for word.

**Glossary.** One term per concept, per language. A second word for the same thing is a bug.

| Concept | English | Arabic |
|---|---|---|
| A listing | Stay | إقامة |
| Wishlist | Saved | المحفوظات |
| Reservations | Bookings | حجوزاتي |
| Filter sheet | Filters | التصفية |
| Internet | Wi‑Fi | واي فاي |
| Laundry | Washer | غسالة |
| Desk area | Workspace | مكتب عمل |
| Zero-bedroom home | Studio | استوديو |
| Elevator | Lift | مصعد |
| Deposit | Refundable deposit | تأمين مسترد |
| Primary booking action | Reserve | احجز |

**Numbers, dates, money.**

- All formatting goes through the shared formatter with the in-app locale. Never concatenate.
- Digits are Western (0 to 9) in both languages. Arabic uses the `ar` locale with Latin digits.
- Money: "£1,383" in English, "1,383 £" in Arabic, always inside a bidi isolate.
- Date ranges: "12–27 Mar" with an en dash; "12–27 مارس" in Arabic.
- Counts use plural resources. English has two forms. Arabic has six: "ليلة واحدة", "ليلتان", "3 ليالٍ", "11 ليلة", "100 ليلة". "1 beds" and "15 ليالي" cannot be written with this system.

## Colour

- Screens sit on `bg`. Cards, sheets and bars sit on `surface`. Recessed areas use `surface-sunken`.
- Text is `ink`, then `ink-muted` for secondary lines. `ink-subtle` is for non-essential text only.
- `brand` is for the primary action, the selected date range and progress. One primary button per screen.
- `accent` (saffron) marks the long-stay offer: the "Long-stay price" badge and nothing else on a card. It is never a button.
- `save` is the heart. `danger` is for errors. Discounts use `brand-ink` with a minus sign, never green against red.
- Dividers use `hairline`. Anything a guest can tap or type into is outlined with `border-control`.
- On filled colours use the matching `on-` token (`on-brand`, `on-accent`, `on-danger`). Never literal white or black.
- Every text pair named in a token's usage note meets 4.5:1 in both themes; control borders, icons and the focus ring meet 3:1. Do not create new pairs.
- The focus ring is 2dp solid `focus` with a 2dp offset.

**Light and dark.** Dark is a full theme, not an inversion. `brand` lightens to a mint and takes dark text. Shadows deepen; surfaces separate by tone first. The app follows the system setting by default and offers System, Light and Dark in Profile. The map follows the app theme through the `map-` tokens.

## Type

Two families, both covering Latin and Arabic so one stack serves both languages:

- **Alexandria** for display and titles: `display`, `title-lg`, `title-md`, `title-sm`, `price`.
- **Readex Pro** for everything else: `body-lg`, `body`, `body-sm`, `label`, `label-sm`, `caption`.

Rules: `label-sm` and `caption` (12) are the smallest sizes. Line heights are set for Arabic as well as Latin, so do not tighten them. Never set Arabic in italics or with letter spacing. Numbers use tabular figures. Text scales with the system font size up to 200% without clipping; rows grow, they do not truncate. Bundle both families as font resources in the shared module.

## Layout

- Side margin `space-4`. Gap between sections `space-6`. Inside cards `space-3`.
- Every tap target is at least `touch-min` (48dp) on both platforms, including text links.
- Use start and end, never left and right. The whole layout mirrors in Arabic. Exceptions that never mirror: the map, photos, phone numbers, booking references, the clock.
- The app draws edge to edge. `TopBar`, `BottomNav`, `BookingBar` and `Sheet` own the system insets; screens do not pad for them. No text may sit under the status bar or behind a floating control.
- Corners: `radius-lg` for photos and cards, `radius-md` for buttons and fields, `radius-full` for chips, pins and the search bar, `radius-xl` for sheet tops.
- Elevation is rare: `shadow-2` for things floating over photos and maps, `shadow-3` for sheets. Cards in lists have no shadow.

## Imagery and maps

- Photography carries the product. Photos are 16:10 in lists, full-bleed on Details, always with `radius-lg` in lists.
- Every image slot has a placeholder, a loaded state and a failed state with retry.
- The drawn rooms and skylines in the previews are stand-ins. They use the `photo-` tokens and never ship.
- The map is styled from the `map-` tokens with reduced labels. Before booking, a stay's position is shown as an area, with the neighbourhood name and walking times beside it.

## Icons

Lucide, 24dp, 1.75 stroke. See the Icons card. Back and chevrons mirror in Arabic; nothing else does. An icon alone always has a content description.

## Platform differences

The design is shared; these follow each platform:

| | Android | iOS |
|---|---|---|
| Back | System back and predictive back gesture | Edge swipe back |
| Sheets | Drag handle plus close button | Same, with the system grabber feel |
| Haptics | Light tick on save and stepper | Light impact on save and stepper |
| Payment | Google Pay sheet first | Apple Pay sheet first |
| Share | Android share sheet | iOS share sheet |
| Date and number formats | From the in-app locale on both | From the in-app locale on both |

## Where things are

- **Screens**: the eleven `Screen…` cards, each with a guideline that lists structure, rules, states and motion.
- **Components**: grouped Actions, Inputs, Navigation, Listing, Booking, Feedback.
- **Engineering standards**: the rules that make the review's problems impossible to ship.
- **Motion**: durations, easings and the animation for each interaction.
- **Findings**: every item from the review, what was decided and where it is handled.


---

