package com.abulubad.sojourn.core.database

import org.koin.core.module.Module
import org.koin.dsl.module

actual val databaseModule: Module = module {
    single { DatabaseDriverFactory() }
    single { sojournDatabase(get<DatabaseDriverFactory>().create()) }
}
