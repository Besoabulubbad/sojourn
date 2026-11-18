package com.abulubad.sojourn.feature.search

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin module: [SearchViewModel], created with its `SearchKey` as a parameter. */
val searchModule: Module = module {
    viewModelOf(::SearchViewModel)
}
