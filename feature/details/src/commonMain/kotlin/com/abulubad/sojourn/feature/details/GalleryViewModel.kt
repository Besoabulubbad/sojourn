package com.abulubad.sojourn.feature.details

import androidx.lifecycle.viewModelScope
import com.abulubad.sojourn.core.common.map
import com.abulubad.sojourn.core.domain.GetStayDetails
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.navigation.GalleryKey
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournViewModel
import kotlinx.coroutines.launch

/** The full-screen gallery for the stay in [key], opened on the photo tapped; the stay comes from the cache. */
class GalleryViewModel(
    key: GalleryKey,
    getStayDetails: GetStayDetails,
) : SojournViewModel<GalleryUiState, GalleryIntent, GalleryEffect>(GalleryUiState(index = key.index)) {

    init {
        viewModelScope.launch {
            getStayDetails(StayId(key.stayId)).collect { loadable ->
                val photos = loadable.map { it.stay.photos }
                setState { copy(photos = ScreenState.from(photos) { it.isEmpty() }) }
            }
        }
    }

    override fun handle(intent: GalleryIntent) {
        when (intent) {
            is GalleryIntent.PhotoShown -> setState { copy(index = intent.index) }
            GalleryIntent.BackTapped -> sendEffect(GalleryEffect.Back)
        }
    }
}
