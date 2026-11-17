package com.abulubad.sojourn.core.data

import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.common.DispatcherProvider
import com.abulubad.sojourn.core.data.local.StayStore
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.network.SojournApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Stays, from the database (ADR 0007). Lists refresh in bulk; a stay's details screen refreshes that one stay and
 * its blocked dates. A failed refresh never clears what is on screen: the caller shows the cache with the offline
 * banner, or the offline state when there is no cache.
 */
interface StayRepository {

    /** All stays, or one city's, in a stable order. */
    fun observeStays(cityId: CityId? = null): Flow<List<Stay>>

    /** The stays with these ids that exist, in a stable order (Saved). */
    fun observeStays(ids: Set<StayId>): Flow<List<Stay>>

    /** One stay, or null when it is not in the database (not fetched yet, or gone from the catalogue). */
    fun observeStay(id: StayId): Flow<Stay?>

    /** Ranges the stay is already booked for, as of its last refresh; the date picker strikes them through. */
    fun observeBlockedDates(id: StayId): Flow<List<DateRange>>

    /** True when the database holds stays in this scope, so a failed refresh can still show content. */
    suspend fun hasCached(cityId: CityId? = null): Boolean

    /** Fetches the stays in scope and replaces them, unless fresh. */
    suspend fun refresh(cityId: CityId? = null, force: Boolean = false): AppResult<Unit>

    /** Fetches one stay and its blocked dates, unless fresh. A stay the catalogue no longer has is removed. */
    suspend fun refreshStay(id: StayId, force: Boolean = false): AppResult<Unit>
}

internal class OfflineFirstStayRepository(
    private val store: StayStore,
    private val api: SojournApi,
    private val policy: RefreshPolicy,
    private val dispatchers: DispatcherProvider,
) : StayRepository {

    override fun observeStays(cityId: CityId?): Flow<List<Stay>> = store.observeAll(cityId)

    override fun observeStays(ids: Set<StayId>): Flow<List<Stay>> = store.observeByIds(ids)

    override fun observeStay(id: StayId): Flow<Stay?> = store.observe(id)

    override fun observeBlockedDates(id: StayId): Flow<List<DateRange>> = store.observeBlocked(id)

    override suspend fun hasCached(cityId: CityId?): Boolean = withContext(dispatchers.io) { store.hasAny(cityId) }

    override suspend fun refresh(cityId: CityId?, force: Boolean): AppResult<Unit> =
        policy.run("stays:${cityId?.value ?: "all"}", force) {
            when (val result = api.stays(cityId)) {
                is AppResult.Success -> {
                    withContext(dispatchers.io) { store.replaceScope(cityId, result.value) }
                    AppResult.Success(Unit)
                }
                is AppResult.Failure -> result
            }
        }

    override suspend fun refreshStay(id: StayId, force: Boolean): AppResult<Unit> =
        policy.run("stay:${id.value}", force) {
            val stay = when (val result = api.stay(id)) {
                is AppResult.Success -> result.value
                is AppResult.Failure -> {
                    // Gone from the catalogue: drop it so the screen shows "no longer available", not stale data.
                    if (result.error is AppError.NotFound) withContext(dispatchers.io) { store.delete(id) }
                    return@run result
                }
            }
            val blocked = when (val result = api.blockedDates(id)) {
                is AppResult.Success -> result.value
                is AppResult.Failure -> return@run result
            }
            withContext(dispatchers.io) { store.write(stay, blocked) }
            AppResult.Success(Unit)
        }
}
