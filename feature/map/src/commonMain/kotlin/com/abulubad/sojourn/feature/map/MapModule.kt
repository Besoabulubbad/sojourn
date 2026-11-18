package com.abulubad.sojourn.feature.map

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin module: [MapViewModel], created with its key as a parameter. */
val mapModule: Module = module {
    viewModelOf(::MapViewModel)
}
