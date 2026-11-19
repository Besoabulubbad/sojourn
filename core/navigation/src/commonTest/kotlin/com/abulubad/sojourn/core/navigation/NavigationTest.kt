package com.abulubad.sojourn.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigationTest {

    /** One instance of every key. A new key that is missing here or in the module fails the size check. */
    private val everyKey: List<SojournNavKey> = listOf(
        ExploreKey,
        SearchKey(
            SearchStep.When,
            SearchArgs(cityId = "london", checkIn = "2026-03-12", checkOut = "2026-03-27", adults = 2),
        ),
        ResultsKey(SearchArgs(cityId = "london")),
        MapKey(SearchArgs(cityId = "paris")),
        FiltersKey(SearchArgs(cityId = "london", checkIn = "2026-03-12", checkOut = "2026-03-27")),
        StayDetailsKey("stay-1", quoteId = "quote-1"),
        GalleryKey("stay-1", index = 3),
        CheckoutKey("quote-1"),
        BookingConfirmedKey("booking-1"),
        SavedKey,
        BookingsKey,
        BookingDetailsKey("booking-1"),
        ProfileKey,
        LanguageKey,
        AppearanceKey,
        RewardsKey,
        HelpKey,
    )

    @Test
    fun `every key round-trips through the explicit serializers module`() {
        val json = Json { serializersModule = sojournNavSerializersModule }
        val serializer = PolymorphicSerializer(NavKey::class)
        everyKey.forEach { key ->
            val decoded = json.decodeFromString(serializer, json.encodeToString(serializer, key))
            assertEquals(key, decoded)
        }
    }

    @Test
    fun `the round-trip covers every sealed subtype`() {
        // The sealed serializer's descriptor lists every subtype by serial name, on every platform, without reflection.
        val sealedSubtypes = SojournNavKey.serializer().descriptor.getElementDescriptor(1).elementNames.toSet()
        val covered = everyKey.map { it::class.simpleName }.toSet()
        assertEquals(sealedSubtypes.map { it.substringAfterLast('.') }.toSet(), covered)
    }

    @Test
    fun `tabs keep their own history`() {
        val navigator = navigator()
        navigator.goTo(ResultsKey(SearchArgs(cityId = "london")))
        navigator.goTo(StayDetailsKey("stay-1"))
        navigator.switchTab(Tab.Saved)
        navigator.goTo(StayDetailsKey("stay-2"))
        navigator.switchTab(Tab.Explore)

        assertEquals(
            listOf<NavKey>(ExploreKey, ResultsKey(SearchArgs(cityId = "london")), StayDetailsKey("stay-1")),
            navigator.currentStack.toList(),
        )
        navigator.switchTab(Tab.Saved)
        assertEquals(listOf<NavKey>(SavedKey, StayDetailsKey("stay-2")), navigator.currentStack.toList())
    }

    @Test
    fun `back pops then returns to explore then lets the platform handle it`() {
        val navigator = navigator()
        navigator.switchTab(Tab.Profile)
        navigator.goTo(LanguageKey)
        assertTrue(navigator.back())
        assertEquals(Tab.Profile, navigator.currentTab)
        assertTrue(navigator.back())
        assertEquals(Tab.Explore, navigator.currentTab)
        assertFalse(navigator.back())
    }

    @Test
    fun `reselecting a tab and popTo return to the requested key`() {
        val navigator = navigator()
        navigator.goTo(ResultsKey(SearchArgs()))
        navigator.goTo(StayDetailsKey("stay-1"))
        navigator.goTo(GalleryKey("stay-1"))
        navigator.popTo(ResultsKey(SearchArgs()))
        assertEquals(ResultsKey(SearchArgs()), navigator.currentStack.last())
        navigator.switchTab(Tab.Explore)
        assertEquals(listOf<NavKey>(ExploreKey), navigator.currentStack.toList())
    }

    @Test
    fun `a finished flow replaces itself and the results it came from but never the root`() {
        val navigator = navigator()
        val first = ResultsKey(SearchArgs(cityId = "london"))
        val second = ResultsKey(SearchArgs(cityId = "paris"))
        navigator.goTo(first)
        navigator.goTo(SearchKey())
        navigator.goToReplacing(second) { it is SearchKey || it is ResultsKey }
        assertEquals(listOf<NavKey>(ExploreKey, second), navigator.currentStack.toList())
        navigator.goToReplacing(first) { true }
        assertEquals(listOf<NavKey>(ExploreKey, first), navigator.currentStack.toList())
    }

    private fun navigator() = TabbedNavigator(Tab.entries.associateWith { NavBackStack<NavKey>(it.root) })
}
