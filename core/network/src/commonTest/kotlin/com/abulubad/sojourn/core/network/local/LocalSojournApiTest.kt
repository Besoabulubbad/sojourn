package com.abulubad.sojourn.core.network.local

import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.errorOrNull
import com.abulubad.sojourn.core.common.getOrNull
import com.abulubad.sojourn.core.network.TestCatalog
import com.abulubad.sojourn.core.testing.FixedAppClock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LocalSojournApiTest {

    private val conditions = FakeNetworkConditions()
    private val api = LocalSojournApi(conditions, FixedAppClock(), TestCatalog.catalog, photoBaseUrl = "https://x/")

    @Test
    fun `online calls succeed after the simulated latency and with absolute photo URLs`() = runTest {
        val stays = assertNotNull(api.stays().getOrNull())
        assertEquals(2, stays.size)
        assertEquals("https://x/catalog/photos/a.jpg", stays.first().photos.first().url)
    }

    @Test
    fun `offline and server error modes fail every call`() = runTest {
        conditions.mode = FakeNetworkConditions.Mode.Offline
        assertEquals(AppError.Offline, api.cities().errorOrNull())
        conditions.mode = FakeNetworkConditions.Mode.ServerError
        assertEquals(AppError.Remote(503), api.cities().errorOrNull())
    }

    @Test
    fun `flaky mode fails every third call`() = runTest {
        conditions.mode = FakeNetworkConditions.Mode.Flaky
        val results = List(6) { api.cities().errorOrNull() }
        assertEquals(listOf(null, null, AppError.Offline, null, null, AppError.Offline), results)
        assertNull(api.cities().errorOrNull())
    }
}
