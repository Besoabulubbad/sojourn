package com.abulubad.sojourn.feature.explore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abulubad.sojourn.core.navigation.Navigator
import com.abulubad.sojourn.core.ui.CollectEffects
import org.koin.compose.viewmodel.koinViewModel

/** Explore's stateful entry: the ViewModel (scoped to this back-stack entry), its effects, and [ExploreScreen]. */
@Composable
fun ExploreRoute(navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<ExploreViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is ExploreEffect.Navigate -> navigator.goTo(effect.key)
        }
    }
    ExploreScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
