package com.abulubad.sojourn.core.data

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal actual val platformDataModule: Module = module {
    single<Settings>(named(DATA_SETTINGS)) {
        SharedPreferencesSettings(androidContext().getSharedPreferences(DATA_SETTINGS, Context.MODE_PRIVATE))
    }
}
