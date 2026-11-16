package com.abulubad.sojourn.core.designsystem.components.rating

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants
import com.abulubad.sojourn.core.localization.SojournFormatter.Companion.isolate
import com.abulubad.sojourn.core.model.Rating
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** N-04 and the spoken label, checked on the semantics tree rather than pixels. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SojournRatingBehaviourTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun render(rating: Rating?) = composeRule.setContent {
        SojournPreview(SojournVariants.EnLight) {
            Box(Modifier.testTag(HOST)) { SojournRating(rating) }
        }
    }

    @Test
    fun `fewer than three reviews renders nothing`() {
        render(SojournRatingSamples.tooFewReviews)
        composeRule.onNodeWithTag(HOST).onChildren().assertCountEquals(0)
    }

    @Test
    fun `a missing rating renders nothing`() {
        render(null)
        composeRule.onNodeWithTag(HOST).onChildren().assertCountEquals(0)
    }

    @Test
    fun `screen readers hear one phrase with score and count`() {
        render(SojournRatingSamples.typical)
        composeRule.onNodeWithTag(HOST).onChildren()[0]
            .assertContentDescriptionEquals("Rated ${isolate("4.8")} out of 5, ${isolate("32")} reviews")
    }

    private companion object {
        const val HOST = "host"
    }
}

private fun androidx.compose.ui.test.SemanticsNodeInteractionCollection.assertCountEquals(expected: Int) {
    val actual = fetchSemanticsNodes().size
    check(actual == expected) { "Expected $expected rating nodes, found $actual" }
}
