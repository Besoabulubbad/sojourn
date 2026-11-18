package com.abulubad.sojourn.feature.filters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abulubad.sojourn.core.designsystem.components.sheet.rememberSojournSheetState
import com.abulubad.sojourn.core.navigation.FiltersKey
import com.abulubad.sojourn.core.navigation.Navigator
import com.abulubad.sojourn.core.ui.CollectEffects
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The filter sheet's stateful entry: the ViewModel (scoped to this back-stack entry, opened with [key]) and
 * [FiltersScreen]. Applying animates the sheet out first; every way out ends in one `back()` once it is hidden.
 */
@Composable
fun FiltersRoute(key: FiltersKey, navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<FiltersViewModel> { parametersOf(key) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheet = rememberSojournSheetState()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            FiltersEffect.Close -> sheet.hide()
            FiltersEffect.Back -> navigator.back()
        }
    }
    FiltersScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier, sheet = sheet)
}
