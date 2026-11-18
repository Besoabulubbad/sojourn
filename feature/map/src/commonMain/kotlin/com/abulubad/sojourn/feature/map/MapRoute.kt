package com.abulubad.sojourn.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abulubad.sojourn.core.navigation.MapKey
import com.abulubad.sojourn.core.navigation.Navigator
import com.abulubad.sojourn.core.ui.CollectEffects
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The map's stateful entry: the ViewModel (scoped to this back-stack entry, opened with [key]) and [MapScreen].
 * "List" shows the results for the same search in the map's place, and in the list's place when the map was opened
 * from it, so switching back and forth never stacks screens.
 */
@Composable
fun MapRoute(key: MapKey, navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<MapViewModel> { parametersOf(key) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            MapEffect.Back -> navigator.back()
            is MapEffect.Navigate -> navigator.goTo(effect.key)
            is MapEffect.ShowList -> navigator.goToReplacing(effect.key) { it is MapKey || it == effect.key }
        }
    }
    MapScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
