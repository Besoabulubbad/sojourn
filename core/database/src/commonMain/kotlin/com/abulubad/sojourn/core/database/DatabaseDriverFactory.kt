package com.abulubad.sojourn.core.database

import app.cash.sqldelight.db.SqlDriver

/** Opens the platform's SQLite driver for [SojournDatabase], with foreign keys on (the child tables cascade). */
expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}

/** The database file name on every platform. */
internal const val DATABASE_NAME = "sojourn.db"

/** Builds the database over a driver from [DatabaseDriverFactory] (or an in-memory driver in tests). */
fun sojournDatabase(driver: SqlDriver): SojournDatabase = SojournDatabase(driver)
