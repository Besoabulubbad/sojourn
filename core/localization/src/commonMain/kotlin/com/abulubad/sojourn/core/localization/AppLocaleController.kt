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

    /**
     * Adopts a language the guest chose outside the app (Android's "App languages" setting, iOS Settings), if the app
     * ships it. The shell calls it when its UI is (re)created: Android recreates the activity after such a change.
     */
    fun syncFromPlatform() {}
}

/** The OS side of a language change: Android's `LocaleManager`, iOS's `AppleLanguages`. */
fun interface PlatformLocaleSync {
    fun apply(locale: AppLocale)

    /** The per-app language the OS holds now (a BCP 47 tag), or null when it holds none. */
    fun current(): String? = null

    companion object {
        val None = PlatformLocaleSync { }
    }
}

/**
 * Persists the choice with multiplatform-settings. The OS's per-app language wins when it names a language the app
 * ships (the guest may have changed it in system settings); else the stored choice; else [initialTag] (a shell may
 * seed it from the device language); else [AppLocale.Default].
 */
class SettingsAppLocaleController(
    private val settings: Settings,
    private val platformSync: PlatformLocaleSync,
    initialTag: String? = null,
) : AppLocaleController {

    private val state = MutableStateFlow(
        AppLocale.supportedOrNull(platformSync.current())
            ?: AppLocale.fromTag(settings.getStringOrNull(KEY) ?: initialTag),
    )

    override val locale: StateFlow<AppLocale> = state.asStateFlow()

    override fun set(locale: AppLocale) {
        if (state.value == locale) return
        settings.putString(KEY, locale.tag)
        state.value = locale
        platformSync.apply(locale)
    }

    override fun syncFromPlatform() {
        val chosen = AppLocale.supportedOrNull(platformSync.current()) ?: return
        if (chosen == state.value) return
        settings.putString(KEY, chosen.tag)
        state.value = chosen
    }

    internal companion object {
        const val KEY = "app_locale"
    }
}
