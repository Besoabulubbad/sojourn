package com.abulubad.sojourn.core.designsystem.theme

import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.LayoutDirection
import com.abulubad.sojourn.core.localization.AppLocale
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.tab_saved
import org.jetbrains.compose.resources.stringResource
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.Locale
import kotlin.test.assertEquals

/**
 * L-03: changing the in-app language switches text and direction together, whatever the device language. Runs the
 * four device/app pairs (EN/EN, EN/AR, AR/EN, AR/AR) through `SojournTheme`, the app's real entry point.
 *
 * Regression for the bug the Price screenshots caught: resources followed the device language, not `AppLocale`.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [36])
class AppLanguagePairsTest(private val deviceLanguage: String, private val appLocale: AppLocale) {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setDeviceLanguage() {
        RuntimeEnvironment.setQualifiers("+$deviceLanguage")
        Locale.setDefault(Locale.forLanguageTag(deviceLanguage))
    }

    @Test
    fun textAndDirectionFollowTheAppLanguage() {
        var direction: LayoutDirection? = null
        composeRule.setContent {
            SojournTheme(locale = appLocale, darkTheme = false) {
                direction = LocalLayoutDirection.current
                BasicText(stringResource(Res.string.tab_saved), Modifier.testTag(TAG))
            }
        }
        val expected = if (appLocale == AppLocale.AR) "المحفوظات" else "Saved"
        composeRule.onNodeWithTag(TAG).assertTextEquals(expected)
        assertEquals(appLocale.layoutDirection, direction)
    }

    companion object {
        private const val TAG = "label"

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "device={0} app={1}")
        fun pairs(): List<Array<Any>> = listOf("en", "ar").flatMap { device ->
            AppLocale.entries.map { app -> arrayOf<Any>(device, app) }
        }
    }
}
