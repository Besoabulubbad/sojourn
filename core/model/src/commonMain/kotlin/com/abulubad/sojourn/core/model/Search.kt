package com.abulubad.sojourn.core.model

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
 */
data class SearchQuery(
    val cityId: CityId? = null,
    val dates: DateRange? = null,
    val guests: Guests = Guests(),
    val length: StayLength? = null,
) {
    val hasDates: Boolean get() = dates != null
}

/** Sort orders for results. Localized by id (S-05). */
enum class SortOrder(val id: String) {
    Recommended("recommended"),
    PriceLowToHigh("price_low_to_high"),
    PriceHighToLow("price_high_to_low"),
    Rating("rating"),
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
) {
    /** How many filters are active, for the "Show 14 stays" button and the filter count badge. */
    val activeCount: Int
        get() = listOf(priceRange != null, amenities.isNotEmpty(), minBedrooms != null, instantBook).count { it } +
            if (sort != SortOrder.Recommended) 1 else 0

    companion object {
        val None = Filters()
    }
}
