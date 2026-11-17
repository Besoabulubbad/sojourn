package com.abulubad.sojourn.core.network

import com.abulubad.sojourn.core.network.local.FakeNetworkConditions
import com.abulubad.sojourn.core.network.local.LocalSojournApi
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module: [SojournApi] bound to [LocalSojournApi] with shared [FakeNetworkConditions]. The Ktor
 * implementation is constructed by `app:shared` when its flag is on.
 */
val networkModule: Module = module {
    single { FakeNetworkConditions() }
    single<SojournApi> { LocalSojournApi(conditions = get(), clock = get()) }
}
