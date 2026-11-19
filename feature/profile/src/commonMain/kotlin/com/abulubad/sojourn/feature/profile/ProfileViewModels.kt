package com.abulubad.sojourn.feature.profile

import androidx.lifecycle.viewModelScope
import com.abulubad.sojourn.core.domain.ObserveRewards
import com.abulubad.sojourn.core.domain.ObserveThemeMode
import com.abulubad.sojourn.core.domain.SetThemeMode
import com.abulubad.sojourn.core.localization.AppLocaleController
import com.abulubad.sojourn.core.navigation.AppearanceKey
import com.abulubad.sojourn.core.navigation.HelpKey
import com.abulubad.sojourn.core.navigation.LanguageKey
import com.abulubad.sojourn.core.navigation.RewardsKey
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * The Profile tab (ScreenProfile card): a guest on this device (no sign-in), Rewards first with the balance, then
 * Language, Appearance and Help, each showing its current value.
 */
class ProfileViewModel(
    observeRewards: ObserveRewards,
    observeThemeMode: ObserveThemeMode,
    localeController: AppLocaleController,
) : SojournViewModel<ProfileUiState, ProfileIntent, ProfileEffect>(
    ProfileUiState(locale = localeController.locale.value, themeMode = observeThemeMode().value),
) {

    init {
        viewModelScope.launch {
            combine(observeRewards(), localeController.locale, observeThemeMode()) { rewards, locale, mode ->
                ProfileUiState(rewards.points, locale, mode)
            }.collect { next -> setState { next } }
        }
    }

    override fun handle(intent: ProfileIntent) {
        val key = when (intent) {
            ProfileIntent.RewardsTapped -> RewardsKey
            ProfileIntent.LanguageTapped -> LanguageKey
            ProfileIntent.AppearanceTapped -> AppearanceKey
            ProfileIntent.HelpTapped -> HelpKey
        }
        sendEffect(ProfileEffect.Navigate(key))
    }
}

/**
 * The Language page: choosing a language applies it at once (the UI recomposes in it, and turns right to left for
 * Arabic) and returns to Profile, now in that language.
 */
class LanguageViewModel(
    private val localeController: AppLocaleController,
) : SojournViewModel<LanguageUiState, LanguageIntent, LanguageEffect>(
    LanguageUiState(localeController.locale.value),
) {

    init {
        viewModelScope.launch { localeController.locale.collect { setState { copy(selected = it) } } }
    }

    override fun handle(intent: LanguageIntent) {
        when (intent) {
            is LanguageIntent.Chosen -> {
                localeController.set(intent.locale)
                sendEffect(LanguageEffect.Back)
            }
            LanguageIntent.BackTapped -> sendEffect(LanguageEffect.Back)
        }
    }
}

/** The Appearance page: the choice applies at once and stays on the page, so the guest sees it before leaving. */
class AppearanceViewModel(
    observeThemeMode: ObserveThemeMode,
    private val setThemeMode: SetThemeMode,
) : SojournViewModel<AppearanceUiState, AppearanceIntent, AppearanceEffect>(
    AppearanceUiState(observeThemeMode().value),
) {

    init {
        viewModelScope.launch { observeThemeMode().collect { setState { copy(selected = it) } } }
    }

    override fun handle(intent: AppearanceIntent) {
        when (intent) {
            is AppearanceIntent.Chosen -> setThemeMode(intent.mode)
            AppearanceIntent.BackTapped -> sendEffect(AppearanceEffect.Back)
        }
    }
}

/** The Rewards page: points from completed stays on this device, so it works offline. */
class RewardsViewModel(
    observeRewards: ObserveRewards,
) : SojournViewModel<RewardsUiState, RewardsIntent, RewardsEffect>(RewardsUiState()) {

    init {
        viewModelScope.launch {
            observeRewards().collect { rewards ->
                setState { copy(rewards = ScreenState.fromData(rewards) { it.earned.isEmpty() }) }
            }
        }
    }

    override fun handle(intent: RewardsIntent) {
        when (intent) {
            RewardsIntent.BackTapped -> sendEffect(RewardsEffect.Back)
            RewardsIntent.ExploreTapped -> sendEffect(RewardsEffect.ShowExplore)
        }
    }
}
