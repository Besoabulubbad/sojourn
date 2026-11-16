package com.abulubad.sojourn.core.designsystem.components.internal

import kotlin.test.Test
import kotlin.test.assertEquals

class ShareRowTest {

    @Test
    fun `children share a row equally when every word fits`() {
        assertEquals(listOf(100, 100, 100), shareRow(300, listOf(40, 90, 20)))
    }

    @Test
    fun `a long word gets its minimum and the rest is shared`() {
        // Equal share is 60; "Studio" needs 120. Minimums sum to 220; the remaining 80 adds 16 to each.
        assertEquals(listOf(56, 136, 36, 36, 36), shareRow(300, listOf(40, 120, 20, 20, 20)))
    }

    @Test
    fun `the rounding remainder goes to the last child`() {
        assertEquals(listOf(33, 33, 34), shareRow(100, listOf(10, 10, 10)))
    }

    @Test
    fun `children wrap onto another row instead of breaking a word`() {
        // 100 wide, gap 4: 60 and 30 fit together (94), adding 50 would not, so it starts the second row.
        // Row one shares 96 px: 60 > 48, so each gets its minimum plus 3.
        assertEquals(listOf(listOf(63, 33), listOf(100)), shareRows(100, listOf(60, 30, 50), 4))
    }

    @Test
    fun `one row when everything fits`() {
        assertEquals(listOf(listOf(48, 48)), shareRows(100, listOf(30, 30), 4))
    }
}
