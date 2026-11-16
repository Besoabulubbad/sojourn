package com.abulubad.sojourn.core.designsystem.components.skeleton

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.abulubad.sojourn.core.designsystem.theme.LocalReduceMotion
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournMotion
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.state_loading
import org.jetbrains.compose.resources.stringResource

/**
 * A loading placeholder shaped like the content it replaces (Skeleton card). Use on first load of any list or page;
 * no full-screen spinners. `surfaceSunken` with a `hairline` pulse every 1.4 s that stands still under reduce motion
 * (M-03). Hidden from screen readers: wrap skeletons in a container that announces "Loading", as
 * [SojournStayCardSkeleton] does.
 */
@Composable
fun SojournSkeleton(modifier: Modifier = Modifier, shape: Shape = SojournShapes.sm) {
    val colors = SojournTheme.colors
    val alpha = pulseAlpha()
    Box(
        modifier
            .clearAndSetSemantics {}
            .clip(shape)
            .background(colors.surfaceSunken)
            .drawWithContent {
                drawContent()
                drawRect(colors.hairline, alpha = alpha)
            },
    )
}

/** The skeleton of a stay card: photo block and three text lines. Announces "Loading" once for the whole card. */
@Composable
fun SojournStayCardSkeleton(modifier: Modifier = Modifier) {
    val loading = stringResource(Res.string.state_loading)
    Column(
        modifier = modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = loading },
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
    ) {
        SojournSkeleton(Modifier.fillMaxWidth().height(SojournSkeletonDefaults.PhotoHeight), SojournShapes.lg)
        SojournSkeleton(
            Modifier.fillMaxWidth(
                SojournSkeletonDefaults.FIRST_LINE_FRACTION,
            ).height(SojournSkeletonDefaults.LineHeightSmall),
        )
        SojournSkeleton(
            Modifier.fillMaxWidth(
                SojournSkeletonDefaults.SECOND_LINE_FRACTION,
            ).height(SojournSkeletonDefaults.LineHeightLarge),
        )
        SojournSkeleton(
            Modifier.fillMaxWidth(
                SojournSkeletonDefaults.THIRD_LINE_FRACTION,
            ).height(SojournSkeletonDefaults.LineHeightSmall),
        )
    }
}

/** 0 → 1 → 0 over `PULSE_MILLIS` with `standard` easing; 0 (no pulse) under reduce motion. */
@Composable
private fun pulseAlpha(): Float {
    if (LocalReduceMotion.current) return 0f
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(SojournSkeletonDefaults.PULSE_MILLIS / 2, easing = SojournMotion.standard),
            RepeatMode.Reverse,
        ),
    )
    return alpha
}
