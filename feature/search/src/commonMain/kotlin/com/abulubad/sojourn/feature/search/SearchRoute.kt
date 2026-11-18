package com.abulubad.sojourn.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abulubad.sojourn.core.navigation.Navigator
import com.abulubad.sojourn.core.navigation.SearchKey
import com.abulubad.sojourn.core.ui.CollectEffects
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The search flow's stateful entry: the ViewModel (scoped to this back-stack entry, opened with [key]), its effects,
 * and [SearchScreen]. "Search" replaces the flow with the results, so back from the results returns to where the
 * flow was opened, not to its last step.
 */
@Composable
fun SearchRoute(key: SearchKey, navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<SearchViewModel> { parametersOf(key) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            SearchEffect.Close -> navigator.back()
            is SearchEffect.ShowResults -> {
                navigator.back()
                navigator.goTo(effect.key)
            }
        }
    }
    SearchScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
