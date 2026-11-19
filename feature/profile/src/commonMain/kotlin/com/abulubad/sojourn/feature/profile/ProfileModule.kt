package com.abulubad.sojourn.feature.profile

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin module: the Profile tab's ViewModels. Needs `domainModule` and `localizationModule`. */
val profileModule: Module = module {
    viewModelOf(::ProfileViewModel)
    viewModelOf(::LanguageViewModel)
    viewModelOf(::AppearanceViewModel)
    viewModelOf(::RewardsViewModel)
}
