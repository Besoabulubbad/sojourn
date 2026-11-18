package com.abulubad.sojourn.feature.filters

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin module: [FiltersViewModel], created with its `FiltersKey` as a parameter. */
val filtersModule: Module = module {
    viewModelOf(::FiltersViewModel)
}
