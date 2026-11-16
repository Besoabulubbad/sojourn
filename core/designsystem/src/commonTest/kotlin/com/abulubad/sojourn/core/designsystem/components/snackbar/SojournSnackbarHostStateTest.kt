package com.abulubad.sojourn.core.designsystem.components.snackbar

import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Snackbar card timing: five seconds on screen, then dismissed; the action ends it early. */
class SojournSnackbarHostStateTest {

    @Test
    fun `a snackbar is dismissed after five seconds`() = runTest {
        val host = SojournSnackbarHostState()
        val shown = async { host.show("Removed from Saved", actionLabel = "Undo") }
        advanceTimeBy(4_999)
        assertFalse(shown.isCompleted, "still visible before five seconds")
        advanceTimeBy(2)
        runCurrent()
        assertTrue(shown.isCompleted)
        assertEquals(SojournSnackbarResult.Dismissed, shown.await())
    }

    @Test
    fun `taking the action reports it`() = runTest {
        val host = SojournSnackbarHostState()
        val shown = async { host.show("Removed from Saved", actionLabel = "Undo") }
        runCurrent()
        host.current!!.performAction()
        assertEquals(SojournSnackbarResult.ActionPerformed, shown.await())
    }
}
