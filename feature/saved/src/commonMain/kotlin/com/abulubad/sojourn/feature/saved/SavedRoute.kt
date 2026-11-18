package com.abulubad.sojourn.feature.saved

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abulubad.sojourn.core.designsystem.components.snackbar.SojournSnackbarHostState
import com.abulubad.sojourn.core.designsystem.components.snackbar.SojournSnackbarResult
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_undo
import com.abulubad.sojourn.core.localization.resources.saved_removed
import com.abulubad.sojourn.core.navigation.Navigator
import com.abulubad.sojourn.core.navigation.Tab
import com.abulubad.sojourn.core.ui.CollectEffects
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The Saved tab's stateful entry: the ViewModel, the snackbar that offers "Undo" after a removal, and [SavedScreen].
 */
@Composable
fun SavedRoute(navigator: Navigator, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<SavedViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SojournSnackbarHostState() }
    val scope = rememberCoroutineScope()
    // Words are read in composition, in the app's language (which can differ from the device's).
    val removedText by rememberUpdatedState(stringResource(Res.string.saved_removed))
    val undoText by rememberUpdatedState(stringResource(Res.string.action_undo))
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is SavedEffect.Navigate -> navigator.goTo(effect.key)
            SavedEffect.ShowExplore -> navigator.switchTab(Tab.Explore)
            // Shown on its own coroutine: the 5 seconds must not hold up the next effect.
            SavedEffect.ShowRemoved -> scope.launch {
                val result = snackbar.show(removedText, undoText)
                if (result == SojournSnackbarResult.ActionPerformed) viewModel.onIntent(SavedIntent.UndoTapped)
            }
        }
    }
    SavedScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier, snackbar = snackbar)
}
