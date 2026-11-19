package com.abulubad.sojourn.feature.profile

import com.abulubad.sojourn.core.domain.Rewards
import com.abulubad.sojourn.core.localization.AppLocale
import com.abulubad.sojourn.core.model.ThemeMode
import com.abulubad.sojourn.core.navigation.SojournNavKey
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.UiEffect
import com.abulubad.sojourn.core.ui.UiIntent
import com.abulubad.sojourn.core.ui.UiState

/**
 * The Profile tab's state: the points balance (null until it is read), and the language and appearance the rows
 * show as their values.
 */
data class ProfileUiState(
    val points: Int? = null,
    val locale: AppLocale = AppLocale.Default,
    val themeMode: ThemeMode = ThemeMode.Default,
) : UiState

/** What the guest did on the Profile tab: each row opens its page. */
sealed interface ProfileIntent : UiIntent {
    data object RewardsTapped : ProfileIntent
    data object LanguageTapped : ProfileIntent
    data object AppearanceTapped : ProfileIntent
    data object HelpTapped : ProfileIntent
}

/** The Profile tab's one-off outputs. */
sealed interface ProfileEffect : UiEffect {
    data class Navigate(val key: SojournNavKey) : ProfileEffect
}

/** The Language page: the app's language, each option named in its own script. */
data class LanguageUiState(val selected: AppLocale = AppLocale.Default) : UiState

sealed interface LanguageIntent : UiIntent {
    data class Chosen(val locale: AppLocale) : LanguageIntent
    data object BackTapped : LanguageIntent
}

sealed interface LanguageEffect : UiEffect {
    data object Back : LanguageEffect
}

/** The Appearance page: System, Light or Dark. */
data class AppearanceUiState(val selected: ThemeMode = ThemeMode.Default) : UiState

sealed interface AppearanceIntent : UiIntent {
    data class Chosen(val mode: ThemeMode) : AppearanceIntent
    data object BackTapped : AppearanceIntent
}

sealed interface AppearanceEffect : UiEffect {
    data object Back : AppearanceEffect
}

/** The Rewards page: the balance and the completed stays that earned it; Empty before the first. */
data class RewardsUiState(val rewards: ScreenState<Rewards> = ScreenState.Loading) : UiState

sealed interface RewardsIntent : UiIntent {
    data object BackTapped : RewardsIntent
    data object ExploreTapped : RewardsIntent
}

sealed interface RewardsEffect : UiEffect {
    data object Back : RewardsEffect
    data object ShowExplore : RewardsEffect
}
