package com.abulubad.sojourn.core.localization

import org.koin.core.module.Module

/**
 * Koin module: the persisted [AppLocaleController] and the platform's [PlatformLocaleSync]. Platform actuals
 * supply the `Settings` store (SharedPreferences / NSUserDefaults).
 */
expect val localizationModule: Module
