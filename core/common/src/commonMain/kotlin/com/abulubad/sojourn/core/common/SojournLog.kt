package com.abulubad.sojourn.core.common

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

/**
 * The logging facade. Classes take a tagged [Logger] from [tagged]; the app shell calls [configure] once at start.
 * Kermit writes to Logcat on Android and OSLog on iOS. Nothing user-facing is ever logged.
 */
object SojournLog {
    fun tagged(tag: String): Logger = Logger.withTag(tag)

    /** Verbose in debug builds, warnings and errors in release. */
    fun configure(debug: Boolean) {
        Logger.setMinSeverity(if (debug) Severity.Verbose else Severity.Warn)
    }
}
