package com.abulubad.sojourn.feature.results

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin module: [ResultsViewModel], created with its `ResultsKey` as a parameter. */
val resultsModule: Module = module {
    viewModelOf(::ResultsViewModel)
}
