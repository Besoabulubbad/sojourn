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
 * The filter sheet's state. [basis] is required with a [priceRange] so the slider filters on the same basis as the
 * cards on screen (P-04).
 */
data class Filters(
    val priceRange: MoneyRange? = null,
    val basis: PriceBasis = PriceBasis.Month,
    val amenities: Set<Amenity> = emptySet(),
    val minBedrooms: Int? = null,
    val instantBook: Boolean = false,
    val sort: SortOrder = SortOrder.Recommended,
    val studioOnly: Boolean = false,
    val freeCancellation: Boolean = false,
) {
    /**
     * How many filters are active, for the "Show 14 stays" button and the filter count badge. Each amenity counts
     * on its own, so a quick-filter chip always moves the badge by one. Sort is an order, not a filter.
     */
    val activeCount: Int
        get() = amenities.size +
            listOf(priceRange != null, minBedrooms != null, instantBook, studioOnly, freeCancellation).count { it }

    companion object {
        val None = Filters()
    }
}
