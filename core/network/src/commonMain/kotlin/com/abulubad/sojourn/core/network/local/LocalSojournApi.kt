package com.abulubad.sojourn.core.network.local

import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.model.Booking
import com.abulubad.sojourn.core.model.City
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Quote
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.network.CatalogEngine
import com.abulubad.sojourn.core.network.CatalogJson
import com.abulubad.sojourn.core.network.SojournApi
import com.abulubad.sojourn.core.network.dto.CatalogDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The default [SojournApi]: the seed catalogue bundled with the app, behind [conditions] (latency and injectable
 * failures). Photo URLs point at the docs site, so images still load over the network and cache like a real app.
 */
class LocalSojournApi(
    private val conditions: FakeNetworkConditions,
    clock: AppClock,
    catalog: CatalogDto = CatalogJson.parse(BundledCatalog.json),
    photoBaseUrl: String = DEFAULT_PHOTO_BASE_URL,
) : SojournApi {

    private val engine = CatalogEngine(catalog.copy(photoBase = photoBaseUrl + catalog.photoBase), clock)
    private val lock = Mutex()

    override suspend fun cities(): AppResult<List<City>> = call { engine.cities() }

    override suspend fun stays(cityId: CityId?): AppResult<List<Stay>> = call { engine.stays(cityId) }

    override suspend fun stay(id: StayId): AppResult<Stay> = call { engine.stay(id) }

    override suspend fun blockedDates(id: StayId): AppResult<List<DateRange>> = call { engine.blockedDates(id) }

    override suspend fun quote(stayId: StayId, dates: DateRange, guests: Guests): AppResult<Quote> =
        call { engine.quote(stayId, dates, guests) }

    override suspend fun book(quoteId: QuoteId): AppResult<Booking> = call { engine.book(quoteId) }

    private suspend fun <T> call(block: () -> AppResult<T>): AppResult<T> {
        delay(conditions.latency)
        conditions.nextFailure()?.let { return AppResult.Failure(it) }
        return lock.withLock { block() }
    }

    companion object {
        /** Where the catalogue's photos are published: the project's GitHub Pages site. */
        const val DEFAULT_PHOTO_BASE_URL = "https://besoabulubbad.github.io/sojourn/"
    }
}
