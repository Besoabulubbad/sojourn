package com.abulubad.sojourn.feature.bookings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheetState
import com.abulubad.sojourn.core.designsystem.components.snackbar.SojournSnackbarHostState
import com.abulubad.sojourn.core.domain.BookingItem
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.key_code_copied
import com.abulubad.sojourn.core.navigation.BookingDetailsKey
import com.abulubad.sojourn.core.navigation.Navigator
import com.abulubad.sojourn.core.navigation.Tab
import com.abulubad.sojourn.core.platform.Clipboard
import com.abulubad.sojourn.core.platform.MapsLauncher
import com.abulubad.sojourn.core.ui.CollectEffects
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The Bookings tab's stateful entry: the ViewModel, maps, the clipboard and "Key code copied", and [BookingsScreen].
 */
@Composable
fun BookingsRoute(navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<BookingsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effects = rememberBookingEffects()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is BookingsEffect.Navigate -> navigator.goTo(effect.key)
            is BookingsEffect.OpenMaps -> effects.openMaps(effect.item)
            is BookingsEffect.Copy -> effects.copy(effect.code)
            BookingsEffect.ShowExplore -> navigator.switchTab(Tab.Explore)
        }
    }
    BookingsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        guideSheet = effects.guideSheet,
        snackbar = effects.snackbar,
    )
}

/** A booking's page: the ViewModel opened with [key], maps, the clipboard, its sheets, and [BookingDetailsScreen]. */
@Composable
fun BookingDetailsRoute(key: BookingDetailsKey, navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<BookingDetailsViewModel> { parametersOf(key) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effects = rememberBookingEffects()
    val cancelSheet = rememberHalfSheetState()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            BookingDetailsEffect.Back -> navigator.back()
            is BookingDetailsEffect.OpenMaps -> effects.openMaps(effect.item)
            is BookingDetailsEffect.Copy -> effects.copy(effect.code)
            BookingDetailsEffect.CloseSheet -> cancelSheet.hide()
        }
    }
    BookingDetailsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        guideSheet = effects.guideSheet,
        cancelSheet = cancelSheet,
        snackbar = effects.snackbar,
    )
}

/** What both booking screens do outside themselves: open maps, copy the key code and say so. */
private class BookingEffects(
    val guideSheet: SojournSheetState,
    val snackbar: SojournSnackbarHostState,
    val openMaps: (BookingItem) -> Unit,
    val copy: (String) -> Unit,
)

@Composable
private fun rememberBookingEffects(): BookingEffects {
    val maps = koinInject<MapsLauncher>()
    val clipboard = koinInject<Clipboard>()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SojournSnackbarHostState() }
    val guideSheet = rememberHalfSheetState()
    // Words are read in composition, in the app's language (which can differ from the device's).
    val copied by rememberUpdatedState(stringResource(Res.string.key_code_copied))
    return remember(maps, clipboard, snackbar, guideSheet) {
        BookingEffects(
            guideSheet = guideSheet,
            snackbar = snackbar,
            openMaps = { item -> item.stay?.let { maps.open(it.location.center, it.title.text) } },
            copy = { code ->
                clipboard.copy(code)
                scope.launch { snackbar.show(copied) }
            },
        )
    }
}
