package com.abulubad.sojourn.feature.profile

import app.cash.turbine.test
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.domain.ListBookings
import com.abulubad.sojourn.core.domain.ObserveRewards
import com.abulubad.sojourn.core.domain.ObserveThemeMode
import com.abulubad.sojourn.core.domain.Rewards
import com.abulubad.sojourn.core.domain.SetThemeMode
import com.abulubad.sojourn.core.localization.AppLocale
import com.abulubad.sojourn.core.localization.AppLocaleController
import com.abulubad.sojourn.core.model.ThemeMode
import com.abulubad.sojourn.core.navigation.AppearanceKey
import com.abulubad.sojourn.core.navigation.HelpKey
import com.abulubad.sojourn.core.navigation.LanguageKey
import com.abulubad.sojourn.core.navigation.RewardsKey
import com.abulubad.sojourn.core.testing.FixedAppClock
import com.abulubad.sojourn.core.testing.QuoteFixtures
import com.abulubad.sojourn.core.testing.StayFixtures
import com.abulubad.sojourn.core.testing.data.FakeBookingRepository
import com.abulubad.sojourn.core.testing.data.FakePreferencesRepository
import com.abulubad.sojourn.core.testing.data.FakeStayRepository
import com.abulubad.sojourn.core.testing.runSojournTest
import com.abulubad.sojourn.core.ui.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class ProfileViewModelTest {

    // The studio for 12–27 March 2026, £ total from the fixture; on 1 April it is completed.
    private val quote = QuoteFixtures.canalStudioFortnight
    private val bookings = FakeBookingRepository()
    private val stays = FakeStayRepository(cached = listOf(StayFixtures.canalStudio))
    private val preferences = FakePreferencesRepository()
    private val locale = FakeLocaleController()

    private fun rewards(clock: FixedAppClock) = ObserveRewards(ListBookings(bookings, stays, clock))
    private val afterStay = FixedAppClock(Instant.parse("2026-04-01T09:00:00Z"))

    private suspend fun booked() = bookings.run {
        issued[quote.id] = quote
        (book(quote.id) as AppResult.Success).value
    }

    @Test
    fun `profile shows the balance and settings and each row opens its page`() = runSojournTest {
        val booking = booked()
        preferences.setThemeMode(ThemeMode.Dark)
        val vm = ProfileViewModel(rewards(afterStay), ObserveThemeMode(preferences), locale)
        advanceUntilIdle()
        assertEquals(ProfileUiState(booking.total.major.toInt(), AppLocale.EN_GB, ThemeMode.Dark), vm.state.value)
        vm.effects.test {
            vm.onIntent(ProfileIntent.RewardsTapped)
            assertEquals(ProfileEffect.Navigate(RewardsKey), awaitItem())
            vm.onIntent(ProfileIntent.LanguageTapped)
            assertEquals(ProfileEffect.Navigate(LanguageKey), awaitItem())
            vm.onIntent(ProfileIntent.AppearanceTapped)
            assertEquals(ProfileEffect.Navigate(AppearanceKey), awaitItem())
            vm.onIntent(ProfileIntent.HelpTapped)
            assertEquals(ProfileEffect.Navigate(HelpKey), awaitItem())
        }
    }

    @Test
    fun `an upcoming stay earns nothing yet`() = runSojournTest {
        booked()
        val vm = ProfileViewModel(rewards(FixedAppClock()), ObserveThemeMode(preferences), locale)
        advanceUntilIdle()
        assertEquals(0, vm.state.value.points)
    }

    @Test
    fun `choosing a language applies it and returns to Profile`() = runSojournTest {
        val vm = LanguageViewModel(locale)
        vm.effects.test {
            vm.onIntent(LanguageIntent.Chosen(AppLocale.AR))
            assertEquals(LanguageEffect.Back, awaitItem())
        }
        advanceUntilIdle()
        assertEquals(AppLocale.AR, locale.locale.value)
        assertEquals(AppLocale.AR, vm.state.value.selected)
    }

    @Test
    fun `choosing an appearance applies it and stays on the page`() = runSojournTest {
        val vm = AppearanceViewModel(ObserveThemeMode(preferences), SetThemeMode(preferences))
        vm.onIntent(AppearanceIntent.Chosen(ThemeMode.Light))
        advanceUntilIdle()
        assertEquals(ThemeMode.Light, preferences.themeMode.value)
        assertEquals(ThemeMode.Light, vm.state.value.selected)
    }

    @Test
    fun `rewards are empty before a completed stay then list it`() = runSojournTest {
        val before = RewardsViewModel(rewards(FixedAppClock()))
        booked()
        advanceUntilIdle()
        assertEquals(ScreenState.Empty, before.state.value.rewards)
        before.effects.test {
            before.onIntent(RewardsIntent.ExploreTapped)
            assertEquals(RewardsEffect.ShowExplore, awaitItem())
        }
        val after = RewardsViewModel(rewards(afterStay))
        advanceUntilIdle()
        val content = assertIs<ScreenState.Content<Rewards>>(after.state.value.rewards)
        assertEquals(quote.stayId, content.data.earned.single().item.booking.stayId)
    }
}

private class FakeLocaleController : AppLocaleController {
    private val state = MutableStateFlow(AppLocale.EN_GB)
    override val locale: StateFlow<AppLocale> = state.asStateFlow()
    override fun set(locale: AppLocale) {
        state.value = locale
    }
}
