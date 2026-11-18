package com.abulubad.sojourn.core.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.DispatcherProvider
import com.abulubad.sojourn.core.database.Saved
import com.abulubad.sojourn.core.database.SojournDatabase
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.SavedEntry
import com.abulubad.sojourn.core.model.StayId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/**
 * Stays the guest saved, each with the search it was saved from. Saving is local: it works offline, is instant, and
 * survives a catalogue refresh (a saved stay that leaves the catalogue simply stops appearing).
 */
interface SavedRepository {

    /** Saved stay ids, the most recently saved first. */
    fun observeSavedIds(): Flow<List<StayId>>

    /** Saved stays with their searches, the most recently saved first. */
    fun observeSaved(): Flow<List<SavedEntry>>

    /** Saves [id] with no search (undated, one adult), or un-saves it. */
    suspend fun setSaved(id: StayId, saved: Boolean)

    /**
     * Flips [id]'s saved state, saving it with [dates] and [guests] (the search the heart was tapped in), and returns
     * the new state (the heart's pop plays only when it becomes saved).
     */
    suspend fun toggle(id: StayId, dates: DateRange? = null, guests: Guests = Guests()): Boolean

    /** Un-saves [id] and returns what was saved, so "Undo" can put it back as it was; null if it was not saved. */
    suspend fun remove(id: StayId): SavedEntry?

    /** Saves [entry] as it is, with its own time: "Undo" returns a stay to its place in the list. */
    suspend fun restore(entry: SavedEntry)
}

internal class DatabaseSavedRepository(
    private val db: SojournDatabase,
    private val clock: AppClock,
    private val dispatchers: DispatcherProvider,
) : SavedRepository {

    private val queries get() = db.savedQueries

    override fun observeSavedIds(): Flow<List<StayId>> = observeSaved().map { list -> list.map { it.stayId } }

    override fun observeSaved(): Flow<List<SavedEntry>> =
        queries.all().asFlow().mapToList(dispatchers.io).map { rows -> rows.mapNotNull { it.toEntry() } }

    override suspend fun setSaved(id: StayId, saved: Boolean) {
        if (saved) restore(SavedEntry(id, dates = null, guests = Guests(), savedAt = clock.now())) else remove(id)
    }

    override suspend fun toggle(id: StayId, dates: DateRange?, guests: Guests): Boolean =
        withContext(dispatchers.io) {
            db.transactionWithResult {
                val nowSaved = !queries.isSaved(id.value).executeAsOne()
                if (nowSaved) write(SavedEntry(id, dates, guests, clock.now())) else queries.remove(id.value)
                nowSaved
            }
        }

    override suspend fun remove(id: StayId): SavedEntry? = withContext(dispatchers.io) {
        db.transactionWithResult {
            val entry = queries.byId(id.value).executeAsOneOrNull()?.toEntry()
            queries.remove(id.value)
            entry
        }
    }

    override suspend fun restore(entry: SavedEntry) {
        withContext(dispatchers.io) { write(entry) }
    }

    private fun write(entry: SavedEntry) = queries.save(
        stay_id = entry.stayId.value,
        saved_at = entry.savedAt.toEpochMilliseconds(),
        check_in = entry.dates?.start?.toString(),
        check_out = entry.dates?.end?.toString(),
        adults = entry.guests.adults.toLong(),
        children = entry.guests.children.toLong(),
    )
}

/** A row as a [SavedEntry]; a row with unreadable dates keeps the save but drops the dates. */
private fun Saved.toEntry(): SavedEntry? {
    val dates = runCatching {
        val start = check_in?.let(LocalDate::parse)
        val end = check_out?.let(LocalDate::parse)
        if (start != null && end != null) DateRange(start, end) else null
    }.getOrNull()
    val guests = runCatching { Guests(adults.toInt(), children.toInt()) }.getOrNull() ?: return null
    return SavedEntry(StayId(stay_id), dates, guests, Instant.fromEpochMilliseconds(saved_at))
}
