package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.data.SavedRepository
import com.abulubad.sojourn.core.data.StayRepository
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.StayId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/** Flips a stay's heart; returns the new state, so the pop plays only when it becomes saved. Works offline. */
class ToggleSaved(private val saved: SavedRepository) {
    suspend operator fun invoke(id: StayId): Boolean = saved.toggle(id)
}

/**
 * The Saved tab: saved stays, the most recently saved first, as cards with monthly "from" prices (P-03). Local, so
 * it works offline; a saved stay no longer in the catalogue is left out.
 */
class ObserveSaved(private val saved: SavedRepository, private val stays: StayRepository) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<StayResult>> = saved.observeSavedIds().flatMapLatest { ids ->
        stays.observeStays(ids.toSet()).map { cached ->
            val byId = cached.associateBy { it.id }
            ids.mapNotNull { byId[it] }.map { stay ->
                StayResult(
                    stay = stay,
                    price = StayRules.fromPrice(stay, PriceBasis.Month),
                    rating = StayRules.visibleRating(stay.rating),
                    saved = true,
                )
            }
        }
    }
}
