package com.abulubad.sojourn.core.designsystem.theme

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.abulubad.sojourn.core.designsystem.tokens.SojournMotion

/**
 * The signature content transitions (docs/design-system/motion.md), for `AnimatedContent`. Each one follows the
 * layout direction (M-04) and becomes a `fast` crossfade under reduce motion (M-03).
 *
 * ```
 * val forward = SojournTransitions.stepForward()
 * AnimatedContent(step, transitionSpec = { forward }) { StepContent(it) }
 * ```
 *
 * The container transform (search bar to search flow, card to Details) is a shared-element transition and lives
 * with navigation in `app:shared`; the sheet, pin, save and tint motions live in their components.
 */
object SojournTransitions {

    /** The next step: the outgoing one slides toward the start edge and fades, the incoming one comes from the end. */
    @Composable
    fun stepForward(): ContentTransform = step(forward = true)

    /** The previous step: the mirror of [stepForward]. */
    @Composable
    fun stepBack(): ContentTransform = step(forward = false)

    /** A price that changes: the old value slides up and out, the new one rises from below (`base`, `standard`). */
    @Composable
    fun priceSwap(): ContentTransform {
        if (LocalReduceMotion.current) return crossfade()
        val spec = tween<IntOffset>(SojournMotion.base, easing = SojournMotion.standard)
        val fade = tween<Float>(SojournMotion.base, easing = SojournMotion.standard)
        return ContentTransform(
            targetContentEnter = slideInVertically(spec) { it } + fadeIn(fade),
            initialContentExit = slideOutVertically(spec) { -it } + fadeOut(fade),
            sizeTransform = SizeTransform(clip = true),
        )
    }

    @Composable
    private fun step(forward: Boolean): ContentTransform {
        if (LocalReduceMotion.current) return crossfade()
        // Forward enters from the end edge: the right in English, the left in Arabic.
        val endSign = if (LocalLayoutDirection.current == LayoutDirection.Ltr) 1 else -1
        val inSign = if (forward) endSign else -endSign
        val spec = tween<IntOffset>(SojournMotion.base, easing = SojournMotion.standard)
        val fade = tween<Float>(SojournMotion.base, easing = SojournMotion.standard)
        return (slideInHorizontally(spec) { inSign * it / STEP_SHIFT_DIVISOR } + fadeIn(fade))
            .togetherWith(slideOutHorizontally(spec) { -inSign * it / STEP_SHIFT_DIVISOR } + fadeOut(fade))
    }

    private fun crossfade(): ContentTransform =
        fadeIn(tween(SojournMotion.fast, easing = SojournMotion.standard))
            .togetherWith(fadeOut(tween(SojournMotion.fast, easing = SojournMotion.standard)))

    /** Steps travel a third of the width while they fade: direction is clear without a full-screen slide. */
    private const val STEP_SHIFT_DIVISOR = 3
}
