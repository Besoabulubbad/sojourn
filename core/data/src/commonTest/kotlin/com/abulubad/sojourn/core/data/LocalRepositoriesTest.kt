package com.abulubad.sojourn.core.data

import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.BedroomChoice
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.MoneyRange
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.SortOrder
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.model.StayLength
import com.abulubad.sojourn.core.model.ThemeMode
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/** The repositories that never touch the network: saved stays, recent searches and preferences. */
class LocalRepositoriesTest {

    @Test
    fun `toggling saves then un-saves and the newest save comes first`() = runDataTest {
        assertTrue(saved.toggle(StayId("a")))
        clock.advance(1.minutes)
        saved.setSaved(StayId("b"), saved = true)
        assertEquals(listOf(StayId("b"), StayId("a")), saved.observeSavedIds().first())
        assertFalse(saved.toggle(StayId("a")))
        assertEquals(listOf(StayId("b")), saved.observeSavedIds().first())
    }

    @Test
    fun `a save keeps its search and comes back as it was after undo`() = runDataTest {
        val march = DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 27))
        saved.toggle(StayId("a"), march, Guests(adults = 2, children = 1))
        saved.toggle(StayId("b"))
        val entry = saved.observeSaved().first().first { it.stayId == StayId("a") }
        assertEquals(march, entry.dates)
        assertEquals(Guests(adults = 2, children = 1), entry.guests)
        assertEquals(entry, saved.remove(StayId("a")))
        assertEquals(listOf(StayId("b")), saved.observeSavedIds().first())
        saved.restore(entry)
        assertEquals(entry, saved.observeSaved().first().first { it.stayId == StayId("a") })
    }

    @Test
    fun `saved stays survive a catalogue refresh`() = runDataTest {
        val id = catalogueStays().first().id
        saved.setSaved(id, saved = true)
        stays.refresh()
        assertEquals(listOf(id), saved.observeSavedIds().first())
    }

    @Test
    fun `a recent search round-trips and an identical one moves to the top`() = runDataTest {
        val paris = SearchQuery(
            cityId = CityId("paris"),
            dates = DateRange(LocalDate(2027, 3, 12), LocalDate(2027, 4, 12)),
            guests = Guests(adults = 2, children = 1),
            length = StayLength.Month,
        )
        val anywhere = SearchQuery()
        val flexible = SearchQuery(length = StayLength.Month, startMonth = YearMonth(2027, 3))
        searches.record(paris)
        clock.advance(1.minutes)
        searches.record(anywhere)
        clock.advance(1.minutes)
        searches.record(flexible)
        clock.advance(1.minutes)
        searches.record(paris)
        assertEquals(listOf(paris, flexible, anywhere), searches.observeRecent().first())
    }

    @Test
    fun `only the newest ten searches are kept`() = runDataTest {
        repeat(12) { adults ->
            searches.record(SearchQuery(guests = Guests(adults = adults + 1)))
            clock.advance(1.minutes)
        }
        val kept = searches.observeRecent(limit = 20).first()
        assertEquals(SearchRepository.KEEP, kept.size)
        assertEquals(12, kept.first().guests.adults)
        assertEquals(SearchRepository.DEFAULT_LIMIT, searches.observeRecent().first().size)
        searches.clear()
        assertTrue(searches.observeRecent().first().isEmpty())
    }

    @Test
    fun `filters start empty and survive a restart`() {
        val settings = MapSettings()
        assertEquals(Filters.None, SettingsPreferencesRepository(settings).filters.value)
        val chosen = Filters(
            priceRange = MoneyRange(Money.of(1_500, Currency.GBP), Money.of(3_000, Currency.GBP)),
            basis = PriceBasis.Month,
            amenities = setOf(Amenity.Wifi, Amenity.Workspace),
            bedrooms = BedroomChoice.ThreeOrMore,
            minBeds = 3,
            minBathrooms = 2,
            instantBook = true,
            sort = SortOrder.PriceLowToHigh,
        )
        SettingsPreferencesRepository(settings).setFilters(chosen)
        assertEquals(chosen, SettingsPreferencesRepository(settings).filters.value)
    }

    @Test
    fun `the appearance survives a restart and starts as the device's`() {
        val settings = MapSettings()
        assertEquals(ThemeMode.System, SettingsPreferencesRepository(settings).themeMode.value)
        SettingsPreferencesRepository(settings).setThemeMode(ThemeMode.Dark)
        assertEquals(ThemeMode.Dark, SettingsPreferencesRepository(settings).themeMode.value)
    }

    @Test
    fun `clearing filters clears them on the next start too`() {
        val settings = MapSettings()
        val repository = SettingsPreferencesRepository(settings)
        repository.setFilters(Filters(priceRange = MoneyRange(Money.of(1, Currency.EUR), Money.of(2, Currency.EUR))))
        repository.setFilters(Filters.None)
        assertEquals(Filters.None, repository.filters.value)
        assertEquals(Filters.None, SettingsPreferencesRepository(settings).filters.value)
    }
}
