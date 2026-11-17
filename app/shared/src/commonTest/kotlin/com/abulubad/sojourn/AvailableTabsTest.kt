package com.abulubad.sojourn

import com.abulubad.sojourn.core.navigation.Tab
import kotlin.test.Test
import kotlin.test.assertEquals

class AvailableTabsTest {

    @Test
    fun `Explore is always available and first because back exits through it`() {
        assertEquals(Tab.Explore, availableTabs.first())
        assertEquals(availableTabs.distinct(), availableTabs)
    }
}
