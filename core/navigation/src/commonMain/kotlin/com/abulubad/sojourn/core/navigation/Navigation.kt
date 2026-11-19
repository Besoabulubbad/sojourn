package com.abulubad.sojourn.core.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/** The four bottom-navigation destinations, each with its own history. */
enum class Tab(val root: SojournNavKey) {
    Explore(ExploreKey),
    Saved(SavedKey),
    Bookings(BookingsKey),
    Profile(ProfileKey),
}

/**
 * What features call to move around (ADR 0005). Features never touch a back stack directly; `app:shared`
 * provides the implementation.
 */
interface Navigator {
    /** Pushes [key] onto the current tab's stack. */
    fun goTo(key: SojournNavKey)

    /** Pops the current tab; returns false when there is nothing left to pop (the platform then handles back). */
    fun back(): Boolean

    /** Shows [tab] with its history intact; re-selecting the current tab returns it to its root. */
    fun switchTab(tab: Tab)

    /** Pops the current tab until [key] is on top, or to the root if it is not in the stack. */
    fun popTo(key: SojournNavKey)

    /**
     * Pops every entry on top of the current tab that [replaces] matches (never the root), then pushes [key]: a flow
     * that hands over to its result. A search finished from Results replaces both the flow and the old results, so
     * back from the new results returns to where the search began.
     */
    fun goToReplacing(key: SojournNavKey, replaces: (SojournNavKey) -> Boolean)
}

/**
 * Explicit polymorphic registration of every key. Required on iOS, where the reflection-based back-stack
 * serializer is unavailable (JetBrains Navigation 3 docs); a unit test round-trips every key through it.
 */
val sojournNavSerializersModule: SerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(ExploreKey::class, ExploreKey.serializer())
        subclass(SearchKey::class, SearchKey.serializer())
        subclass(ResultsKey::class, ResultsKey.serializer())
        subclass(MapKey::class, MapKey.serializer())
        subclass(FiltersKey::class, FiltersKey.serializer())
        subclass(StayDetailsKey::class, StayDetailsKey.serializer())
        subclass(GalleryKey::class, GalleryKey.serializer())
        subclass(CheckoutKey::class, CheckoutKey.serializer())
        subclass(BookingConfirmedKey::class, BookingConfirmedKey.serializer())
        subclass(SavedKey::class, SavedKey.serializer())
        subclass(BookingsKey::class, BookingsKey.serializer())
        subclass(BookingDetailsKey::class, BookingDetailsKey.serializer())
        subclass(ProfileKey::class, ProfileKey.serializer())
        subclass(LanguageKey::class, LanguageKey.serializer())
        subclass(AppearanceKey::class, AppearanceKey.serializer())
        subclass(RewardsKey::class, RewardsKey.serializer())
        subclass(HelpKey::class, HelpKey.serializer())
    }
}

/** The configuration `rememberNavBackStack` needs on every platform. */
val sojournSavedStateConfiguration: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = sojournNavSerializersModule
}

/**
 * Tab back stacks plus the selected tab: the whole navigation state, as plain observable data. `app:shared`
 * creates the stacks with `rememberNavBackStack(sojournSavedStateConfiguration, tab.root)` so they survive process
 * death, then renders [currentStack] in its `NavDisplay`.
 */
class TabbedNavigator(private val stacks: Map<Tab, NavBackStack<NavKey>>, initialTab: Tab = Tab.Explore) : Navigator {

    init {
        require(stacks.keys == Tab.entries.toSet()) { "Every tab needs a back stack" }
    }

    var currentTab: Tab by mutableStateOf(initialTab)
        private set

    val currentStack: NavBackStack<NavKey> get() = stacks.getValue(currentTab)

    override fun goTo(key: SojournNavKey) {
        currentStack.add(key)
    }

    override fun back(): Boolean {
        if (currentStack.size > 1) {
            currentStack.removeAt(currentStack.lastIndex)
            return true
        }
        if (currentTab != Tab.Explore) {
            currentTab = Tab.Explore
            return true
        }
        return false
    }

    override fun switchTab(tab: Tab) {
        if (tab == currentTab) {
            popTo(tab.root)
        } else {
            currentTab = tab
        }
    }

    override fun goToReplacing(key: SojournNavKey, replaces: (SojournNavKey) -> Boolean) {
        val stack = currentStack
        while (stack.size > 1 && (stack.last() as? SojournNavKey)?.let(replaces) == true) {
            stack.removeAt(stack.lastIndex)
        }
        stack.add(key)
    }

    override fun popTo(key: SojournNavKey) {
        val stack = currentStack
        while (stack.size > 1 && stack.last() != key) stack.removeAt(stack.lastIndex)
    }
}
