package com.abulubad.sojourn.core.localization

import android.content.Context
import android.content.res.Resources
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
        SettingsAppLocaleController(
            settings = get(named(LOCALIZATION_SETTINGS)),
            platformSync = get(),
            // Until the guest chooses, the phone's own language: an Arabic phone opens the app in Arabic. Read from
            // the system configuration, which the in-app language never changes.
            initialTag = Resources.getSystem().configuration.locales.get(0)?.toLanguageTag(),
        )
    }
}
