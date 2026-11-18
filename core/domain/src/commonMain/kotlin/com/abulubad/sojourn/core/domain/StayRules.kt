package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.model.GeoPoint
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.Policy
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.Rating
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayLength
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Business rules from the engineering standards that more than one use case applies. Pure functions: every rule
 * here has a test in `StayRulesTest`.
 */
object StayRules {

    /**
     * Whether [price] passes the price range in [filters] for cards on [basis]. P-04: a range on another basis does
     * not apply; one in another currency excludes what it cannot compare.
     */
    fun inPriceRange(price: PriceDisplay, filters: Filters, basis: PriceBasis): Boolean {
        val range = filters.priceRange ?: return true
        if (filters.basis != basis) return true
        return price.amount.currency == range.min.currency && price.amount in range
    }

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
        return when {
            dates.nights < minimumNights(stay) -> Problem.MINIMUM_NIGHTS
            guests.total > stay.facts.sleeps -> Problem.TOO_MANY_GUESTS
            blocked.any { it.start < dates.end && dates.start < it.end } -> Problem.UNAVAILABLE
            else -> null
        }
    }

    /**
     * Whether [stay] has a free run of [length] (or its own minimum stay, if longer) that starts in [month], on or
     * after [today]: the flexible search's "1 month, starting in March". A run may end after the month.
     */
    fun hasFreeRun(
        stay: Stay,
        length: StayLength,
        month: YearMonth,
        blocked: List<DateRange>,
        today: LocalDate,
    ): Boolean {
        val nights = maxOf(length.minNights, minimumNights(stay))
        val first = maxOf(month.firstDay, today)
        return generateSequence(first) { it.plus(DatePeriod(days = 1)) }
            .takeWhile { it <= month.lastDay }
            .any { start ->
                val end = start.plus(DatePeriod(days = nights))
                blocked.none { it.start < end && start < it.end }
            }
    }

    /** Whether [stay] can still be cancelled for free: a window that closes on a date counts until that date. */
    fun hasFreeCancellation(stay: Stay, today: LocalDate): Boolean = stay.policies.any { policy ->
        when (policy) {
            is Policy.FreeCancellation -> policy.until >= today
            is Policy.FreeCancellationBefore -> true
            else -> false
        }
    }

    /** The great-circle distance between two points, in whole metres (haversine, mean Earth radius). */
    fun distanceMetres(from: GeoPoint, to: GeoPoint): Int {
        val lat1 = from.latitude * RADIANS
        val lat2 = to.latitude * RADIANS
        val dLat = lat2 - lat1
        val dLon = (to.longitude - from.longitude) * RADIANS
        val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        return (2 * EARTH_RADIUS_METRES * asin(sqrt(h))).roundToInt()
    }

    private const val RADIANS = PI / 180
    private const val EARTH_RADIUS_METRES = 6_371_000.0

    private fun minimumNights(stay: Stay): Int =
        stay.policies.filterIsInstance<Policy.MinimumStay>().maxOfOrNull { it.nights } ?: 1

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
