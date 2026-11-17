package com.abulubad.sojourn.core.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.abulubad.sojourn.core.database.SojournDatabase
import java.util.Properties

actual fun inMemoryDriver(): SqlDriver = JdbcSqliteDriver(
    url = JdbcSqliteDriver.IN_MEMORY,
    properties = Properties().apply { put("foreign_keys", "true") },
    schema = SojournDatabase.Schema,
)
