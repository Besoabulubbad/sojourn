package com.abulubad.sojourn.feature.details

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin module: [DetailsViewModel] and [GalleryViewModel], each created with its key as a parameter. */
val detailsModule: Module = module {
    viewModelOf(::DetailsViewModel)
    viewModelOf(::GalleryViewModel)
}
