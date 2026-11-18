package com.abulubad.sojourn.feature.results

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abulubad.sojourn.core.navigation.Navigator
import com.abulubad.sojourn.core.navigation.ResultsKey
import com.abulubad.sojourn.core.ui.CollectEffects
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Results' stateful entry: the ViewModel (scoped to this back-stack entry, opened with [key]) and [ResultsScreen]. */
@Composable
fun ResultsRoute(key: ResultsKey, navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<ResultsViewModel> { parametersOf(key) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            ResultsEffect.Back -> navigator.back()
            is ResultsEffect.Navigate -> navigator.goTo(effect.key)
        }
    }
    ResultsScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
