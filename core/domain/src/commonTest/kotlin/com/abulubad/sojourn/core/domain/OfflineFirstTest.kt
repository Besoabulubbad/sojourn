package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.common.Loadable
import com.abulubad.sojourn.core.model.City
import com.abulubad.sojourn.core.testing.StayFixtures
import com.abulubad.sojourn.core.testing.data.FakeCityRepository
import com.abulubad.sojourn.core.testing.runSojournTest
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/** The offline-first rule, through [GetCities]: what a screen sees for each cache and network combination. */
class OfflineFirstTest {

    private val london = StayFixtures.london

    /** A repository whose refresh takes a second, so the state while it runs is observable. */
    private fun repository(remote: List<City> = listOf(london), cached: List<City> = emptyList()) =
        FakeCityRepository(remote, cached).apply { latency = 1.seconds }

    @Test
    fun `the first launch loads and then shows what the refresh brought`() = runSojournTest {
        val cities = GetCities(repository())()
        assertEquals(Loadable.Loading, cities.first())
        assertEquals(Loadable(listOf(london)), cities.first { !it.refreshing })
    }

    @Test
    fun `cached content shows at once and stays when the refresh fails`() = runSojournTest {
        val offline = repository(cached = listOf(london)).apply { refreshResult = AppResult.Failure(AppError.Offline) }
        val cities = GetCities(offline)()
        assertEquals(Loadable(listOf(london), refreshing = true), cities.first())
        assertEquals(Loadable(listOf(london), error = AppError.Offline), cities.first { !it.refreshing })
    }

    @Test
    fun `nothing cached and an offline refresh is offline with no data`() = runSojournTest {
        val repository = FakeCityRepository().apply { refreshResult = AppResult.Failure(AppError.Offline) }
        assertEquals(Loadable(null, error = AppError.Offline), GetCities(repository)().first { !it.refreshing })
    }

    @Test
    fun `an empty catalogue is empty only once a refresh confirms it`() = runSojournTest {
        val cities = GetCities(repository(remote = emptyList()))()
        assertEquals(null, cities.first().data, "not empty yet: nothing has been fetched")
        assertEquals(Loadable(emptyList()), cities.first { !it.refreshing })
    }

    @Test
    fun `retry with force refreshes again`() = runSojournTest {
        val repository = FakeCityRepository(remote = listOf(london))
        GetCities(repository)().first { !it.refreshing }
        GetCities(repository)(force = true).first { !it.refreshing }
        assertEquals(2, repository.refreshCalls)
    }
}
