package com.abulubad.sojourn.feature.details

import com.abulubad.sojourn.core.model.Photo
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.UiEffect
import com.abulubad.sojourn.core.ui.UiIntent
import com.abulubad.sojourn.core.ui.UiState

/** The full-screen gallery: the stay's [photos] and the one shown, for the counter ("3 / 13"). */
data class GalleryUiState(
    val index: Int,
    val photos: ScreenState<List<Photo>> = ScreenState.Loading,
) : UiState

/** What the guest did in the gallery. */
sealed interface GalleryIntent : UiIntent {
    data class PhotoShown(val index: Int) : GalleryIntent
    data object BackTapped : GalleryIntent
}

/** The gallery's one-off outputs. */
sealed interface GalleryEffect : UiEffect {
    data object Back : GalleryEffect
}
