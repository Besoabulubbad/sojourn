package com.abulubad.sojourn.core.database

import androidx.test.platform.app.InstrumentationRegistry
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual fun inMemoryDriver(): SqlDriver = AndroidSqliteDriver(
    schema = SojournDatabase.Schema,
    context = InstrumentationRegistry.getInstrumentation().targetContext,
    name = null,
).also { it.execute(null, "PRAGMA foreign_keys = ON", 0) }
