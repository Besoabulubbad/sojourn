package com.abulubad.sojourn.core.data

import app.cash.sqldelight.db.SqlDriver

/** A fresh in-memory database driver with the schema created and foreign keys on. */
expect fun inMemoryDriver(): SqlDriver
