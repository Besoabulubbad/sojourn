package com.abulubad.sojourn.core.data

import com.abulubad.sojourn.core.data.local.id
import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.BedroomChoice
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.MoneyRange
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.SortOrder
import com.abulubad.sojourn.core.model.ThemeMode
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The guest's preferences that outlive a screen: the filter sheet's state, so filters chosen once still apply after
 * a restart, and the app's appearance. Stored in key-value settings, not the database: small, read synchronously
 * on start.
 */
interface PreferencesRepository {

    /** The current filters; [Filters.None] until the guest sets any. */
    val filters: StateFlow<Filters>

    fun setFilters(filters: Filters)

    /** System, Light or Dark; [ThemeMode.Default] until the guest chooses. */
    val themeMode: StateFlow<ThemeMode>

    fun setThemeMode(mode: ThemeMode)
}

internal class SettingsPreferencesRepository(private val settings: Settings) : PreferencesRepository {

    private val state = MutableStateFlow(read())
    override val filters: StateFlow<Filters> = state.asStateFlow()

    private val theme = MutableStateFlow(
        settings.getStringOrNull(THEME_MODE)?.let { id -> ThemeMode.entries.find { it.id == id } } ?: ThemeMode.Default,
    )
    override val themeMode: StateFlow<ThemeMode> = theme.asStateFlow()

    override fun setFilters(filters: Filters) {
        write(filters)
        state.value = filters
    }

    override fun setThemeMode(mode: ThemeMode) {
        settings.putString(THEME_MODE, mode.id)
        theme.value = mode
    }

    private fun read(): Filters = Filters(
        priceRange = readPriceRange(),
        basis = settings.getStringOrNull(BASIS)?.let { id -> PriceBasis.entries.find { it.id == id } }
            ?: Filters.None.basis,
        amenities = settings.getString(AMENITIES, "").split(SEPARATOR)
            .mapNotNullTo(LinkedHashSet()) { id -> Amenity.entries.find { it.id == id } },
        bedrooms = settings.getStringOrNull(BEDROOMS)?.let { id -> BedroomChoice.entries.find { it.id == id } },
        minBeds = settings.getIntOrNull(MIN_BEDS)?.takeIf { it > 1 },
        minBathrooms = settings.getIntOrNull(MIN_BATHROOMS)?.takeIf { it > 1 },
        instantBook = settings.getBoolean(INSTANT_BOOK, false),
        freeCancellation = settings.getBoolean(FREE_CANCELLATION, false),
        sort = settings.getStringOrNull(SORT)?.let { id -> SortOrder.entries.find { it.id == id } }
            ?: Filters.None.sort,
    )

    private fun readPriceRange(): MoneyRange? {
        val code = settings.getStringOrNull(PRICE_CURRENCY)
        val currency = Currency.entries.find { it.code == code } ?: return null
        val min = settings.getLongOrNull(PRICE_MIN)
        val max = settings.getLongOrNull(PRICE_MAX)
        val valid = min != null && max != null && min <= max
        return if (valid) MoneyRange(Money(min, currency), Money(max, currency)) else null
    }

    private fun write(filters: Filters) {
        val range = filters.priceRange
        if (range == null) {
            listOf(PRICE_MIN, PRICE_MAX, PRICE_CURRENCY).forEach(settings::remove)
        } else {
            settings.putLong(PRICE_MIN, range.min.minor)
            settings.putLong(PRICE_MAX, range.max.minor)
            settings.putString(PRICE_CURRENCY, range.min.currency.code)
        }
        settings.putString(BASIS, filters.basis.id)
        settings.putString(AMENITIES, filters.amenities.joinToString(SEPARATOR) { it.id })
        filters.bedrooms?.let { settings.putString(BEDROOMS, it.id) } ?: settings.remove(BEDROOMS)
        filters.minBeds?.let { settings.putInt(MIN_BEDS, it) } ?: settings.remove(MIN_BEDS)
        filters.minBathrooms?.let { settings.putInt(MIN_BATHROOMS, it) } ?: settings.remove(MIN_BATHROOMS)
        settings.putBoolean(INSTANT_BOOK, filters.instantBook)
        settings.putBoolean(FREE_CANCELLATION, filters.freeCancellation)
        settings.putString(SORT, filters.sort.id)
    }

    private companion object {
        const val SEPARATOR = ","
        const val PRICE_MIN = "filters.price_min"
        const val PRICE_MAX = "filters.price_max"
        const val PRICE_CURRENCY = "filters.price_currency"
        const val BASIS = "filters.basis"
        const val AMENITIES = "filters.amenities"
        const val BEDROOMS = "filters.bedrooms"
        const val MIN_BEDS = "filters.min_beds"
        const val MIN_BATHROOMS = "filters.min_bathrooms"
        const val INSTANT_BOOK = "filters.instant_book"
        const val FREE_CANCELLATION = "filters.free_cancellation"
        const val SORT = "filters.sort"
        const val THEME_MODE = "appearance.theme_mode"
    }
}
