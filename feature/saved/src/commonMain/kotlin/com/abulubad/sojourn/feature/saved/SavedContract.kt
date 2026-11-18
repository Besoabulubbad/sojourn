package com.abulubad.sojourn.feature.saved

import com.abulubad.sojourn.core.domain.SavedStay
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.navigation.SojournNavKey
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.UiEffect
import com.abulubad.sojourn.core.ui.UiIntent
import com.abulubad.sojourn.core.ui.UiState

/** The Saved tab's state: the guest's saved stays, newest first, each priced for the search it was saved from. */
data class SavedUiState(val saved: ScreenState<List<SavedStay>> = ScreenState.Loading) : UiState

/** What the guest did on the Saved tab. */
sealed interface SavedIntent : UiIntent {
    data class StayTapped(val stay: SavedStay) : SavedIntent

    /** The heart on a saved card: it comes off the list at once, with "Undo" (no confirmation, ScreenSaved card). */
    data class RemoveTapped(val id: StayId) : SavedIntent
    data object UndoTapped : SavedIntent

    /** The empty state's one action. */
    data object ExploreTapped : SavedIntent
}

/** The Saved tab's one-off outputs. */
sealed interface SavedEffect : UiEffect {
    data class Navigate(val key: SojournNavKey) : SavedEffect

    /** "Removed from Saved" with "Undo", for 5 seconds. */
    data object ShowRemoved : SavedEffect

    /** To the Explore tab, where stays are found. */
    data object ShowExplore : SavedEffect
}
