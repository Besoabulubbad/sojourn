package com.abulubad.sojourn.core.localization

import platform.Foundation.NSUserDefaults

/**
 * Writes the `AppleLanguages` override so the app's bundle resources (and Compose Multiplatform resources) resolve
 * the chosen language on next launch; the shell shows a relaunch prompt where needed (ADR 0009).
 */
class IosLocaleSync : PlatformLocaleSync {
    override fun apply(locale: AppLocale) {
        NSUserDefaults.standardUserDefaults.setObject(listOf(locale.tag), forKey = APPLE_LANGUAGES)
    }

    /**
     * The app's first preferred language: the one chosen in Settings › Sojourn › Language (iOS relaunches the app
     * after a change), or the in-app choice, or, before either, the device's first language.
     */
    override fun current(): String? =
        NSUserDefaults.standardUserDefaults.stringArrayForKey(APPLE_LANGUAGES)?.firstOrNull() as? String

    private companion object {
        const val APPLE_LANGUAGES = "AppleLanguages"
    }
}
