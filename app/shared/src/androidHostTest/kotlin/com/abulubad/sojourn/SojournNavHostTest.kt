package com.abulubad.sojourn

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants
import com.abulubad.sojourn.core.domain.GetCities
import com.abulubad.sojourn.core.domain.ListBookings
import com.abulubad.sojourn.core.domain.ObserveRecentSearches
import com.abulubad.sojourn.core.testing.FixedAppClock
import com.abulubad.sojourn.core.testing.StayFixtures
import com.abulubad.sojourn.core.testing.data.FakeBookingRepository
import com.abulubad.sojourn.core.testing.data.FakeCityRepository
import com.abulubad.sojourn.core.testing.data.FakeSearchRepository
import com.abulubad.sojourn.core.testing.data.FakeStayRepository
import com.abulubad.sojourn.feature.explore.ExploreViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The navigation host composes with the tabs that exist and opens on Explore. Decorating a stack resolves every key
 * in it, so a tab whose root has no entry crashes at start-up ("Unknown screen"); this catches it before a device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SojournNavHostTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val fakes = module {
        viewModel {
            ExploreViewModel(
                GetCities(FakeCityRepository(remote = listOf(StayFixtures.london))),
                ObserveRecentSearches(FakeSearchRepository()),
                ListBookings(FakeBookingRepository(), FakeStayRepository(), FixedAppClock()),
            )
        }
    }

    @Test
    fun `the host starts on Explore`() {
        composeRule.setContent {
            KoinApplication(application = { modules(fakes) }) {
                SojournPreview(SojournVariants.EnLight) { SojournNavHost() }
            }
        }
        composeRule.onNodeWithText("Find your next home base").assertExists()
    }
}
