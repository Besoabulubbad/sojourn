package com.abulubad.sojourn.core.database

import org.koin.core.module.Module
import org.koin.dsl.module

/** Android: the factory needs the application `Context`, which `app:android` registers with Koin. */
actual val databaseModule: Module = module {
    single { DatabaseDriverFactory(context = get()) }
    single { sojournDatabase(get<DatabaseDriverFactory>().create()) }
}
