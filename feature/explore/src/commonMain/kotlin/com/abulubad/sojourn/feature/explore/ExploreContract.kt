package com.abulubad.sojourn.feature.explore

import com.abulubad.sojourn.core.model.Booking
import com.abulubad.sojourn.core.model.City
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.Photo
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.StayLength
import com.abulubad.sojourn.core.navigation.SojournNavKey
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.UiEffect
import com.abulubad.sojourn.core.ui.UiIntent
import com.abulubad.sojourn.core.ui.UiState

/**
 * Explore's state. The greeting, search bar and stay-length shortcuts are always there; [cities] follows the
 * offline-first rule (cached cities under the offline banner); [resume] is the "Continue" row, absent on first run.
 */
data class ExploreUiState(
    val cities: ScreenState<List<City>> = ScreenState.Loading,
    val resume: ExploreResume? = null,
) : UiState

/** What the "Continue" row resumes: the next upcoming stay if there is one, otherwise the last search. */
sealed interface ExploreResume {
    val photo: Photo?

    data class LastSearch(val query: SearchQuery, override val photo: Photo?) : ExploreResume

    data class NextStay(val booking: Booking, val cityId: CityId?, override val photo: Photo?) : ExploreResume
}

/** What the guest did on Explore. */
sealed interface ExploreIntent : UiIntent {
    data object SearchTapped : ExploreIntent
    data class LengthTapped(val length: StayLength) : ExploreIntent
    data class CityTapped(val id: CityId) : ExploreIntent
    data object SeeAllCitiesTapped : ExploreIntent
    data object ResumeTapped : ExploreIntent
    data object RetryTapped : ExploreIntent
}

/** Explore's one-off outputs. */
sealed interface ExploreEffect : UiEffect {
    data class Navigate(val key: SojournNavKey) : ExploreEffect
}
