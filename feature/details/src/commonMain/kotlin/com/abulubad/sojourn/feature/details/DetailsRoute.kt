package com.abulubad.sojourn.feature.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abulubad.sojourn.core.designsystem.components.sheet.rememberSojournSheetState
import com.abulubad.sojourn.core.localization.localizedPlace
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.details_share_text
import com.abulubad.sojourn.core.navigation.GalleryKey
import com.abulubad.sojourn.core.navigation.Navigator
import com.abulubad.sojourn.core.navigation.StayDetailsKey
import com.abulubad.sojourn.core.platform.ShareSheet
import com.abulubad.sojourn.core.ui.CollectEffects
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Details' stateful entry: the ViewModel (scoped to this back-stack entry, opened with [key]), the sheets it
 * animates closed, the platform share sheet, and [DetailsScreen].
 */
@Composable
fun DetailsRoute(key: StayDetailsKey, navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<DetailsViewModel> { parametersOf(key) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val share = koinInject<ShareSheet>()
    val datesSheet = rememberSojournSheetState()
    val priceSheet = rememberPriceSheetState()
    // Words are the screen's: the title and "Paddington, London", in the app's language.
    val shareText by rememberUpdatedState(
        state.stay?.let {
            stringResource(Res.string.details_share_text, it.title.text, localizedPlace(it.cityId, it.areaKey))
        },
    )
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            DetailsEffect.Back -> navigator.back()
            is DetailsEffect.Navigate -> navigator.goTo(effect.key)
            is DetailsEffect.Share -> shareText?.let(share::share)
            DetailsEffect.CloseSheet -> datesSheet.hide()
        }
    }
    DetailsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        datesSheet = datesSheet,
        priceSheet = priceSheet,
    )
}

/** The full-screen gallery's stateful entry. */
@Composable
fun GalleryRoute(key: GalleryKey, navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<GalleryViewModel> { parametersOf(key) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            GalleryEffect.Back -> navigator.back()
        }
    }
    GalleryScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
