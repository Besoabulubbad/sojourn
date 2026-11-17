package com.abulubad.sojourn.core.network.ktor

import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.AppError
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
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

/**
 * [SojournApi] over HTTP (ADR 0007, flag-gated): fetches the same catalogue the app bundles from
 * `<baseUrl>catalog/catalog.json` on the docs site, once per process, and prices quotes with the same engine as
 * [com.abulubad.sojourn.core.network.local.LocalSojournApi]. A real backend replaces the engine, not the seam.
 */
class KtorSojournApi(
    private val client: HttpClient,
    private val clock: AppClock,
    private val baseUrl: String,
) : SojournApi {

    private val lock = Mutex()
    private var engine: CatalogEngine? = null

    override suspend fun cities(): AppResult<List<City>> = call { it.cities() }

    override suspend fun stays(cityId: CityId?): AppResult<List<Stay>> = call { it.stays(cityId) }

    override suspend fun stay(id: StayId): AppResult<Stay> = call { it.stay(id) }

    override suspend fun blockedDates(id: StayId): AppResult<List<DateRange>> = call { it.blockedDates(id) }

    override suspend fun quote(stayId: StayId, dates: DateRange, guests: Guests): AppResult<Quote> =
        call { it.quote(stayId, dates, guests) }

    override suspend fun book(quoteId: QuoteId): AppResult<Booking> = call { it.book(quoteId) }

    private suspend fun <T> call(block: (CatalogEngine) -> AppResult<T>): AppResult<T> = lock.withLock {
        val loaded = engine ?: when (val fetched = fetch()) {
            is AppResult.Success -> fetched.value.also { engine = it }
            is AppResult.Failure -> return@withLock fetched
        }
        block(loaded)
    }

    private suspend fun fetch(): AppResult<CatalogEngine> = try {
        val catalog = CatalogJson.parse(client.get("${baseUrl}catalog/catalog.json").bodyAsText())
        AppResult.Success(CatalogEngine(catalog.copy(photoBase = baseUrl + catalog.photoBase), clock))
    } catch (e: CancellationException) {
        throw e
    } catch (e: ResponseException) {
        AppResult.Failure(AppError.Remote(e.response.status.value))
    } catch (expected: IOException) {
        // Any transport failure reads as offline to the UI; the cause carries nothing the user can act on.
        AppResult.Failure(AppError.Offline)
    } catch (e: SerializationException) {
        AppResult.Failure(AppError.Unexpected(e))
    }
}
