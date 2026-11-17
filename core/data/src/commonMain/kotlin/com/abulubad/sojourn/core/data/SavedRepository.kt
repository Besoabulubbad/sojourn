package com.abulubad.sojourn.core.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.DispatcherProvider
import com.abulubad.sojourn.core.database.SojournDatabase
import com.abulubad.sojourn.core.model.StayId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Stays the guest saved. Saving is local: it works offline, is instant, and survives a catalogue refresh (a saved
 * stay that leaves the catalogue simply stops appearing).
 */
interface SavedRepository {

    /** Saved stay ids, the most recently saved first. */
    fun observeSavedIds(): Flow<List<StayId>>

    /** Saves or un-saves [id]. */
    suspend fun setSaved(id: StayId, saved: Boolean)

    /** Flips [id]'s saved state and returns the new one (the heart's pop plays only when it becomes saved). */
    suspend fun toggle(id: StayId): Boolean
}

internal class DatabaseSavedRepository(
    private val db: SojournDatabase,
    private val clock: AppClock,
    private val dispatchers: DispatcherProvider,
) : SavedRepository {

    private val queries get() = db.savedQueries

    override fun observeSavedIds(): Flow<List<StayId>> =
        queries.all().asFlow().mapToList(dispatchers.io).map { rows -> rows.map { StayId(it.stay_id) } }

    override suspend fun setSaved(id: StayId, saved: Boolean) {
        withContext(dispatchers.io) {
            if (saved) queries.save(id.value, clock.now().toEpochMilliseconds()) else queries.remove(id.value)
        }
    }

    override suspend fun toggle(id: StayId): Boolean = withContext(dispatchers.io) {
        db.transactionWithResult {
            val nowSaved = !queries.isSaved(id.value).executeAsOne()
            if (nowSaved) queries.save(id.value, clock.now().toEpochMilliseconds()) else queries.remove(id.value)
            nowSaved
        }
    }
}
