package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.common.Loadable
import com.abulubad.sojourn.core.data.SavedRepository
import com.abulubad.sojourn.core.data.StayRepository
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Rating
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** What the Details screen shows: the stay, its rating if shown (N-04), the dates already booked, and the heart. */
data class StayDetails(val stay: Stay, val rating: Rating?, val blocked: List<DateRange>, val saved: Boolean)

/**
 * One stay for Details, offline-first: the cached stay at once, then the refreshed one with its blocked dates.
 * A stay the catalogue no longer has ends as `AppError.NotFound`, which the screen shows as "no longer available".
 */
class GetStayDetails(private val stays: StayRepository, private val saved: SavedRepository) {
    operator fun invoke(id: StayId, force: Boolean = false): Flow<Loadable<StayDetails>> {
        val read = combine(stays.observeStay(id), stays.observeBlockedDates(id), saved.observeSavedIds()) {
                stay, blocked, savedIds ->
            stay?.let { StayDetails(it, StayRules.visibleRating(it.rating), blocked, saved = it.id in savedIds) }
        }
        return offlineFirst(read, { it != null }) { stays.refreshStay(id, force) }
            .map { Loadable(it.data, it.refreshing, it.error) }
    }
}
