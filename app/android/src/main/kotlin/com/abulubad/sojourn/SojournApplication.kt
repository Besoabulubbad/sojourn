package com.abulubad.sojourn

import android.app.Application
import org.koin.android.ext.koin.androidContext

/** Starts Koin once per process, with the application `Context` the database and settings need. */
class SojournApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@SojournApplication) }
    }
}
