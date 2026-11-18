package com.abulubad.sojourn.feature.saved

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin module: [SavedViewModel]. */
val savedModule: Module = module {
    viewModelOf(::SavedViewModel)
}
