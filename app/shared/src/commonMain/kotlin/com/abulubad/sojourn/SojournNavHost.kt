package com.abulubad.sojourn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.abulubad.sojourn.core.designsystem.components.bottomnav.SojournBottomNav
import com.abulubad.sojourn.core.designsystem.components.bottomnav.SojournNavTab
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.tab_bookings
import com.abulubad.sojourn.core.localization.resources.tab_explore
import com.abulubad.sojourn.core.localization.resources.tab_profile
import com.abulubad.sojourn.core.localization.resources.tab_saved
import com.abulubad.sojourn.core.navigation.Navigator
import com.abulubad.sojourn.core.navigation.Tab
import com.abulubad.sojourn.core.navigation.TabbedNavigator
import com.abulubad.sojourn.core.navigation.sojournSavedStateConfiguration
import com.abulubad.sojourn.core.ui.LocalTabBar
import com.abulubad.sojourn.feature.explore.exploreEntries
import com.abulubad.sojourn.feature.filters.filtersEntries
import com.abulubad.sojourn.feature.results.resultsEntries
import com.abulubad.sojourn.feature.search.searchEntries
import org.jetbrains.compose.resources.stringResource

/**
 * The tabs whose feature exists. A tab joins in its feature's PR; the bottom nav appears once there are two, the
 * fewest it shows (`SojournBottomNav`).
 */
internal val availableTabs: List<Tab> = listOf(Tab.Explore)

/**
 * Navigation 3 with one back stack per tab (ADR 0005), after the official multiple-back-stacks recipe:
 *
 * - Each tab's stack is a `rememberNavBackStack`, so it survives configuration changes and process death.
 * - Each stack's entries are decorated on their own (saved state, and a `ViewModelStore` per entry), so a tab keeps
 *   its screens' state while another tab is shown.
 * - An entry marked `SojournScenes.overlay()` draws over the one beneath it ([SojournOverlaySceneStrategy]).
 * - The display shows Explore's stack, plus the current tab's on top of it: back from another tab's root returns to
 *   Explore, and back from Explore's root leaves the app ([TabbedNavigator.back]).
 */
@Composable
internal fun SojournNavHost(modifier: Modifier = Modifier) {
    val stacks = Tab.entries.associateWith { tab -> rememberNavBackStack(sojournSavedStateConfiguration, tab.root) }
    val navigator = remember(stacks) { TabbedNavigator(stacks) }
    val provider = entryProvider<NavKey>(fallback = { key -> NavEntry(key) { NotBuiltYet() } }) {
        sojournEntries(navigator)
    }
    // Only tabs that exist are decorated: decorating resolves every key in a stack, and an unbuilt tab's root has no
    // entry yet. Their stacks still exist, as TabbedNavigator needs one per tab.
    val entries: Map<Tab, List<NavEntry<NavKey>>> = availableTabs.associateWith { tab ->
        rememberDecoratedNavEntries(
            backStack = stacks.getValue(tab),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = provider,
        )
    }
    val shown = entries.getValue(Tab.Explore) +
        if (navigator.currentTab == Tab.Explore) emptyList() else entries[navigator.currentTab].orEmpty()

    val overlays = remember { SojournOverlaySceneStrategy<NavKey>() }
    CompositionLocalProvider(LocalTabBar provides { TabBar(navigator) }) {
        NavDisplay(
            entries = shown,
            onBack = { navigator.back() },
            modifier = modifier,
            sceneStrategies = listOf(overlays, SinglePaneSceneStrategy()),
        )
    }
}

/** Every feature's entries. Each feature PR adds its `<name>Entries(navigator)` here. */
private fun EntryProviderScope<NavKey>.sojournEntries(navigator: Navigator) {
    exploreEntries(navigator)
    searchEntries(navigator)
    resultsEntries(navigator)
    filtersEntries(navigator)
}

@Composable
private fun TabBar(navigator: TabbedNavigator) {
    if (availableTabs.size < MIN_TABS) return
    val tabs = availableTabs.map { tab -> SojournNavTab(tab, tab.icon, stringResource(tab.label)) }
    SojournBottomNav(tabs = tabs, selected = navigator.currentTab, onSelect = navigator::switchTab)
}

private const val MIN_TABS = 2

private val Tab.icon: SojournIcon
    get() = when (this) {
        Tab.Explore -> SojournIcon.Explore
        Tab.Saved -> SojournIcon.Saved
        Tab.Bookings -> SojournIcon.Bookings
        Tab.Profile -> SojournIcon.Profile
    }

private val Tab.label
    get() = when (this) {
        Tab.Explore -> Res.string.tab_explore
        Tab.Saved -> Res.string.tab_saved
        Tab.Bookings -> Res.string.tab_bookings
        Tab.Profile -> Res.string.tab_profile
    }
