# Design rationale

Why the design system makes the choices it makes. Each item names a problem that is common in rental and reservation apps, the principle that answers it, and where this codebase enforces it. No product is singled out; these are the patterns anyone who has booked a stay will recognise.

## Across every screen

| Problem | Principle | Where it lives |
|---|---|---|
| Layout direction follows the device, not the in-app language | One `AppLocale` drives text **and** direction together; tested in the four device/app language pairs | L-01, L-03 · `core:localization` |
| Content slides under the status bar or behind floating controls | Bars own the insets; over photos the status bar gets a scrim and actions are floating buttons | I-01, I-02 · `SojournScaffold`, `SojournTopBar` |
| Arabic is half translated, host text forced into the UI direction | Catalogue text is localized by id; host-written text keeps its own language and direction with a translate action | S-05, S-06 · `HostText` |
| Numbers, prices and counters reverse in RTL | Localized templates; the formatter wraps inserted values in bidi isolates | S-02, S-04 · `SojournFormatter` |
| Directional icons are not mirrored | The icon carries its mirroring flag; call sites cannot flip icons | L-05 · `SojournIcon` |
| Plurals are fixed strings ("1 beds", "15 ليالي") | Plural resources with all six Arabic forms, checked in CI | S-03 · `verifyPlurals` |
| Weak contrast and small targets | Tokens built to 4.5:1 / 3:1 in both themes; every target 48 dp | C-02, C-03 · token contrast test |
| One concept, several words | One glossary per language; a second term for the same thing is a bug | S-07 · brand book glossary |
| Flags stand in for languages | Language names in their own script | L-06 · Profile |

## Home and search

| Problem | Principle | Where it lives |
|---|---|---|
| Dates are optional but their effect is unstated | Dates stay optional (people browse before committing); without dates, prices read "from … a month" and say what they exclude; filters use the same basis | P-03, P-04 · `PriceDisplay.From` |
| The longer-stay offer is invisible | Stay-length shortcuts on Explore, monthly "from" prices on cities, a long-stay badge and discount line | `SojournCityCard`, `SojournPriceBreakdown` |
| A search form fills the first screen | One search bar opening a guided Where / When / Who flow; personal modules only when there is something real to show | `SojournSearchBar`, `feature:search` |
| Mixed input patterns | Three steps of one flow | `feature:search` |
| Navigation priorities | Saved is a tab; rewards lives in Profile | `SojournBottomNav` |

## Results, map and filters

| Problem | Principle | Where it lives |
|---|---|---|
| Card price and checkout total do not match | One all-in total leads on every surface; the quote id travels with the stay through navigation | P-01, P-02 · `SojournPrice`, nav keys |
| Map pins carry no information | Price pins with seen/selected states, clustering, "Search this area" | `SojournPricePin`, `feature:map` |
| The list is slow to scan | A denser card: area, rating, facts, swipeable photos, save | `SojournStayCard` |
| Filters do not match the results | Price on the cards' basis with a labelled slider, live count, sort | `SojournPriceRange` |
| List/map switching hidden behind a drag handle | A visible toggle; dragging remains with three fixed stops | `SojournViewToggle`, `SojournSheet` |
| Map style clashes with the app theme | The map follows the theme through the `map-*` tokens | `feature:map` |

## Details and booking

| Problem | Principle | Where it lives |
|---|---|---|
| No price until dates are picked; a disabled bar | A "from" price and a working "Choose dates" action; the bar is never disabled | `SojournBookingBar` |
| Location comes last and says little | Area under the gallery; location above amenities with walking times; an area, not an exact pin, before booking | `feature:details` |
| A large floating booking bar | Slim, docked, opaque, owns its inset | I-03 |
| Gallery states missing | Placeholder, loaded, failed-with-retry, full-screen gallery | `SojournGallery` |
| Key facts unclear | Four counts with one icon each; "Studio" never "0 bedrooms" | N-01 · `SojournKeyFacts` |
| Policies without numbers | Cancellation date, deposit amount, check-in time and method are required fields | N-03 · `Policy` |
| Description hard to read | Aligned by its own language; truncated at a sentence end | S-06, S-08 |
| Thin trust signals | Rating and count under the title when data exists; hidden under 3 reviews; "what the price includes" always shown | N-04 · `SojournRating` |
| Amenities ranked for weekends | Ranked for longer stays: Wi-Fi, workspace, washer, kitchen, heating | `feature:details` |

## Not covered by the design system

Sign-up, login and verification; card entry and booking confirmation beyond the demo flow; a full-screen reviews list; screen-reader behaviour and large text beyond what screenshots can prove — these need device testing and are listed in the [plan](../plan.md) as out of scope or Phase 6 work.
