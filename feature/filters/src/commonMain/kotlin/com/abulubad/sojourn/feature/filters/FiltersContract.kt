package com.abulubad.sojourn.feature.filters

import com.abulubad.sojourn.core.domain.FilterPreview
import com.abulubad.sojourn.core.domain.PriceSpread
import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.BedroomChoice
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.model.MoneyRange
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.ui.UiEffect
import com.abulubad.sojourn.core.ui.UiIntent
import com.abulubad.sojourn.core.ui.UiState

/**
 * The filter sheet's state: a [draft] of the guest's filters, applied only by "Show 14 stays", and the live
 * [preview] of what it keeps for the [query] Results shows. [prices] is the last price spread seen, kept while a
 * draft keeps no stays so the slider does not vanish under the guest's thumb. [counting] is true while a change is
 * being counted.
 */
data class FiltersUiState(
    val query: SearchQuery,
    val draft: Filters,
    val preview: FilterPreview? = null,
    val prices: PriceSpread? = null,
    val counting: Boolean = true,
) : UiState {

    /** The nights a total price covers, for "Total price for 15 nights". */
    val nights: Int? get() = query.dates?.nights

    /**
     * The range the slider shows: the draft's, within the bounds, when it is on the cards' basis and currency
     * (P-04); otherwise the full bounds, which filter nothing.
     */
    val shownRange: MoneyRange?
        get() {
            val bounds = prices?.bounds ?: return null
            val range = draft.priceRange
                ?.takeIf { draft.basis == preview?.basis && it.min.currency == bounds.min.currency }
                ?: return bounds
            val min = range.min.coerceIn(bounds.min, bounds.max)
            return MoneyRange(min, range.max.coerceIn(min, bounds.max))
        }

    /** What the apply button says and whether it can be pressed (ScreenFilters card). */
    val apply: ApplyButton
        get() {
            val count = preview?.count
            return when {
                count == null -> ApplyButton.Uncounted
                count == 0 -> ApplyButton.NoMatch
                else -> ApplyButton.Count(count)
            }
        }
}

/** The apply button's label: "Show 14 stays", "No stays match" (disabled), or "Show stays" before a count. */
sealed interface ApplyButton {
    data class Count(val stays: Int) : ApplyButton
    data object NoMatch : ApplyButton
    data object Uncounted : ApplyButton
}

/** What the guest did in the filter sheet. */
sealed interface FiltersIntent : UiIntent {
    data class PriceChanged(val range: MoneyRange) : FiltersIntent
    data class BedroomsSelected(val choice: BedroomChoice?) : FiltersIntent
    data class BedsChanged(val beds: Int) : FiltersIntent
    data class BathroomsChanged(val bathrooms: Int) : FiltersIntent
    data class AmenityTapped(val amenity: Amenity) : FiltersIntent
    data object FreeCancellationTapped : FiltersIntent
    data object InstantBookTapped : FiltersIntent
    data object ClearAllTapped : FiltersIntent
    data object ApplyTapped : FiltersIntent

    /** The sheet has closed, however it was closed. */
    data object Dismissed : FiltersIntent
}

/** The filter sheet's one-off outputs. */
sealed interface FiltersEffect : UiEffect {
    /** Animate the sheet closed: the filters are applied. */
    data object Close : FiltersEffect

    /** The sheet is gone: leave the destination. */
    data object Back : FiltersEffect
}
