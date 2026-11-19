package com.abulubad.sojourn.feature.bookings

import androidx.lifecycle.viewModelScope
import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.domain.ListBookings
import com.abulubad.sojourn.core.navigation.BookingDetailsKey
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournViewModel
import kotlinx.coroutines.launch

/**
 * The Bookings tab (ScreenBookings card): the guest's bookings from the device, so it works offline. Upcoming leads
 * with what arrival day needs (countdown, directions, the check-in guide); Past keeps completed and cancelled stays.
 */
class BookingsViewModel(
    listBookings: ListBookings,
    clock: AppClock,
) : SojournViewModel<BookingsUiState, BookingsIntent, BookingsEffect>(BookingsUiState(today = clock.today())) {

    init {
        viewModelScope.launch {
            listBookings().collect { items ->
                setState { copy(bookings = ScreenState.fromData(items) { it.isEmpty() }) }
            }
        }
    }

    override fun handle(intent: BookingsIntent) {
        when (intent) {
            is BookingsIntent.TabSelected -> setState { copy(tab = intent.tab) }
            is BookingsIntent.BookingTapped ->
                sendEffect(BookingsEffect.Navigate(BookingDetailsKey(intent.item.booking.id.value)))
            is BookingsIntent.DirectionsTapped -> sendEffect(BookingsEffect.OpenMaps(intent.item))
            is BookingsIntent.GuideTapped -> setState { copy(guide = intent.item) }
            BookingsIntent.GuideDismissed -> setState { copy(guide = null) }
            is BookingsIntent.CopyTapped -> sendEffect(BookingsEffect.Copy(intent.text, intent.what))
            BookingsIntent.ExploreTapped -> sendEffect(BookingsEffect.ShowExplore)
        }
    }
}
