package com.abulubad.sojourn.feature.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyState
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyStateAction
import com.abulubad.sojourn.core.designsystem.components.gallery.SojournGalleryDefaults
import com.abulubad.sojourn.core.designsystem.components.skeleton.SojournSkeleton
import com.abulubad.sojourn.core.designsystem.components.snackbar.SojournSnackbarHost
import com.abulubad.sojourn.core.designsystem.components.snackbar.SojournSnackbarHostState
import com.abulubad.sojourn.core.designsystem.components.staycard.SojournStayCard
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.domain.SavedStay
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_explore_stays
import com.abulubad.sojourn.core.localization.resources.saved_empty_body
import com.abulubad.sojourn.core.localization.resources.saved_empty_title
import com.abulubad.sojourn.core.localization.resources.tab_saved
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournScaffold
import com.abulubad.sojourn.core.ui.SojournTabBar
import org.jetbrains.compose.resources.stringResource

/**
 * The Saved tab (ScreenSaved card): the guest's shortlist, kept for comparing stays over days.
 *
 * - "Saved", then the saved stays as cards, newest first. Each shows the total for the dates it was saved with, or
 *   the "from" price when it had none.
 * - The heart takes a stay off at once; "Removed from Saved" with "Undo" shows above the tab bar for 5 seconds. No
 *   confirmation dialog.
 * - Empty: what the heart does, and "Explore stays". Saved stays are on the device, so there is no offline or error
 *   state; loading is only the moment the database is read.
 */
@Composable
fun SavedScreen(
    state: SavedUiState,
    onIntent: (SavedIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbar: SojournSnackbarHostState = SojournSnackbarHostState(),
) {
    SojournScaffold(modifier, bottomBar = { SojournTabBar() }) { padding ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = SojournSpacing.space4,
                    end = SojournSpacing.space4,
                    top = padding.calculateTopPadding() + SojournSpacing.space2,
                    bottom = padding.calculateBottomPadding() + SojournSpacing.space6,
                ),
                verticalArrangement = Arrangement.spacedBy(SojournSpacing.space5),
            ) {
                item("title") {
                    BasicText(
                        stringResource(Res.string.tab_saved),
                        modifier = Modifier.semantics { heading() },
                        style = SojournTheme.type.titleLg.copy(color = SojournTheme.colors.ink),
                    )
                }
                when (val saved = state.saved) {
                    is ScreenState.Content -> items(saved.data, key = { it.result.stay.id.value }) { stay ->
                        Card(stay, onIntent)
                    }
                    ScreenState.Empty -> item("empty") {
                        SojournEmptyState(
                            icon = SojournIcon.Saved,
                            title = stringResource(Res.string.saved_empty_title),
                            body = stringResource(Res.string.saved_empty_body),
                            modifier = Modifier.padding(top = SojournSpacing.space10),
                            action = SojournEmptyStateAction(stringResource(Res.string.action_explore_stays)) {
                                onIntent(SavedIntent.ExploreTapped)
                            },
                        )
                    }
                    // Local data: no network to wait for or lose, so loading is brief and there is no error page.
                    else -> item("loading") { Loading() }
                }
            }
            SojournSnackbarHost(
                snackbar,
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = padding.calculateBottomPadding() + SojournSpacing.space3)
                    .padding(horizontal = SojournSpacing.space3),
            )
        }
    }
}

@Composable
private fun Card(stay: SavedStay, onIntent: (SavedIntent) -> Unit) {
    val result = stay.result
    SojournStayCard(
        stay = result.stay,
        price = result.price,
        saved = true,
        onClick = { onIntent(SavedIntent.StayTapped(stay)) },
        onToggleSave = { onIntent(SavedIntent.RemoveTapped(result.stay.id)) },
    )
}

/** A card's shape while the database is read. */
@Composable
private fun Loading() {
    SojournSkeleton(
        Modifier.fillMaxWidth().aspectRatio(SojournGalleryDefaults.ASPECT_RATIO),
        SojournShapes.lg,
    )
}
