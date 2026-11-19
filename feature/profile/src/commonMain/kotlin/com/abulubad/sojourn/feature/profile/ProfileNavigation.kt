package com.abulubad.sojourn.feature.profile

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.abulubad.sojourn.core.navigation.AppearanceKey
import com.abulubad.sojourn.core.navigation.HelpKey
import com.abulubad.sojourn.core.navigation.LanguageKey
import com.abulubad.sojourn.core.navigation.Navigator
import com.abulubad.sojourn.core.navigation.ProfileKey
import com.abulubad.sojourn.core.navigation.RewardsKey

/** The Profile tab's root and the pages its rows open. */
fun EntryProviderScope<NavKey>.profileEntries(navigator: Navigator) {
    entry<ProfileKey> { ProfileRoute(navigator) }
    entry<RewardsKey> { RewardsRoute(navigator) }
    entry<LanguageKey> { LanguageRoute(navigator) }
    entry<AppearanceKey> { AppearanceRoute(navigator) }
    entry<HelpKey> { HelpRoute(navigator) }
}
