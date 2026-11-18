package com.abulubad.sojourn.feature.details

import androidx.lifecycle.viewModelScope
import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.domain.GetQuote
import com.abulubad.sojourn.core.domain.GetStayDetails
import com.abulubad.sojourn.core.domain.ObserveQuote
import com.abulubad.sojourn.core.domain.StayRules
import com.abulubad.sojourn.core.domain.ToggleSaved
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.navigation.CheckoutKey
import com.abulubad.sojourn.core.navigation.GalleryKey
import com.abulubad.sojourn.core.navigation.MapKey
import com.abulubad.sojourn.core.navigation.SojournNavKey
import com.abulubad.sojourn.core.navigation.StayDetailsKey
import com.abulubad.sojourn.core.navigation.toQuery
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.yearMonth

/**
 * Details (ScreenDetails card) for the stay in [key], offline-first. The booking bar shows the card's quote when the
 * guest came with dates (P-02), else the "from" price; "Choose dates" opens the picker, and dates the device can
 * already rule out (minimum stay, capacity, booked nights) are refused before the network is asked. "Reserve" goes
 * to checkout with the quote's id, so checkout charges what the bar showed.
 */
class DetailsViewModel(
    private val key: StayDetailsKey,
    private val getStayDetails: GetStayDetails,
    private val observeQuote: ObserveQuote,
    private val getQuote: GetQuote,
    private val toggleSaved: ToggleSaved,
    clock: AppClock,
) : SojournViewModel<DetailsUiState, DetailsIntent, DetailsEffect>(
    DetailsUiState(query = key.args.toQuery(), today = clock.today()),
) {

    private val stayId = StayId(key.stayId)

    /** Bumped by "Try again"; each bump fetches the stay past the refresh policy. */
    private val refreshes = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            refreshes.collectLatest { refresh ->
                getStayDetails(stayId, force = refresh > 0).collect { loadable ->
                    setState { copy(details = ScreenState.from(loadable) { false }) }
                }
            }
        }
        key.quoteId?.let { id ->
            viewModelScope.launch {
                val quote = observeQuote(QuoteId(id)).filterNotNull().first()
                // Dates chosen here meanwhile win over the card's.
                setState { if (this.quote == null) copy(quote = quote) else this }
            }
        }
    }

    override fun handle(intent: DetailsIntent) {
        when (intent) {
            DetailsIntent.BackTapped -> sendEffect(DetailsEffect.Back)
            DetailsIntent.ShareTapped -> currentState.stay?.let { sendEffect(DetailsEffect.Share(it)) }
            DetailsIntent.SaveTapped -> viewModelScope.launch { toggleSaved(stayId, savedSearch()) }
            is DetailsIntent.PhotoTapped -> navigate(GalleryKey(key.stayId, intent.index))
            DetailsIntent.ShowOnMapTapped -> currentState.stay?.let { stay ->
                navigate(MapKey(key.args.copy(cityId = stay.cityId.value), stayId = stay.id.value))
            }
            DetailsIntent.TranslateTapped -> setState { copy(translated = !translated) }
            DetailsIntent.ReadMoreTapped -> setState { copy(expanded = !expanded) }
            DetailsIntent.RetryTapped -> refreshes.value++
            DetailsIntent.PrimaryTapped -> {
                val quote = currentState.quote
                if (quote != null) navigate(CheckoutKey(quote.id.value)) else openDates()
            }
            DetailsIntent.PriceDetailsTapped,
            is DetailsIntent.MonthShown,
            is DetailsIntent.DatesSelected,
            DetailsIntent.DatesConfirmed,
            DetailsIntent.SheetDismissed,
            -> handleSheet(intent)
        }
    }

    /** The sheets' intents: price details, and the dates picker from first month to priced dates. */
    private fun handleSheet(intent: DetailsIntent) {
        when (intent) {
            DetailsIntent.PriceDetailsTapped -> setState { copy(sheet = DetailsSheet.PriceDetails) }
            is DetailsIntent.MonthShown -> editDates { copy(month = intent.month) }
            is DetailsIntent.DatesSelected -> editDates { copy(selection = intent.dates, problem = null) }
            DetailsIntent.DatesConfirmed -> priceDates()
            DetailsIntent.SheetDismissed -> setState { copy(sheet = null) }
            else -> Unit
        }
    }

    private fun openDates() {
        val state = currentState
        val start = state.quote?.dates?.start ?: state.query.dates?.start ?: state.today
        setState { copy(sheet = DetailsSheet.Dates(month = start.yearMonth, selection = query.dates)) }
    }

    private fun priceDates() {
        val sheet = currentState.sheet as? DetailsSheet.Dates ?: return
        val dates = sheet.selection ?: return
        if (sheet.quoting) return
        editDates { copy(quoting = true, problem = null) }
        viewModelScope.launch {
            when (val result = getQuote(stayId, dates, currentState.query.guests)) {
                is AppResult.Success -> {
                    setState { copy(quote = result.value) }
                    editDates { copy(quoting = false) }
                    sendEffect(DetailsEffect.CloseSheet)
                }
                is AppResult.Failure -> {
                    val problem = StayRules.problemFor(result.error, currentState.stay)
                    editDates { copy(quoting = false, problem = problem) }
                }
            }
        }
    }

    private fun editDates(change: DetailsSheet.Dates.() -> DetailsSheet.Dates) = setState {
        val dates = sheet as? DetailsSheet.Dates ?: return@setState this
        copy(sheet = dates.change())
    }

    /** The dates priced here, else the search's: the Saved tab shows that search's total. */
    private fun savedSearch(): SearchQuery {
        val state = currentState
        val quote = state.quote ?: return state.query
        return state.query.copy(dates = quote.dates, guests = quote.guests)
    }

    private fun navigate(key: SojournNavKey) = sendEffect(DetailsEffect.Navigate(key))
}
