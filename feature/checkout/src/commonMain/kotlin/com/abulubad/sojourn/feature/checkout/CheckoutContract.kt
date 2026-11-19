package com.abulubad.sojourn.feature.checkout

import com.abulubad.sojourn.core.domain.StayDetails
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.Policy
import com.abulubad.sojourn.core.model.Quote
import com.abulubad.sojourn.core.model.QuoteProblem
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.navigation.SojournNavKey
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.UiEffect
import com.abulubad.sojourn.core.ui.UiIntent
import com.abulubad.sojourn.core.ui.UiState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * Checkout's state: the [quote] being paid for (the one Details showed, or the one an edit here priced) and its
 * stay offline-first ([details]). [previousTotal] is the total before the last edit, so a re-price is said, never
 * silent; [notice] is why the last payment did not book; [sheet] is the editor open over the page.
 */
data class CheckoutUiState(
    val today: LocalDate,
    val quote: Quote? = null,
    val details: ScreenState<StayDetails> = ScreenState.Loading,
    val previousTotal: Money? = null,
    val paying: Boolean = false,
    val notice: CheckoutNotice? = null,
    val sheet: CheckoutSheet? = null,
    /** The demo wallet is open: "Pay" asks there before anything is charged. */
    val wallet: Boolean = false,
) : UiState {

    /** The stay, from content or the offline cache. */
    val stay: Stay?
        get() = details.cachedOrData?.stay

    /** Booked nights, which the dates editor shows as unavailable. */
    val blocked: List<DateRange>
        get() = details.cachedOrData?.blocked.orEmpty()

    /** The refundable deposit, held apart from the total and shown under it. */
    val deposit: Money?
        get() = stay?.policies?.filterIsInstance<Policy.Deposit>()?.firstOrNull()?.amount

    /** The free-cancellation policy, when the stay has one. */
    val cancellation: Policy?
        get() = stay?.policies?.firstOrNull { it is Policy.FreeCancellation || it is Policy.FreeCancellationBefore }
}

private val ScreenState<StayDetails>.cachedOrData: StayDetails?
    get() = when (this) {
        is ScreenState.Content -> data
        is ScreenState.Offline -> cached
        else -> null
    }

/** The editors checkout opens in place (ScreenCheckout card: dates and guests are edited here, then re-priced). */
sealed interface CheckoutSheet {
    val quoting: Boolean
    val problem: QuoteProblem?

    /** The dates picker on [month] with the [selection] so far. */
    data class EditDates(
        val month: YearMonth,
        val selection: DateRange?,
        override val quoting: Boolean = false,
        override val problem: QuoteProblem? = null,
    ) : CheckoutSheet

    /** Adults and children, within what the stay sleeps. */
    data class EditGuests(
        val guests: Guests,
        override val quoting: Boolean = false,
        override val problem: QuoteProblem? = null,
    ) : CheckoutSheet
}

/** Why the last tap on "Pay" did not end in a booking. Each one says nothing was charged. */
sealed interface CheckoutNotice {
    /** The payment sheet declined. */
    data object PaymentFailed : CheckoutNotice

    /** The total changed while paying; the page now shows the new [total] and the guest pays again. */
    data class PriceChanged(val total: Money) : CheckoutNotice

    data object Offline : CheckoutNotice

    /** Paid, but the booking could not be confirmed; the authorization is released. */
    data object BookingFailed : CheckoutNotice
}

/** What the guest did on checkout. */
sealed interface CheckoutIntent : UiIntent {
    data object BackTapped : CheckoutIntent
    data object EditDatesTapped : CheckoutIntent
    data object EditGuestsTapped : CheckoutIntent
    data class MonthShown(val month: YearMonth) : CheckoutIntent
    data class DatesSelected(val dates: DateRange?) : CheckoutIntent
    data class GuestsChanged(val guests: Guests) : CheckoutIntent

    /** "Update the total" in an editor. */
    data object EditConfirmed : CheckoutIntent

    /** An editor has closed, however it was closed. */
    data object SheetDismissed : CheckoutIntent

    /** "Pay £1,383" on the page: opens the demo wallet. */
    data object PayTapped : CheckoutIntent

    /** "Pay £1,383" in the demo wallet: pays, then books. */
    data object WalletConfirmed : CheckoutIntent

    /** The demo wallet has closed, however it was closed. Nothing is charged unless it was confirmed. */
    data object WalletDismissed : CheckoutIntent

    /** "Try again" when the stay could not be loaded. */
    data object RetryTapped : CheckoutIntent
}

/** Checkout's one-off outputs. */
sealed interface CheckoutEffect : UiEffect {
    data object Back : CheckoutEffect

    /** Booked: show the confirmation in checkout's place, so back never returns to a paid checkout. */
    data class Booked(val key: SojournNavKey) : CheckoutEffect

    /** The payment haptic: [success] when booked, else the error pattern (motion.md, Haptics). */
    data class PaymentFeedback(val success: Boolean) : CheckoutEffect

    /** Animate the open editor closed: its change is priced. */
    data object CloseSheet : CheckoutEffect

    /** Animate the demo wallet closed: the guest confirmed and paying has started. */
    data object CloseWallet : CheckoutEffect
}
