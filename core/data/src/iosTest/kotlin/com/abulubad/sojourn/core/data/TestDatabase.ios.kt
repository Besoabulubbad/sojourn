package com.abulubad.sojourn.core.data

import app.cash.sqldelight.db.SqlDriver
import com.abulubad.sojourn.core.database.SojournDatabase
import app.cash.sqldelight.driver.native.inMemoryDriver as nativeInMemoryDriver

actual fun inMemoryDriver(): SqlDriver = nativeInMemoryDriver(SojournDatabase.Schema).also {
    it.execute(null, "PRAGMA foreign_keys = ON", 0)
}
