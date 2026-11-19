package com.abulubad.sojourn.feature.checkout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abulubad.sojourn.core.designsystem.components.sheet.rememberSojournSheetState
import com.abulubad.sojourn.core.designsystem.theme.rememberSojournHaptics
import com.abulubad.sojourn.core.navigation.BookingConfirmedKey
import com.abulubad.sojourn.core.navigation.BookingDetailsKey
import com.abulubad.sojourn.core.navigation.CheckoutKey
import com.abulubad.sojourn.core.navigation.ExploreKey
import com.abulubad.sojourn.core.navigation.Navigator
import com.abulubad.sojourn.core.ui.CollectEffects
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Checkout's stateful entry: the ViewModel (scoped to this back-stack entry, opened with [key]), the editors it
 * animates closed, the payment haptics, and [CheckoutScreen]. Once booked, the confirmation takes checkout's place.
 */
@Composable
fun CheckoutRoute(key: CheckoutKey, navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<CheckoutViewModel> { parametersOf(key) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptics = rememberSojournHaptics()
    val datesSheet = rememberSojournSheetState()
    val guestsSheet = rememberGuestsSheetState()
    val walletSheet = rememberWalletSheetState()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            CheckoutEffect.Back -> navigator.back()
            is CheckoutEffect.Booked -> navigator.goToReplacing(effect.key) { it is CheckoutKey }
            is CheckoutEffect.PaymentFeedback -> if (effect.success) haptics.success() else haptics.error()
            CheckoutEffect.CloseSheet -> when (viewModel.state.value.sheet) {
                is CheckoutSheet.EditDates -> datesSheet.hide()
                is CheckoutSheet.EditGuests -> guestsSheet.hide()
                null -> Unit
            }
            CheckoutEffect.CloseWallet -> walletSheet.hide()
        }
    }
    CheckoutScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        datesSheet = datesSheet,
        guestsSheet = guestsSheet,
        walletSheet = walletSheet,
    )
}

/**
 * The confirmation's stateful entry. "View booking" opens the booking's page; "Done" returns to the start of the
 * tab, as the trip is planned.
 */
@Composable
fun ConfirmationRoute(key: BookingConfirmedKey, navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<ConfirmationViewModel> { parametersOf(key) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is ConfirmationEffect.ViewBooking -> navigator.goTo(BookingDetailsKey(effect.bookingId))
            // Explore's root on Explore; another tab's root when it is not in the stack.
            ConfirmationEffect.Done -> navigator.popTo(ExploreKey)
        }
    }
    ConfirmationScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
