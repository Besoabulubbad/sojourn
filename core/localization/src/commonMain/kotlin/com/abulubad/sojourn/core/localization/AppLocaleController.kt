package com.abulubad.sojourn.core.localization

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reads and changes the app language. The value is persisted and exposed as state; the app recomposes from it.
 * Changing it also asks the OS to set the per-app language so keyboards and system dialogs follow (best effort,
 * see [PlatformLocaleSync]).
 */
interface AppLocaleController {
    val locale: StateFlow<AppLocale>
    fun set(locale: AppLocale)
}

/** The OS side of a language change: Android's `LocaleManager`, iOS's `AppleLanguages`. */
fun interface PlatformLocaleSync {
    fun apply(locale: AppLocale)

    companion object {
        val None = PlatformLocaleSync { }
    }
}

/**
 * Persists the choice with multiplatform-settings. With no stored value the app starts in [AppLocale.Default];
 * platform shells may seed it from the device language on first launch via [initialTag].
 */
class SettingsAppLocaleController(
    private val settings: Settings,
    private val platformSync: PlatformLocaleSync,
    initialTag: String? = null,
) : AppLocaleController {

    private val state = MutableStateFlow(AppLocale.fromTag(settings.getStringOrNull(KEY) ?: initialTag))

    override val locale: StateFlow<AppLocale> = state.asStateFlow()

    override fun set(locale: AppLocale) {
        if (state.value == locale) return
        settings.putString(KEY, locale.tag)
        state.value = locale
        platformSync.apply(locale)
    }

    internal companion object {
        const val KEY = "app_locale"
    }
}
