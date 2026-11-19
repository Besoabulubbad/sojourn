package com.abulubad.sojourn.core.localization

import androidx.compose.ui.unit.LayoutDirection
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppLocaleControllerTest {

    @Test
    fun `starts in english when nothing is stored and nothing is seeded`() {
        val controller = SettingsAppLocaleController(MapSettings(), PlatformLocaleSync.None)
        assertEquals(AppLocale.EN_GB, controller.locale.value)
    }

    @Test
    fun `seeds from the device language on first launch only`() {
        val settings = MapSettings()
        assertEquals(
            AppLocale.AR,
            SettingsAppLocaleController(settings, PlatformLocaleSync.None, initialTag = "ar-DZ").locale.value,
        )
        assertNull(settings.getStringOrNull(SettingsAppLocaleController.KEY), "seeding does not persist")
    }

    @Test
    fun `setting a locale persists it and flips direction and syncs the platform`() {
        val settings = MapSettings()
        val synced = mutableListOf<AppLocale>()
        val controller = SettingsAppLocaleController(settings, { synced += it })

        controller.set(AppLocale.AR)

        assertEquals(AppLocale.AR, controller.locale.value)
        assertEquals(LayoutDirection.Rtl, controller.locale.value.layoutDirection)
        assertEquals("ar", settings.getStringOrNull(SettingsAppLocaleController.KEY))
        assertEquals(listOf(AppLocale.AR), synced)

        controller.set(AppLocale.AR)
        assertEquals(1, synced.size, "no-op when unchanged")

        val restarted = SettingsAppLocaleController(settings, PlatformLocaleSync.None, initialTag = "en")
        assertEquals(AppLocale.AR, restarted.locale.value, "stored choice beats the device language")
    }

    @Test
    fun `unknown tags fall back to english`() {
        assertEquals(AppLocale.EN_GB, AppLocale.fromTag("fr-FR"))
        assertEquals(AppLocale.EN_GB, AppLocale.fromTag(null))
        assertEquals(AppLocale.AR, AppLocale.fromTag("AR"))
    }

    @Test
    fun `the platform's per-app language wins when the app ships it`() {
        val settings = MapSettings().apply { putString(SettingsAppLocaleController.KEY, "en-GB") }
        val platform = FakePlatform("ar")
        assertEquals(AppLocale.AR, SettingsAppLocaleController(settings, platform).locale.value)
        platform.tag = "fr-FR"
        assertEquals(AppLocale.EN_GB, SettingsAppLocaleController(settings, platform).locale.value, "stored wins")
    }

    @Test
    fun `a change made in system settings is adopted and stored without echoing back`() {
        val settings = MapSettings()
        val platform = FakePlatform(null)
        val controller = SettingsAppLocaleController(settings, platform)
        assertEquals(AppLocale.EN_GB, controller.locale.value)

        platform.tag = "ar"
        controller.syncFromPlatform()

        assertEquals(AppLocale.AR, controller.locale.value)
        assertEquals("ar", settings.getStringOrNull(SettingsAppLocaleController.KEY))
        assertEquals(emptyList(), platform.applied)

        platform.tag = null
        controller.syncFromPlatform()
        assertEquals(AppLocale.AR, controller.locale.value, "system default keeps the in-app choice")
    }
}

private class FakePlatform(var tag: String?) : PlatformLocaleSync {
    val applied = mutableListOf<AppLocale>()
    override fun apply(locale: AppLocale) {
        applied += locale
    }
    override fun current(): String? = tag
}
