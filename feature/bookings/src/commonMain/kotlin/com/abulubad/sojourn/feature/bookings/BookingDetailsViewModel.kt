package com.abulubad.sojourn.feature.bookings

import androidx.lifecycle.viewModelScope
import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.domain.CancelBooking
import com.abulubad.sojourn.core.domain.GetBooking
import com.abulubad.sojourn.core.model.BookingId
import com.abulubad.sojourn.core.navigation.BookingDetailsKey
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournViewModel
import kotlinx.coroutines.launch

/**
 * A booking's page (from the Bookings tab, Explore's next stay or the confirmation's "View booking"): the stay, the
 * trip, what was paid, the policies, directions and the check-in guide. Before check-in it can be cancelled; the
 * sheet says what comes back under the stay's policy, and the booking moves to Past with its nights freed.
 */
class BookingDetailsViewModel(
    private val key: BookingDetailsKey,
    getBooking: GetBooking,
    private val cancelBooking: CancelBooking,
    clock: AppClock,
) : SojournViewModel<BookingDetailsUiState, BookingDetailsIntent, BookingDetailsEffect>(
    BookingDetailsUiState(today = clock.today()),
) {

    init {
        viewModelScope.launch {
            getBooking(BookingId(key.bookingId)).collect { item ->
                val booking = item?.let { ScreenState.Content(it) }
                    ?: ScreenState.Error(AppError.NotFound(key.bookingId))
                setState { copy(booking = booking) }
            }
        }
    }

    override fun handle(intent: BookingDetailsIntent) {
        when (intent) {
            BookingDetailsIntent.BackTapped -> sendEffect(BookingDetailsEffect.Back)
            BookingDetailsIntent.DirectionsTapped ->
                currentState.item?.let { sendEffect(BookingDetailsEffect.OpenMaps(it)) }
            BookingDetailsIntent.GuideTapped -> setState { copy(sheet = BookingSheet.Guide) }
            is BookingDetailsIntent.CopyTapped -> sendEffect(BookingDetailsEffect.Copy(intent.text, intent.what))
            is BookingDetailsIntent.AddToCalendarTapped -> sendEffect(BookingDetailsEffect.AddToCalendar(intent.stay))
            BookingDetailsIntent.CancelTapped ->
                if (currentState.canCancel) setState { copy(sheet = BookingSheet.Cancel()) }
            BookingDetailsIntent.CancelConfirmed -> cancel()
            BookingDetailsIntent.SheetDismissed -> setState { copy(sheet = null) }
        }
    }

    private fun cancel() {
        val sheet = currentState.sheet as? BookingSheet.Cancel ?: return
        if (sheet.cancelling) return
        setState { copy(sheet = BookingSheet.Cancel(cancelling = true)) }
        viewModelScope.launch {
            when (val result = cancelBooking(BookingId(key.bookingId))) {
                is AppResult.Success -> sendEffect(BookingDetailsEffect.CloseSheet)
                is AppResult.Failure -> setState { copy(sheet = BookingSheet.Cancel(error = result.error)) }
            }
        }
    }
}
