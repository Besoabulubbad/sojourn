package com.abulubad.sojourn.core.designsystem.components.internal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.theme.LocalReduceMotion
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournMotion

/** Press feedback (motion.md, "Button press"): scale to 0.98 over `dur-instant`; no movement under reduce motion. */
private const val PRESSED_SCALE = 0.98f

/** The focus ring (brand book): 2 dp solid `focus`, drawn 2 dp outside the control. */
private val FOCUS_RING_WIDTH: Dp = 2.dp
private val FOCUS_RING_OFFSET: Dp = 2.dp

/** Scales the control down slightly while pressed. */
@Composable
internal fun Modifier.pressScale(interactionSource: InteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) PRESSED_SCALE else 1f,
        animationSpec = tween(SojournMotion.instant, easing = SojournMotion.standard),
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Draws the focus ring around a control with corner radius [cornerRadius] when it has keyboard or accessibility
 * focus (C-03 companion: every interactive element is reachable and visibly focused). [cornerRadius] = null means
 * fully rounded.
 */
@Composable
internal fun Modifier.focusRing(interactionSource: InteractionSource, cornerRadius: Dp?): Modifier {
    val focused by interactionSource.collectIsFocusedAsState()
    if (!focused) return this
    val color = SojournTheme.colors.focus
    return drawWithContent {
        drawContent()
        val stroke = FOCUS_RING_WIDTH.toPx()
        val inset = FOCUS_RING_OFFSET.toPx() + stroke / 2
        val radius = cornerRadius?.toPx()?.plus(inset) ?: (size.minDimension / 2 + inset)
        drawRoundRect(
            color = color,
            topLeft = Offset(-inset, -inset),
            size = Size(size.width + inset * 2, size.height + inset * 2),
            cornerRadius = CornerRadius(radius, radius),
            style = Stroke(width = stroke),
        )
    }
}
