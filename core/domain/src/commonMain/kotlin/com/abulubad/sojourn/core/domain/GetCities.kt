package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.common.Loadable
import com.abulubad.sojourn.core.data.CityRepository
import com.abulubad.sojourn.core.model.City
import kotlinx.coroutines.flow.Flow

/** The cities on Explore, most stays first, offline-first (ADR 0007). [force] bypasses the refresh policy (retry). */
class GetCities(private val cities: CityRepository) {
    operator fun invoke(force: Boolean = false): Flow<Loadable<List<City>>> =
        offlineFirst(cities.observeCities(), { it.isNotEmpty() }) { cities.refresh(force) }
}
