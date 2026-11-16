package com.abulubad.sojourn.core.localization

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual val localizationModule: Module = module {
    single<Settings>(named(LOCALIZATION_SETTINGS)) { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
    single<PlatformLocaleSync> { IosLocaleSync() }
    single<AppLocaleController> {
        SettingsAppLocaleController(settings = get(named(LOCALIZATION_SETTINGS)), platformSync = get())
    }
}
