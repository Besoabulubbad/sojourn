package com.abulubad.sojourn.core.model

/** What a price is per. Filters must use the same basis as the cards they filter (P-04). */
enum class PriceBasis { Night, Month, Stay }

/**
 * Costs a "from" price does not include. Localized by id (S-05); listed so the guest is told what is excluded
 * (P-03).
 */
enum class PriceExclusion(val id: String) {
    Utilities("utilities"),
    CleaningFee("cleaning_fee"),
    Deposit("deposit"),
    CityTax("city_tax"),
}

/**
 * The only thing a price component accepts (P-01). A `Double` or `Long` can never reach the UI as a price.
 *
 * - [Total] is the all-in amount for chosen dates. It carries the [QuoteId] so the card, pin, booking bar, checkout
 *   and pay button all show the same number from the same quote (P-02).
 * - [From] is shown when no dates are chosen and must say what it excludes (P-03).
 */
sealed interface PriceDisplay {
    val amount: Money

    data class Total(val quoteId: QuoteId, override val amount: Money, val nights: Int) : PriceDisplay {
        init {
            require(nights > 0) { "A total covers at least one night" }
        }
    }

    data class From(
        override val amount: Money,
        val per: PriceBasis,
        val excludes: List<PriceExclusion>,
    ) : PriceDisplay {
        init {
            require(per != PriceBasis.Stay) { "A 'from' price is per night or per month, never per stay" }
            require(excludes.isNotEmpty()) { "A 'from' price must state what it excludes (P-03)" }
        }
    }
}

/**
 * Kinds of lines a quote breaks down into. Labels are localized by id (S-05).
 *
 * The refundable deposit is not a line: it is not part of the total. It comes from `Policy.Deposit` and is
 * shown under the total.
 */
enum class PriceLineKind(val id: String) {
    Nights("nights"),
    LongStayDiscount("long_stay_discount"),
    CleaningFee("cleaning_fee"),
    ServiceFee("service_fee"),
    CityTax("city_tax"),
}

/**
 * One line of a [PriceBreakdown]. [quantity] is the multiplier shown next to the label (`£92 × 15 nights`),
 * [unitAmount] its per-unit price when the line is a product. Discounts are negative amounts, shown with a minus
 * sign in `brandInk` (brand book), never as a separate "savings" colour pair.
 */
data class PriceLine(
    val kind: PriceLineKind,
    val amount: Money,
    val quantity: Int? = null,
    val unitAmount: Money? = null,
) {
    init {
        require((quantity == null) == (unitAmount == null)) { "quantity and unitAmount come together" }
        if (quantity != null && unitAmount != null) {
            require(unitAmount * quantity == amount) { "$kind: $unitAmount × $quantity != $amount" }
        }
    }
}

/**
 * The lines of a quote and their total. Construction fails if the lines do not sum to the total or mix
 * currencies, so the invariant P-05 holds by type, not by review.
 */
data class PriceBreakdown(val lines: List<PriceLine>, val total: Money) {
    init {
        require(lines.isNotEmpty()) { "A breakdown has at least one line" }
        require(lines.all { it.amount.currency == total.currency }) { "All lines share the total's currency" }
        val sum = lines.fold(Money.zero(total.currency)) { acc, line -> acc + line.amount }
        require(sum == total) { "Breakdown lines sum to $sum but total is $total (P-05)" }
    }

    companion object {
        /** Builds a breakdown whose total is the sum of [lines]; the usual way to construct one. */
        fun of(lines: List<PriceLine>): PriceBreakdown {
            require(lines.isNotEmpty()) { "A breakdown has at least one line" }
            val currency = lines.first().amount.currency
            return PriceBreakdown(lines, lines.fold(Money.zero(currency)) { acc, line -> acc + line.amount })
        }
    }
}
