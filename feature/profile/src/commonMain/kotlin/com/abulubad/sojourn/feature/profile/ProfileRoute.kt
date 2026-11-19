package com.abulubad.sojourn.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abulubad.sojourn.core.navigation.Navigator
import com.abulubad.sojourn.core.navigation.Tab
import com.abulubad.sojourn.core.ui.CollectEffects
import org.koin.compose.viewmodel.koinViewModel

/** The Profile tab's stateful entry: the ViewModel, each row's page, and [ProfileScreen]. */
@Composable
fun ProfileRoute(navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is ProfileEffect.Navigate -> navigator.goTo(effect.key)
        }
    }
    ProfileScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

/** The Language page's stateful entry; choosing returns to Profile, now in the chosen language. */
@Composable
fun LanguageRoute(navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<LanguageViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            LanguageEffect.Back -> navigator.back()
        }
    }
    LanguageScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

/** The Appearance page's stateful entry. */
@Composable
fun AppearanceRoute(navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<AppearanceViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            AppearanceEffect.Back -> navigator.back()
        }
    }
    AppearanceScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

/** The Rewards page's stateful entry; with no points yet, "Explore stays" switches to Explore. */
@Composable
fun RewardsRoute(navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<RewardsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            RewardsEffect.Back -> navigator.back()
            RewardsEffect.ShowExplore -> navigator.switchTab(Tab.Explore)
        }
    }
    RewardsScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

/** The Help page: static answers, so no ViewModel. */
@Composable
fun HelpRoute(navigator: Navigator, modifier: Modifier = Modifier) {
    HelpScreen(onBack = { navigator.back() }, modifier = modifier)
}
