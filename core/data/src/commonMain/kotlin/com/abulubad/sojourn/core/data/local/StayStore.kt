package com.abulubad.sojourn.core.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.abulubad.sojourn.core.common.DispatcherProvider
import com.abulubad.sojourn.core.database.SojournDatabase
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Review
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.abulubad.sojourn.core.database.Stay as StayRow

/**
 * Reads and writes whole stays: the `stay` row and its photos, amenities, policies, inclusions and translations
 * (reviews are written with Details' refresh). A stay is written in one transaction, so a reader never sees a stay
 * without its children; reads re-emit when the stay rows change.
 */
internal class StayStore(private val db: SojournDatabase, private val dispatchers: DispatcherProvider) {

    private val queries get() = db.stayQueries

    fun observeAll(cityId: CityId?): Flow<List<Stay>> {
        val query = if (cityId == null) queries.all() else queries.inCity(cityId.value)
        return query.asFlow().mapToList(dispatchers.io).map(::assemble)
    }

    fun observeByIds(ids: Collection<StayId>): Flow<List<Stay>> =
        queries.byIds(ids.map { it.value }).asFlow().mapToList(dispatchers.io).map(::assemble)

    fun observe(id: StayId): Flow<Stay?> =
        queries.byId(id.value).asFlow().mapToOneOrNull(dispatchers.io).map { row ->
            row?.let { assemble(listOf(it)).firstOrNull() }
        }

    fun observeReviews(id: StayId): Flow<List<Review>> =
        queries.reviewsFor(id.value).asFlow().mapToList(dispatchers.io).map { rows -> rows.mapNotNull { it.toModel() } }

    fun observeBlocked(id: StayId): Flow<List<DateRange>> =
        queries.blockedFor(id.value).asFlow().mapToList(dispatchers.io).map { rows -> rows.map { it.toModel() } }

    fun hasAny(cityId: CityId?): Boolean = idsIn(cityId).isNotEmpty()

    fun has(id: StayId): Boolean = queries.byId(id.value).executeAsOneOrNull() != null

    /**
     * Makes the stays in scope ([cityId], or all of them when null) exactly [stays]: writes each and deletes the
     * ones the catalogue no longer has. Saved stays and bookings keep their ids; a deleted stay reads as missing.
     */
    fun replaceScope(cityId: CityId?, stays: List<Stay>) = db.transaction {
        val keep = stays.mapTo(HashSet()) { it.id.value }
        idsIn(cityId).filterNot { it in keep }.forEach { queries.deleteById(it) }
        stays.forEach(::writeOne)
    }

    fun delete(id: StayId) = queries.deleteById(id.value)

    /** Writes one stay with its blocked dates and, unless [reviews] is null (not fetched), its reviews. */
    fun write(stay: Stay, blocked: List<DateRange>, reviews: List<Review>? = null) = db.transaction {
        writeOne(stay)
        queries.clearBlocked(stay.id.value)
        blocked.forEach { queries.insertBlocked(it.toBlockedRow(stay.id)) }
        if (reviews != null) {
            queries.clearReviews(stay.id.value)
            reviews.forEachIndexed { position, review -> queries.insertReview(review.toRow(stay.id, position)) }
        }
    }

    private fun writeOne(stay: Stay) {
        val row = stay.toRow()
        queries.insertStayIfAbsent(row)
        with(row) {
            queries.updateStay(
                city_id, area_key, title_text, title_lang, description_text, description_lang, bedrooms, beds,
                bathrooms, sleeps, rating_score, rating_count, latitude, longitude, radius_metres, from_minor,
                from_currency, from_basis, from_excludes, walk_minutes, long_stay_discount_percent, instant_book, id,
            )
        }
        queries.clearChildren(stay.id.value)
        stay.photoRows().forEach(queries::insertPhoto)
        stay.amenityRows().forEach(queries::insertAmenity)
        stay.policyRows().forEach(queries::insertPolicy)
        stay.inclusionRows().forEach(queries::insertInclusion)
        stay.translationRows().forEach(queries::insertTranslation)
    }

    private fun idsIn(cityId: CityId?): List<String> =
        if (cityId == null) queries.allIds().executeAsList() else queries.idsInCity(cityId.value).executeAsList()

    /** Loads the children of [rows] in five queries (not five per stay) and builds the stays, in row order. */
    private fun assemble(rows: List<StayRow>): List<Stay> {
        if (rows.isEmpty()) return emptyList()
        val ids = rows.map { it.id }
        val photos = queries.photosFor(ids).executeAsList().groupBy { it.stay_id }
        val amenities = queries.amenitiesFor(ids).executeAsList().groupBy { it.stay_id }
        val policies = queries.policiesIn(ids).executeAsList().groupBy { it.stay_id }
        val inclusions = queries.inclusionsFor(ids).executeAsList().groupBy { it.stay_id }
        val translations = queries.translationsFor(ids).executeAsList().groupBy { it.stay_id }
        return rows.mapNotNull { row ->
            row.toModel(
                photos = photos[row.id].orEmpty(),
                amenities = amenities[row.id].orEmpty(),
                policies = policies[row.id].orEmpty(),
                inclusions = inclusions[row.id].orEmpty(),
                translations = translations[row.id].orEmpty(),
            )
        }
    }
}
