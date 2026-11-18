package com.abulubad.sojourn.core.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.DispatcherProvider
import com.abulubad.sojourn.core.data.local.toModel
import com.abulubad.sojourn.core.database.SojournDatabase
import com.abulubad.sojourn.core.model.SearchQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Recent searches for the search flow's "Recent" list. Local only; the newest [KEEP] are kept, without repeats. */
interface SearchRepository {

    /** The newest [limit] searches, newest first. */
    fun observeRecent(limit: Int = DEFAULT_LIMIT): Flow<List<SearchQuery>>

    /** Records [query] as the newest search; an identical earlier search moves to the top instead of repeating. */
    suspend fun record(query: SearchQuery)

    suspend fun clear()

    companion object {
        const val DEFAULT_LIMIT = 5
        const val KEEP = 10
    }
}

internal class DatabaseSearchRepository(
    private val db: SojournDatabase,
    private val clock: AppClock,
    private val dispatchers: DispatcherProvider,
) : SearchRepository {

    private val queries get() = db.searchRecentQueries

    override fun observeRecent(limit: Int): Flow<List<SearchQuery>> =
        queries.latest(limit.toLong()).asFlow().mapToList(dispatchers.io).map { rows -> rows.map { it.toModel() } }

    override suspend fun record(query: SearchQuery): Unit = withContext(dispatchers.io) {
        db.transaction {
            queries.latest(SearchRepository.KEEP.toLong()).executeAsList()
                .filter { it.toModel() == query }
                .forEach { queries.deleteById(it.id) }
            queries.insert(
                city_id = query.cityId?.value,
                start_date = query.dates?.start?.toString(),
                end_date = query.dates?.end?.toString(),
                adults = query.guests.adults.toLong(),
                children = query.guests.children.toLong(),
                length = query.length?.name,
                start_month = query.startMonth?.toString(),
                searched_at = clock.now().toEpochMilliseconds(),
            )
            queries.trimTo(SearchRepository.KEEP.toLong())
        }
    }

    override suspend fun clear() {
        withContext(dispatchers.io) { queries.clear() }
    }
}
