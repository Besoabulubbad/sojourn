package com.abulubad.sojourn.core.data

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

internal actual val platformDataModule: Module = module {
    single<Settings>(named(DATA_SETTINGS)) { NSUserDefaultsSettings(NSUserDefaults(suiteName = DATA_SETTINGS)) }
}
