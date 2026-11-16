# core:model

Domain types, pure Kotlin and immutable: identifiers, `Money`, `PriceDisplay` / `PriceBreakdown`, `Quote`, `Stay` (with `StayFacts`, `Photo`, `Amenity`, `Rating`, `Policy`, `HostText`), `City`, `SearchQuery` / `Filters` / `Guests`, `DateRange` / `StayLength`, `Booking`.

**May depend on:** kotlinx-datetime only. No framework imports, no formatting, no strings for the UI.

## Rules this module enforces by type

| Rule | Where |
|---|---|
| P-01 a price is only a `PriceDisplay` | `PriceDisplay` is the only price type a component accepts |
| P-02 the quote id travels with the total | `PriceDisplay.Total(quoteId, …)` |
| P-03 "from" prices state what they exclude | `PriceDisplay.From` requires a non-empty `excludes` list and a per-night/month basis |
| P-05 breakdown lines sum to the total | `PriceBreakdown` validates on construction; `PriceLine` validates `unit × quantity` |
| N-01 a studio is never "0 bedrooms" | `StayFacts.isStudio` |
| N-03 policies carry their numbers | `Policy` subtypes require the date, amount or time |
| S-05 catalogue text is localized by id | `CityId`, `areaKey`, `Amenity.id`, `PriceLineKind.id`, … are keys, not names |
| S-06 host text keeps its own language | `HostText(text, languageTag)` |

`Money` is integer minor units in one `Currency`; arithmetic across currencies throws. Symbols, grouping and placement are the formatter's job in `core:localization` (P-06).

**Tests:** `commonTest` covers every invariant above.
