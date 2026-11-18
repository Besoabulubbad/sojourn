package com.abulubad.sojourn.core.data

import com.abulubad.sojourn.core.data.local.StayStore
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

/** Qualifier of the key-value [com.russhwolf.settings.Settings] that [PreferencesRepository] stores in. */
internal const val DATA_SETTINGS = "sojourn_preferences"

/** The platform's key-value settings, named [DATA_SETTINGS]. */
internal expect val platformDataModule: Module

/**
 * Koin module: every repository, bound to its interface. Needs `SojournDatabase` (`databaseModule`), `SojournApi`
 * (`networkModule`), and an `AppClock` and `DispatcherProvider`, which `app:shared` provides.
 */
val dataModule: Module = module {
    includes(platformDataModule)
    // Not singleOf: the constructor reference would make Koin resolve maxAge's Duration instead of its default.
    single { RefreshPolicy(clock = get()) }
    singleOf(::StayStore)
    singleOf(::OfflineFirstCityRepository) bind CityRepository::class
    singleOf(::OfflineFirstStayRepository) bind StayRepository::class
    singleOf(::OfflineFirstPricingRepository) bind PricingRepository::class
    singleOf(::OfflineFirstBookingRepository) bind BookingRepository::class
    singleOf(::DatabaseSavedRepository) bind SavedRepository::class
    singleOf(::DatabaseSearchRepository) bind SearchRepository::class
    single<PreferencesRepository> { SettingsPreferencesRepository(get(named(DATA_SETTINGS))) }
}
