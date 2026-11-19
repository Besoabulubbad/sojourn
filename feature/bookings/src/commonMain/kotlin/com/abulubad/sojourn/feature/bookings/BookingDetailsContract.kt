package com.abulubad.sojourn.feature.bookings

import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.domain.BookingItem
import com.abulubad.sojourn.core.domain.BookingRules
import com.abulubad.sojourn.core.domain.Refund
import com.abulubad.sojourn.core.platform.CalendarStay
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.UiEffect
import com.abulubad.sojourn.core.ui.UiIntent
import com.abulubad.sojourn.core.ui.UiState
import kotlinx.datetime.LocalDate

/** A booking's own page: the booking from the device, and the sheet open over it. */
data class BookingDetailsUiState(
    val today: LocalDate,
    val booking: ScreenState<BookingItem> = ScreenState.Loading,
    val sheet: BookingSheet? = null,
) : UiState {

    val item: BookingItem?
        get() = (booking as? ScreenState.Content)?.data

    /** Cancelling is offered before check-in only. */
    val canCancel: Boolean
        get() = item?.let { BookingRules.canCancel(it.booking, today) } ?: false

    /** What cancelling today would give back. */
    val refund: Refund?
        get() = item?.let { BookingRules.refund(it.booking, it.stay, today) }
}

/** The sheets over a booking. */
sealed interface BookingSheet {
    data object Guide : BookingSheet

    /** "Cancel this booking?": [cancelling] while the API answers; [error] if it could not. */
    data class Cancel(val cancelling: Boolean = false, val error: AppError? = null) : BookingSheet
}

sealed interface BookingDetailsIntent : UiIntent {
    data object BackTapped : BookingDetailsIntent
    data object DirectionsTapped : BookingDetailsIntent
    data object GuideTapped : BookingDetailsIntent
    data class CopyTapped(val text: String, val what: Copied) : BookingDetailsIntent

    /** [stay] is the calendar entry, worded on screen in the app's language. */
    data class AddToCalendarTapped(val stay: CalendarStay) : BookingDetailsIntent
    data object CancelTapped : BookingDetailsIntent
    data object CancelConfirmed : BookingDetailsIntent

    /** A sheet has closed, however it was closed. */
    data object SheetDismissed : BookingDetailsIntent
}

sealed interface BookingDetailsEffect : UiEffect {
    data object Back : BookingDetailsEffect
    data class OpenMaps(val item: BookingItem) : BookingDetailsEffect
    data class Copy(val text: String, val what: Copied) : BookingDetailsEffect

    /** The stay into the phone's calendar, through the system's own editor. */
    data class AddToCalendar(val stay: CalendarStay) : BookingDetailsEffect

    /** Animate the open sheet closed: the booking is cancelled. */
    data object CloseSheet : BookingDetailsEffect
}
