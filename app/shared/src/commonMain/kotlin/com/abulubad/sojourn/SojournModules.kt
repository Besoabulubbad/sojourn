package com.abulubad.sojourn

import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.DefaultDispatcherProvider
import com.abulubad.sojourn.core.common.DispatcherProvider
import com.abulubad.sojourn.core.common.SystemAppClock
import com.abulubad.sojourn.core.data.dataModule
import com.abulubad.sojourn.core.database.databaseModule
import com.abulubad.sojourn.core.domain.domainModule
import com.abulubad.sojourn.core.localization.localizationModule
import com.abulubad.sojourn.core.network.networkModule
import com.abulubad.sojourn.core.platform.platformModule
import com.abulubad.sojourn.feature.explore.exploreModule
import com.abulubad.sojourn.feature.filters.filtersModule
import com.abulubad.sojourn.feature.results.resultsModule
import com.abulubad.sojourn.feature.search.searchModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/** What every layer shares: the device clock and the production dispatchers. */
val appModule: Module = module {
    single<AppClock> { SystemAppClock() }
    single<DispatcherProvider> { DefaultDispatcherProvider() }
}

/**
 * Every Koin module the app starts with (ADR 0004). Feature modules join in Phase 5, when each feature gets its
 * first screen; `SojournModulesTest` verifies the whole graph on every PR.
 */
val sojournModules: List<Module> = listOf(
    appModule,
    localizationModule,
    platformModule,
    databaseModule,
    networkModule,
    dataModule,
    domainModule,
    exploreModule,
    searchModule,
    resultsModule,
    filtersModule,
)

/**
 * Starts Koin with [sojournModules]. Called once at process start: by `SojournApplication` on Android (which adds
 * `androidContext`) and by `iOSApp` through [startKoinIos] on iOS.
 */
fun initKoin(platform: KoinAppDeclaration = {}): KoinApplication = startKoin {
    platform()
    modules(sojournModules)
}
