package com.abulubad.sojourn.core.database

import org.koin.core.module.Module

/** Koin module: the [DatabaseDriverFactory] for the platform and the one [SojournDatabase]. */
expect val databaseModule: Module
