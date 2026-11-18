package com.abulubad.sojourn.feature.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyState
import com.abulubad.sojourn.core.designsystem.components.photoviewer.SojournPhotoViewer
import com.abulubad.sojourn.core.designsystem.components.skeleton.SojournSkeleton
import com.abulubad.sojourn.core.designsystem.components.topbar.SojournTopBar
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.state_error_body
import com.abulubad.sojourn.core.localization.resources.state_error_title
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournScaffold
import org.jetbrains.compose.resources.stringResource

/**
 * The full-screen gallery (Gallery card): every photo whole on the screen's background, swiped in the layout
 * direction, with the counter ("3 / 13") as the top bar's title and back to Details.
 */
@Composable
fun GalleryScreen(state: GalleryUiState, onIntent: (GalleryIntent) -> Unit, modifier: Modifier = Modifier) {
    val formatter = rememberSojournFormatter()
    val photos = (state.photos as? ScreenState.Content)?.data ?: (state.photos as? ScreenState.Offline)?.cached
    SojournScaffold(
        modifier = modifier,
        topBar = {
            SojournTopBar(
                title = photos?.let { formatter.counter(state.index + 1, it.size) },
                onBack = { onIntent(GalleryIntent.BackTapped) },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                photos != null -> SojournPhotoViewer(
                    photos,
                    initialIndex = state.index,
                    onPageChange = { onIntent(GalleryIntent.PhotoShown(it)) },
                )
                state.photos == ScreenState.Loading -> SojournSkeleton(Modifier.fillMaxSize())
                else -> SojournEmptyState(
                    icon = SojournIcon.ImageFailed,
                    title = stringResource(Res.string.state_error_title),
                    body = stringResource(Res.string.state_error_body),
                    modifier = Modifier.padding(SojournSpacing.space4),
                )
            }
        }
    }
}
