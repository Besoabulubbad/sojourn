package com.abulubad.sojourn.core.model

/** Currencies the catalogue prices in. Symbols and placement belong to the formatter (P-06), not here. */
enum class Currency(val code: String, val minorUnitsPerMajor: Long) {
    GBP("GBP", 100),
    EUR("EUR", 100),
    DZD("DZD", 100),
}

/**
 * An exact amount in a currency's minor units (pence, cents, centimes). Integer arithmetic only, so breakdown
 * lines sum to their total without rounding surprises (P-05). Arithmetic across currencies is a programming error.
 */
data class Money(val minor: Long, val currency: Currency) : Comparable<Money> {

    val isNegative: Boolean get() = minor < 0
    val isZero: Boolean get() = minor == 0L

    /** Whole major units, truncated toward zero (1383.50 → 1383). */
    val major: Long get() = minor / currency.minorUnitsPerMajor

    /** Minor units beyond the major part, always non-negative (1383.50 → 50). */
    val minorRemainder: Long get() = (minor % currency.minorUnitsPerMajor).let { if (it < 0) -it else it }

    operator fun plus(other: Money): Money = Money(minor + other.sameCurrency(this).minor, currency)
    operator fun minus(other: Money): Money = Money(minor - other.sameCurrency(this).minor, currency)
    operator fun times(quantity: Int): Money = Money(minor * quantity, currency)
    operator fun unaryMinus(): Money = Money(-minor, currency)
    override fun compareTo(other: Money): Int = minor.compareTo(other.sameCurrency(this).minor)

    private fun sameCurrency(reference: Money): Money {
        require(currency == reference.currency) { "Currency mismatch: ${reference.currency} vs $currency" }
        return this
    }

    companion object {
        /** `Money.of(1383, Currency.GBP)` is £1,383.00. */
        fun of(major: Long, currency: Currency, minorRemainder: Long = 0): Money =
            Money(major * currency.minorUnitsPerMajor + minorRemainder, currency)

        fun zero(currency: Currency): Money = Money(0, currency)
    }
}

/** A closed price interval in one currency, for the filters' price range (P-04 carries the [PriceBasis] separately). */
data class MoneyRange(val min: Money, val max: Money) {
    init {
        require(min.currency == max.currency) { "Range must use one currency" }
        require(min <= max) { "min must not exceed max" }
    }

    operator fun contains(amount: Money): Boolean = amount >= min && amount <= max
}
