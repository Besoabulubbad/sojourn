package com.abulubad.sojourn.feature.checkout

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin module: [CheckoutViewModel] and [ConfirmationViewModel], each created with its key as a parameter. */
val checkoutModule: Module = module {
    viewModelOf(::CheckoutViewModel)
    viewModelOf(::ConfirmationViewModel)
}
