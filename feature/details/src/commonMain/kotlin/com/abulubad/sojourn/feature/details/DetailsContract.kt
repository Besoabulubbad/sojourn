package com.abulubad.sojourn.feature.details

import com.abulubad.sojourn.core.domain.StayDetails
import com.abulubad.sojourn.core.domain.StayRules
import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.HostText
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.Quote
import com.abulubad.sojourn.core.model.QuoteProblem
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.navigation.SojournNavKey
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.UiEffect
import com.abulubad.sojourn.core.ui.UiIntent
import com.abulubad.sojourn.core.ui.UiState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * Details' state: the stay offline-first ([details]), the dated [quote] when there is one (from the card, or chosen
 * here), and what the guest has opened: the [sheet], the [translated] description and the [expanded] one.
 * [query] is the search Details came from: its guests price a new quote, and its basis sets a "from" price.
 */
data class DetailsUiState(
    val query: SearchQuery,
    val today: LocalDate,
    val details: ScreenState<StayDetails> = ScreenState.Loading,
    val quote: Quote? = null,
    val translated: Boolean = false,
    val expanded: Boolean = false,
    val sheet: DetailsSheet? = null,
) : UiState {

    /** The stay, from content or the offline cache. */
    val stay: Stay?
        get() = when (val shown = details) {
            is ScreenState.Content -> shown.data.stay
            is ScreenState.Offline -> shown.cached?.stay
            else -> null
        }

    /**
     * The booking bar's price: the quote's all-in total with dates (P-02), otherwise the "from" price on the
     * search's basis (P-03; a month unless the guest searched for a weekend or a week).
     */
    val price: PriceDisplay?
        get() {
            quote?.let { return it.display }
            val stay = stay ?: return null
            // Without dates the basis is a night or a month, never a stay.
            return StayRules.fromPrice(stay, StayRules.basisFor(query.copy(dates = null)))
        }

    /** The description in the reader's language when [translated] and a translation exists, else the host's own. */
    fun description(appLanguage: String): HostText? {
        val stay = stay ?: return null
        if (!translated) return stay.description
        return stay.descriptionTranslations.firstOrNull { it.languageTag == appLanguage } ?: stay.description
    }
}

/** The bottom sheets Details opens. */
sealed interface DetailsSheet {
    /** "Choose dates": the picker on [month], the [selection] so far, and why the last try could not be priced. */
    data class Dates(
        val month: YearMonth,
        val selection: DateRange? = null,
        val quoting: Boolean = false,
        val problem: QuoteProblem? = null,
    ) : DetailsSheet

    /** "Price details": the quote's breakdown, or what a "from" price leaves out. */
    data object PriceDetails : DetailsSheet
}

/** What the guest did on Details. */
sealed interface DetailsIntent : UiIntent {
    data object BackTapped : DetailsIntent
    data object ShareTapped : DetailsIntent
    data object SaveTapped : DetailsIntent
    data class PhotoTapped(val index: Int) : DetailsIntent
    data object ShowOnMapTapped : DetailsIntent
    data object TranslateTapped : DetailsIntent
    data object ReadMoreTapped : DetailsIntent
    data object RetryTapped : DetailsIntent

    /** The booking bar's button: "Reserve" with a quote, "Choose dates" without. */
    data object PrimaryTapped : DetailsIntent
    data object PriceDetailsTapped : DetailsIntent
    data class MonthShown(val month: YearMonth) : DetailsIntent
    data class DatesSelected(val dates: DateRange?) : DetailsIntent
    data object DatesConfirmed : DetailsIntent

    /** A sheet has closed, however it was closed. */
    data object SheetDismissed : DetailsIntent
}

/** Details' one-off outputs. */
sealed interface DetailsEffect : UiEffect {
    data object Back : DetailsEffect
    data class Navigate(val key: SojournNavKey) : DetailsEffect

    /** Share the stay: its title and area, put into words by the screen. */
    data class Share(val stay: Stay) : DetailsEffect

    /** Animate the open sheet closed: its job is done (dates priced). */
    data object CloseSheet : DetailsEffect
}

/**
 * Amenities ranked for longer stays (ScreenDetails card): Wi-Fi, workspace, washer, kitchen and heating first, then
 * the rest in the catalogue's order.
 */
internal val AmenityRanking: List<Amenity> = listOf(
    Amenity.Wifi,
    Amenity.Workspace,
    Amenity.Washer,
    Amenity.Kitchen,
    Amenity.Heating,
).let { first -> first + (Amenity.entries - first.toSet()) }
