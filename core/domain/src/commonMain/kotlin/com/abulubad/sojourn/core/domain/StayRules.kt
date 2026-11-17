package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.Policy
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.Rating
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayLength

/**
 * Business rules from the engineering standards that more than one use case applies. Pure functions: every rule
 * here has a test in `StayRulesTest`.
 */
object StayRules {

    /** A month, for "from … a month" prices. The same 30 nights the catalogue's city prices use. */
    const val NIGHTS_PER_MONTH = 30

    /** N-04: ratings with fewer reviews are hidden, and do not rank in a "rating" sort. */
    const val MIN_REVIEWS_SHOWN = 3

    private const val PERCENT = 100

    /**
     * The basis cards show for [query] (P-03, P-04). With dates, the all-in total for the stay; without, "from …"
     * per night for short stays (a weekend or a week) and per month otherwise, the brand's default.
     */
    fun basisFor(query: SearchQuery): PriceBasis = when {
        query.dates != null -> PriceBasis.Stay
        query.length == StayLength.Weekend || query.length == StayLength.Week -> PriceBasis.Night
        else -> PriceBasis.Month
    }

    /**
     * [stay]'s "from" price on [basis] (Night or Month), keeping its exclusions (P-03). A month is
     * [NIGHTS_PER_MONTH] nights less the long-stay discount.
     */
    fun fromPrice(stay: Stay, basis: PriceBasis): PriceDisplay.From {
        require(basis != PriceBasis.Stay) { "A 'from' price is per night or per month" }
        val from = stay.fromPrice
        if (from.per == basis) return from
        val amount = when (basis) {
            PriceBasis.Month -> monthly(from.amount, stay.longStayDiscountPercent)
            else -> Money(from.amount.minor / NIGHTS_PER_MONTH, from.amount.currency)
        }
        return PriceDisplay.From(amount, basis, from.excludes)
    }

    /** The rating to show, or null under [MIN_REVIEWS_SHOWN] reviews (N-04). */
    fun visibleRating(rating: Rating?): Rating? = rating?.takeIf { it.count >= MIN_REVIEWS_SHOWN }

    /**
     * Why [stay] cannot be booked for [dates] and [guests] before asking the network, or null. The reasons are the
     * same ids the API uses: `minimum_nights`, `too_many_guests`, `unavailable`.
     */
    fun bookingProblem(stay: Stay, dates: DateRange, guests: Guests, blocked: List<DateRange>): String? {
        val minimum = stay.policies.filterIsInstance<Policy.MinimumStay>().maxOfOrNull { it.nights } ?: 1
        return when {
            dates.nights < minimum -> Problem.MINIMUM_NIGHTS
            guests.total > stay.facts.sleeps -> Problem.TOO_MANY_GUESTS
            blocked.any { it.start < dates.end && dates.start < it.end } -> Problem.UNAVAILABLE
            else -> null
        }
    }

    /** A month of nights; a month is long enough for the long-stay discount (30 ≥ [StayLength.Month] nights). */
    private fun monthly(nightly: Money, discountPercent: Int?): Money {
        val month = nightly * NIGHTS_PER_MONTH
        val discount = discountPercent?.let { month.minor * it / PERCENT } ?: 0
        return Money(month.minor - discount, nightly.currency)
    }

    /** `AppError.Validation` reasons, shared with the API. */
    object Problem {
        const val MINIMUM_NIGHTS = "minimum_nights"
        const val TOO_MANY_GUESTS = "too_many_guests"
        const val UNAVAILABLE = "unavailable"
        const val DATES_IN_PAST = "dates_in_past"
        const val PRICE_CHANGED = "price_changed"
    }
}
