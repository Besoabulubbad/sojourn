package com.abulubad.sojourn.feature.checkout

import com.abulubad.sojourn.core.domain.BookingItem
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.UiEffect
import com.abulubad.sojourn.core.ui.UiIntent
import com.abulubad.sojourn.core.ui.UiState

/** The confirmation after paying: the booking just made, read from the device. */
data class ConfirmationUiState(val booking: ScreenState<BookingItem> = ScreenState.Loading) : UiState

sealed interface ConfirmationIntent : UiIntent {
    data object ViewBookingTapped : ConfirmationIntent
    data object DoneTapped : ConfirmationIntent
}

sealed interface ConfirmationEffect : UiEffect {
    /** Open the booking's own page. */
    data class ViewBooking(val bookingId: String) : ConfirmationEffect

    /** Back to where the search began. */
    data object Done : ConfirmationEffect
}
