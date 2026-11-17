package com.abulubad.sojourn.core.network.ktor

import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.errorOrNull
import com.abulubad.sojourn.core.common.getOrNull
import com.abulubad.sojourn.core.network.CatalogJson
import com.abulubad.sojourn.core.network.TestCatalog
import com.abulubad.sojourn.core.network.dto.CatalogDto
import com.abulubad.sojourn.core.testing.FixedAppClock
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class KtorSojournApiTest {

    private val json = CatalogJson.format.encodeToString(CatalogDto.serializer(), TestCatalog.catalog)

    private fun api(engine: MockEngine) =
        KtorSojournApi(HttpClient(engine) { expectSuccess = true }, FixedAppClock(), baseUrl = "https://site/")

    @Test
    fun `fetches the catalogue once from the docs site and serves every call from it`() = runTest {
        var requests = 0
        val api = api(
            MockEngine { request ->
                requests++
                assertEquals("https://site/catalog/catalog.json", request.url.toString())
                respond(json)
            },
        )
        assertEquals(2, assertNotNull(api.stays().getOrNull()).size)
        val photo = assertNotNull(api.stays().getOrNull()).first().photos.first()
        assertEquals("https://site/catalog/photos/a.jpg", photo.url)
        assertEquals(1, requests)
    }

    @Test
    fun `no connection is offline and an HTTP error is remote`() = runTest {
        assertEquals(AppError.Offline, api(MockEngine { throw IOException("no route") }).cities().errorOrNull())
        assertEquals(
            AppError.Remote(HttpStatusCode.ServiceUnavailable.value),
            api(MockEngine { respondError(HttpStatusCode.ServiceUnavailable) }).cities().errorOrNull(),
        )
    }
}
