package com.abulubad.sojourn.core.localization

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val localizationModule: Module = module {
    single<Settings>(named(LOCALIZATION_SETTINGS)) {
        SharedPreferencesSettings(androidContext().getSharedPreferences(LOCALIZATION_SETTINGS, Context.MODE_PRIVATE))
    }
    single<PlatformLocaleSync> { AndroidLocaleSync(androidContext()) }
    single<AppLocaleController> {
        SettingsAppLocaleController(settings = get(named(LOCALIZATION_SETTINGS)), platformSync = get())
    }
}
