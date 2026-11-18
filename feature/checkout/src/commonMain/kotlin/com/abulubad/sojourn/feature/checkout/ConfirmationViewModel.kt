package com.abulubad.sojourn.feature.checkout

import androidx.lifecycle.viewModelScope
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.domain.GetBooking
import com.abulubad.sojourn.core.model.BookingId
import com.abulubad.sojourn.core.navigation.BookingConfirmedKey
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournViewModel
import kotlinx.coroutines.launch

/**
 * "You're booked": the booking in [key] with its reference, stay, dates and what was paid. Checkout stored the
 * booking before showing this, so it reads from the device and works offline.
 */
class ConfirmationViewModel(
    private val key: BookingConfirmedKey,
    getBooking: GetBooking,
) : SojournViewModel<ConfirmationUiState, ConfirmationIntent, ConfirmationEffect>(ConfirmationUiState()) {

    init {
        viewModelScope.launch {
            getBooking(BookingId(key.bookingId)).collect { item ->
                val booking = item?.let { ScreenState.Content(it) }
                    ?: ScreenState.Error(AppError.NotFound(key.bookingId))
                setState { copy(booking = booking) }
            }
        }
    }

    override fun handle(intent: ConfirmationIntent) {
        when (intent) {
            ConfirmationIntent.ViewBookingTapped -> sendEffect(ConfirmationEffect.ViewBooking(key.bookingId))
            ConfirmationIntent.DoneTapped -> sendEffect(ConfirmationEffect.Done)
        }
    }
}
