package com.abulubad.sojourn.core.model

import kotlin.time.Instant

/**
 * A priced stay for specific dates. One quote feeds every surface that shows the total (P-02); the breakdown's
 * lines sum to the total by construction (P-05).
 */
data class Quote(
    val id: QuoteId,
    val stayId: StayId,
    val dates: DateRange,
    val guests: Guests,
    val breakdown: PriceBreakdown,
    /** When the API priced it, set as it is stored; null until then. Offline, older prices say their age. */
    val pricedAt: Instant? = null,
) {
    val total: Money get() = breakdown.total
    val nights: Int get() = dates.nights

    /** The one price display every surface uses for this quote. */
    val display: PriceDisplay.Total get() = PriceDisplay.Total(quoteId = id, amount = total, nights = nights)
}
