package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.common.Loadable
import com.abulubad.sojourn.core.common.map
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.MoneyRange
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.SearchQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The filter sheet's live view of a draft (ScreenFilters card): how many stays it keeps, for "Show 14 stays", and
 * where the prices sit on the cards' [basis] (P-04) once the price range is set aside, for the slider's histogram.
 * [prices] is null when no stay has a price to place.
 */
data class FilterPreview(val count: Int, val basis: PriceBasis, val prices: PriceSpread?)

/**
 * The prices of the stays a draft keeps before its price range: [bounds] in whole steps around them (£10 a night,
 * £100 a month or a stay), and [histogram] counting them in [PreviewFilters.BUCKETS] equal buckets across the bounds.
 */
data class PriceSpread(val bounds: MoneyRange, val histogram: List<Int>)

/**
 * Previews a draft of the filters for [SearchQuery] without applying it: the same search Results runs (the count
 * matches the list the guest returns to), run once without the price range so the histogram shows every price the
 * other filters keep.
 */
class PreviewFilters(private val searchStays: SearchStays) {

    operator fun invoke(query: SearchQuery, filters: Filters): Flow<Loadable<FilterPreview>> =
        searchStays(query, filters.copy(priceRange = null)).map { loadable ->
            loadable.map { results ->
                val kept = results.stays.count { StayRules.inPriceRange(it.price, filters, results.basis) }
                FilterPreview(kept, results.basis, spread(results.stays.map { it.price.amount }, results.basis))
            }
        }

    private fun spread(amounts: List<Money>, basis: PriceBasis): PriceSpread? {
        // Places the most common currency; a city prices in one, so the rest is an edge the range cannot compare.
        val currency = amounts.groupingBy { it.currency }.eachCount().maxByOrNull { it.value }?.key ?: return null
        val majors = amounts.filter { it.currency == currency }.map { it.major }
        val step = if (basis == PriceBasis.Night) NIGHT_STEP else STEP
        val low = majors.min().floorDiv(step) * step
        val high = maxOf((majors.max() + step - 1).floorDiv(step) * step, low + step)
        val histogram = IntArray(BUCKETS)
        majors.forEach { major ->
            histogram[((major - low) * BUCKETS / (high - low)).toInt().coerceIn(0, BUCKETS - 1)]++
        }
        return PriceSpread(MoneyRange(Money.of(low, currency), Money.of(high, currency)), histogram.toList())
    }

    companion object {
        /** The histogram's bars, as on the RangeSlider card. */
        const val BUCKETS = 14
        private const val NIGHT_STEP = 10L
        private const val STEP = 100L
    }
}
