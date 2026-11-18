package com.abulubad.sojourn.feature.checkout

import androidx.lifecycle.viewModelScope
import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.domain.CreateBooking
import com.abulubad.sojourn.core.domain.GetQuote
import com.abulubad.sojourn.core.domain.GetStayDetails
import com.abulubad.sojourn.core.domain.ObserveQuote
import com.abulubad.sojourn.core.domain.StayRules
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Quote
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.model.QuoteProblem
import com.abulubad.sojourn.core.navigation.BookingConfirmedKey
import com.abulubad.sojourn.core.navigation.CheckoutKey
import com.abulubad.sojourn.core.platform.PaymentRequest
import com.abulubad.sojourn.core.platform.PaymentResult
import com.abulubad.sojourn.core.platform.PaymentSheet
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.yearMonth

/**
 * Checkout (ScreenCheckout card): one review screen before payment, for the quote in [key].
 *
 * - The total is the quote's, the same number the card, the pin and the booking bar showed (P-02); "Pay" charges it.
 * - Dates and guests are edited in place. An edit is priced before it is shown, and a changed total says what it
 *   was, so a re-price is never silent.
 * - Paying goes through the platform sheet, then books. A payment that fails, or a booking that cannot be confirmed,
 *   keeps the guest here with the page as it was and the reason in plain words; nothing is charged, as the payment
 *   is only an authorization until the booking exists. A total that changed meanwhile is shown, not charged.
 */
class CheckoutViewModel(
    private val key: CheckoutKey,
    private val observeQuote: ObserveQuote,
    private val getStayDetails: GetStayDetails,
    private val getQuote: GetQuote,
    private val createBooking: CreateBooking,
    private val payments: PaymentSheet,
    clock: AppClock,
) : SojournViewModel<CheckoutUiState, CheckoutIntent, CheckoutEffect>(CheckoutUiState(today = clock.today())) {

    /** Bumped by "Try again"; each bump fetches the stay past the refresh policy. */
    private val refreshes = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            val quote = observeQuote(QuoteId(key.quoteId)).first()
            if (quote == null) {
                setState { copy(details = ScreenState.Error(AppError.NotFound(key.quoteId))) }
                return@launch
            }
            setState { copy(quote = quote) }
            refreshes.collectLatest { refresh ->
                getStayDetails(quote.stayId, force = refresh > 0).collect { loadable ->
                    setState { copy(details = ScreenState.from(loadable) { false }) }
                }
            }
        }
    }

    override fun handle(intent: CheckoutIntent) {
        when (intent) {
            CheckoutIntent.BackTapped -> sendEffect(CheckoutEffect.Back)
            CheckoutIntent.RetryTapped -> refreshes.value++
            CheckoutIntent.PayTapped -> pay()
            CheckoutIntent.EditDatesTapped -> currentState.quote?.let { quote ->
                setState { copy(sheet = CheckoutSheet.EditDates(quote.dates.start.yearMonth, quote.dates)) }
            }
            CheckoutIntent.EditGuestsTapped -> currentState.quote?.let { quote ->
                setState { copy(sheet = CheckoutSheet.EditGuests(quote.guests)) }
            }
            is CheckoutIntent.MonthShown -> editDates { copy(month = intent.month) }
            is CheckoutIntent.DatesSelected -> editDates { copy(selection = intent.dates, problem = null) }
            is CheckoutIntent.GuestsChanged -> editGuests { copy(guests = intent.guests, problem = null) }
            CheckoutIntent.EditConfirmed -> reprice()
            CheckoutIntent.SheetDismissed -> setState { copy(sheet = null) }
        }
    }

    /** Prices the edit; the page changes only once the new total is known. */
    private fun reprice() {
        val quote = currentState.quote
        val sheet = currentState.sheet?.takeUnless { it.quoting }
        val edit = if (quote != null && sheet != null) edited(quote, sheet) else null
        when {
            quote == null || edit == null -> Unit
            edit == (quote.dates to quote.guests) -> sendEffect(CheckoutEffect.CloseSheet)
            else -> price(quote, edit.first, edit.second)
        }
    }

    /** Prices [dates] and [guests] for the open editor, which closes once the page shows the new total. */
    private fun price(quote: Quote, dates: DateRange, guests: Guests) {
        setSheet(quoting = true, problem = null)
        viewModelScope.launch {
            when (val result = getQuote(quote.stayId, dates, guests)) {
                is AppResult.Success -> {
                    val fresh = result.value
                    setState {
                        copy(quote = fresh, previousTotal = quote.total.takeIf { it != fresh.total }, notice = null)
                    }
                    setSheet(quoting = false, problem = null)
                    sendEffect(CheckoutEffect.CloseSheet)
                }
                is AppResult.Failure -> setSheet(quoting = false, StayRules.problemFor(result.error, currentState.stay))
            }
        }
    }

    /** The dates and guests the open editor asks for; null while no dates are chosen. */
    private fun edited(quote: Quote, sheet: CheckoutSheet): Pair<DateRange, Guests>? = when (sheet) {
        is CheckoutSheet.EditDates -> sheet.selection?.let { it to quote.guests }
        is CheckoutSheet.EditGuests -> quote.dates to sheet.guests
    }

    private fun pay() {
        val state = currentState
        val quote = state.quote ?: return
        if (state.paying || state.stay == null) return
        setState { copy(paying = true, notice = null) }
        viewModelScope.launch {
            when (payments.pay(PaymentRequest(quote.id, quote.total))) {
                PaymentResult.Cancelled -> setState { copy(paying = false) }
                is PaymentResult.Failed -> fail(CheckoutNotice.PaymentFailed)
                is PaymentResult.Authorized -> book(quote)
            }
        }
    }

    private suspend fun book(quote: Quote) {
        when (val booked = createBooking(quote.id)) {
            is AppResult.Success -> {
                sendEffect(CheckoutEffect.PaymentFeedback(success = true))
                sendEffect(CheckoutEffect.Booked(BookingConfirmedKey(booked.value.id.value)))
            }
            is AppResult.Failure -> when (val error = booked.error) {
                AppError.Validation(StayRules.Problem.PRICE_CHANGED) -> showNewPrice(quote)
                else -> fail(if (error == AppError.Offline) CheckoutNotice.Offline else CheckoutNotice.BookingFailed)
            }
        }
    }

    /** The total changed while paying: show the new one and its breakdown, and let the guest pay again. */
    private suspend fun showNewPrice(shown: Quote) {
        when (val fresh = getQuote(shown.stayId, shown.dates, shown.guests)) {
            is AppResult.Success -> {
                setState { copy(quote = fresh.value, previousTotal = shown.total) }
                fail(CheckoutNotice.PriceChanged(fresh.value.total))
            }
            is AppResult.Failure ->
                fail(if (fresh.error == AppError.Offline) CheckoutNotice.Offline else CheckoutNotice.BookingFailed)
        }
    }

    private fun fail(notice: CheckoutNotice) {
        setState { copy(paying = false, notice = notice) }
        sendEffect(CheckoutEffect.PaymentFeedback(success = false))
    }

    private fun setSheet(quoting: Boolean, problem: QuoteProblem?) = setState {
        copy(
            sheet = when (val open = sheet) {
                is CheckoutSheet.EditDates -> open.copy(quoting = quoting, problem = problem)
                is CheckoutSheet.EditGuests -> open.copy(quoting = quoting, problem = problem)
                null -> null
            },
        )
    }

    private fun editDates(change: CheckoutSheet.EditDates.() -> CheckoutSheet.EditDates) = setState {
        val open = sheet as? CheckoutSheet.EditDates ?: return@setState this
        copy(sheet = open.change())
    }

    private fun editGuests(change: CheckoutSheet.EditGuests.() -> CheckoutSheet.EditGuests) = setState {
        val open = sheet as? CheckoutSheet.EditGuests ?: return@setState this
        copy(sheet = open.change())
    }
}
