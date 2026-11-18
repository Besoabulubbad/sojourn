package com.abulubad.sojourn.core.model

import kotlinx.datetime.YearMonth

/** Who is staying. At least one adult; children count toward "sleeps". */
data class Guests(val adults: Int = 1, val children: Int = 0) {
    init {
        require(adults >= 1) { "At least one adult" }
        require(children >= 0) { "Children cannot be negative" }
    }

    val total: Int get() = adults + children
}

/**
 * The Where / When / Who of a search. Every field is optional until the guest chooses it; a query without dates
 * yields "from" prices (P-03), a query with dates yields all-in totals (P-02).
 *
 * A flexible search has a [length] and, optionally, a [startMonth]: "1 month, starting in March". With both, only
 * stays with a free run of that length starting in that month match. Neither applies once there are [dates].
 */
data class SearchQuery(
    val cityId: CityId? = null,
    val dates: DateRange? = null,
    val guests: Guests = Guests(),
    val length: StayLength? = null,
    val startMonth: YearMonth? = null,
) {
    val hasDates: Boolean get() = dates != null
}

/** Sort orders for results, the four the ScreenResults card offers. Localized by id (S-05). */
enum class SortOrder(val id: String) {
    Recommended("recommended"),
    PriceLowToHigh("price_low_to_high"),
    Rating("rating"),

    /** From the city's centre to the centre of the stay's area. */
    DistanceToCentre("distance_to_centre"),
}

/**
 * The filter sheet's Bedrooms control (ScreenFilters card): an exact count, or three and more. A studio is "Studio",
 * never "0 bedrooms" (N-01). Localized by id (S-05).
 */
enum class BedroomChoice(val id: String) {
    Studio("studio"),
    One("one"),
    Two("two"),
    ThreeOrMore("three_or_more"),
    ;

    fun matches(bedrooms: Int): Boolean = when (this) {
        Studio -> bedrooms == 0
        One -> bedrooms == 1
        Two -> bedrooms == 2
        ThreeOrMore -> bedrooms >= THREE
    }

    private companion object {
        const val THREE = 3
    }
}

/**
 * The filter sheet's state. [basis] is required with a [priceRange] so the slider filters on the same basis as the
 * cards on screen (P-04). [minBeds] and [minBathrooms] are null for "any": every stay has at least one of each.
 */
data class Filters(
    val priceRange: MoneyRange? = null,
    val basis: PriceBasis = PriceBasis.Month,
    val amenities: Set<Amenity> = emptySet(),
    val bedrooms: BedroomChoice? = null,
    val minBeds: Int? = null,
    val minBathrooms: Int? = null,
    val instantBook: Boolean = false,
    val freeCancellation: Boolean = false,
    val sort: SortOrder = SortOrder.Recommended,
) {
    init {
        require(minBeds == null || minBeds > 1) { "One bed is any stay: use null" }
        require(minBathrooms == null || minBathrooms > 1) { "One bathroom is any stay: use null" }
    }

    /**
     * How many filters are active, for the filter count badge. Each amenity counts on its own, so a quick-filter
     * chip always moves the badge by one. Sort is an order, not a filter.
     */
    val activeCount: Int
        get() = amenities.size +
            listOf(priceRange, bedrooms, minBeds, minBathrooms).count { it != null } +
            listOf(instantBook, freeCancellation).count { it }

    companion object {
        val None = Filters()
    }
}
