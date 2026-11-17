package com.abulubad.sojourn.core.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.common.DispatcherProvider
import com.abulubad.sojourn.core.common.map
import com.abulubad.sojourn.core.data.local.toModel
import com.abulubad.sojourn.core.data.local.toRow
import com.abulubad.sojourn.core.database.SojournDatabase
import com.abulubad.sojourn.core.model.City
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.network.SojournApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * The catalogue's cities. Reads come from the database and re-emit on change (ADR 0007); [refresh] pulls from the
 * API and writes back, so a failed refresh leaves the last cities on screen.
 */
interface CityRepository {

    /** Cities, the ones with most stays first. Empty until the first successful refresh. */
    fun observeCities(): Flow<List<City>>

    fun observeCity(id: CityId): Flow<City?>

    /** Fetches the cities unless they are fresh ([RefreshPolicy]); [force] always fetches. */
    suspend fun refresh(force: Boolean = false): AppResult<Unit>
}

internal class OfflineFirstCityRepository(
    private val db: SojournDatabase,
    private val api: SojournApi,
    private val policy: RefreshPolicy,
    private val dispatchers: DispatcherProvider,
) : CityRepository {

    private val queries get() = db.cityQueries

    override fun observeCities(): Flow<List<City>> =
        queries.all().asFlow().mapToList(dispatchers.io).map { rows -> rows.mapNotNull { it.toModel() } }

    override fun observeCity(id: CityId): Flow<City?> =
        queries.byId(id.value).asFlow().mapToOneOrNull(dispatchers.io).map { it?.toModel() }

    override suspend fun refresh(force: Boolean): AppResult<Unit> = policy.run(KEY, force) {
        api.cities().map { cities -> withContext(dispatchers.io) { replaceAll(cities) } }
    }

    private fun replaceAll(cities: List<City>) = db.transaction {
        val keep = cities.mapTo(HashSet()) { it.id.value }
        queries.all().executeAsList().map { it.id }.filterNot { it in keep }.forEach { queries.deleteById(it) }
        cities.forEach { queries.upsert(it.toRow()) }
    }

    private companion object {
        const val KEY = "cities"
    }
}
