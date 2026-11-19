package com.abulubad.sojourn.core.model

/** The app's colour scheme (ScreenProfile card): the device's by default, or always light or dark. */
enum class ThemeMode(val id: String) {
    System("system"),
    Light("light"),
    Dark("dark"),
    ;

    /** Whether the app draws dark, given whether the device is in dark mode. */
    fun isDark(deviceIsDark: Boolean): Boolean = when (this) {
        System -> deviceIsDark
        Light -> false
        Dark -> true
    }

    companion object {
        val Default: ThemeMode = System
    }
}
