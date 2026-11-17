package com.abulubad.sojourn.feature.explore

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin module: [ExploreViewModel]. */
val exploreModule: Module = module {
    viewModelOf(::ExploreViewModel)
}
