package com.abulubad.sojourn.core.database

import app.cash.sqldelight.db.SqlDriver

/** A fresh in-memory database with the schema created and foreign keys on. */
expect fun inMemoryDriver(): SqlDriver
