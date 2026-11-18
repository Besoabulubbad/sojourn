package com.abulubad.sojourn.core.data

import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.getOrNull
import com.abulubad.sojourn.core.data.local.StayStore
import com.abulubad.sojourn.core.database.sojournDatabase
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.network.local.FakeNetworkConditions
import com.abulubad.sojourn.core.network.local.LocalSojournApi
import com.abulubad.sojourn.core.testing.TestDispatcherProvider
import com.abulubad.sojourn.core.testing.runSojournTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * The real data stack for a test: an in-memory [com.abulubad.sojourn.core.database.SojournDatabase], the bundled
 * catalogue behind [LocalSojournApi] with no latency, and a clock the test moves. Repositories are built the way
 * `dataModule` builds them.
 */
class DataTestEnv(val dispatchers: TestDispatcherProvider) {

    val clock = MutableClock()
    val conditions = FakeNetworkConditions(latency = Duration.ZERO)
    val api = LocalSojournApi(conditions = conditions, clock = clock)
    private val driver = inMemoryDriver()
    val db = sojournDatabase(driver)
    internal val store = StayStore(db, dispatchers)
    private val policy = RefreshPolicy(clock)

    val cities: CityRepository = OfflineFirstCityRepository(db, api, policy, dispatchers)
    val stays: StayRepository = OfflineFirstStayRepository(store, api, policy, dispatchers)
    val pricing: PricingRepository = OfflineFirstPricingRepository(db, api, clock, dispatchers)
    val bookings: BookingRepository = OfflineFirstBookingRepository(db, api, dispatchers)
    val saved: SavedRepository = DatabaseSavedRepository(db, clock, dispatchers)
    val searches: SearchRepository = DatabaseSearchRepository(db, clock, dispatchers)

    /** Every stay the catalogue serves, straight from the API, in id order (the database's order). */
    suspend fun catalogueStays(): List<Stay> = api.stays().getOrNull().orEmpty().sortedBy { it.id.value }

    fun close() = driver.close()

    companion object {
        /** After every seeded booked range, so any stay quotes. */
        val QUOTE_START = LocalDate(2027, 7, 1)

        /** Three months: above every stay's minimum stay. */
        fun quarter(start: LocalDate = QUOTE_START): DateRange = DateRange(start, start.plus(DatePeriod(days = 90)))
    }
}

/** A clock tests move by hand. */
class MutableClock(var now: Instant = Instant.parse("2026-11-17T09:00:00Z")) : AppClock {
    override val timeZone: TimeZone = TimeZone.UTC
    override fun now(): Instant = now
    fun advance(by: Duration) {
        now += by
    }
}

/** Runs [body] with a fresh [DataTestEnv], closing its database afterwards. */
fun runDataTest(body: suspend DataTestEnv.() -> Unit) = runSojournTest { dispatchers ->
    val env = DataTestEnv(dispatchers)
    try {
        env.body()
    } finally {
        env.close()
    }
}
