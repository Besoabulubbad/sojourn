package com.abulubad.sojourn.feature.bookings

import com.abulubad.sojourn.core.domain.BookingItem
import com.abulubad.sojourn.core.model.BookingStatus
import com.abulubad.sojourn.core.navigation.SojournNavKey
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.UiEffect
import com.abulubad.sojourn.core.ui.UiIntent
import com.abulubad.sojourn.core.ui.UiState
import kotlinx.datetime.LocalDate

/** Which bookings the tab shows. */
enum class BookingsTab { Upcoming, Past }

/**
 * The Bookings tab's state: every booking on the device ([bookings], statuses following [today]), the [tab] shown,
 * and the booking whose check-in [guide] is open.
 */
data class BookingsUiState(
    val today: LocalDate,
    val tab: BookingsTab = BookingsTab.Upcoming,
    val bookings: ScreenState<List<BookingItem>> = ScreenState.Loading,
    val guide: BookingItem? = null,
) : UiState {

    private val all: List<BookingItem>
        get() = (bookings as? ScreenState.Content)?.data.orEmpty()

    /** Staying now first, then upcoming soonest first. */
    val upcoming: List<BookingItem>
        get() = all.filter { it.booking.status == BookingStatus.Active || it.booking.status == BookingStatus.Upcoming }

    /** Completed and cancelled, latest first. */
    val past: List<BookingItem>
        get() = all.filter {
            it.booking.status == BookingStatus.Completed || it.booking.status == BookingStatus.Cancelled
        }

    /** The bookings of the [tab] shown. */
    val shown: List<BookingItem>
        get() = if (tab == BookingsTab.Upcoming) upcoming else past
}

/** What the guest did on the Bookings tab. */
sealed interface BookingsIntent : UiIntent {
    data class TabSelected(val tab: BookingsTab) : BookingsIntent
    data class BookingTapped(val item: BookingItem) : BookingsIntent
    data class DirectionsTapped(val item: BookingItem) : BookingsIntent
    data class GuideTapped(val item: BookingItem) : BookingsIntent
    data object GuideDismissed : BookingsIntent
    data class CopyTapped(val code: String) : BookingsIntent
    data object ExploreTapped : BookingsIntent
}

/** The Bookings tab's one-off outputs. */
sealed interface BookingsEffect : UiEffect {
    data class Navigate(val key: SojournNavKey) : BookingsEffect

    /** The platform maps app at the stay's area. */
    data class OpenMaps(val item: BookingItem) : BookingsEffect

    /** Copy the key code and say so. */
    data class Copy(val code: String) : BookingsEffect
    data object ShowExplore : BookingsEffect
}
