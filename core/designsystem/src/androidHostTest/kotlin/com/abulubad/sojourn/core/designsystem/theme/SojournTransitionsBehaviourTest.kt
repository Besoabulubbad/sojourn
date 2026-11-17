package com.abulubad.sojourn.core.designsystem.theme

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariant
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants
import com.abulubad.sojourn.core.designsystem.tokens.SojournMotion
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The step transition follows reading direction (M-04) and becomes a plain fade under reduce motion (M-03). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SojournTransitionsBehaviourTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `a forward step enters from the right in English`() {
        assertTrue(incomingOffsetMidway(SojournVariants.EnLight, reduceMotion = false) > 0f)
    }

    @Test
    fun `a forward step enters from the left in Arabic`() {
        assertTrue(incomingOffsetMidway(SojournVariants.ArLight, reduceMotion = false) < 0f)
    }

    @Test
    fun `under reduce motion a step only fades`() {
        assertEquals(0f, incomingOffsetMidway(SojournVariants.EnLight, reduceMotion = true))
    }

    /** Starts a forward step, stops the clock halfway, and returns the incoming step's horizontal position in px. */
    private fun incomingOffsetMidway(variant: SojournVariant, reduceMotion: Boolean): Float {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            SojournPreview(variant) {
                CompositionLocalProvider(LocalReduceMotion provides reduceMotion) {
                    var step by remember { mutableIntStateOf(0) }
                    LaunchedEffect(Unit) { step = 1 }
                    val forward = SojournTransitions.stepForward()
                    AnimatedContent(step, transitionSpec = { forward }) { current ->
                        Box(Modifier.fillMaxWidth().height(STEP_HEIGHT).testTag("step-$current"))
                    }
                }
            }
        }
        // Frame 1 composes step 0 and runs the effect; frame 2 starts the transition to step 1.
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeBy(SojournMotion.base / 2L)
        // positionInRoot is not clipped to the root, so an incoming step left of the screen reads as negative.
        return composeRule.onNodeWithTag("step-1", useUnmergedTree = true).fetchSemanticsNode().positionInRoot.x
    }

    private companion object {
        val STEP_HEIGHT = 40.dp
    }
}
