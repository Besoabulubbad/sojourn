package com.abulubad.sojourn.core.designsystem.components.topbar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButton
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButtonTint
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButtonVariant
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.LocalReduceMotion
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournMotion
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.cd_back
import com.abulubad.sojourn.core.localization.resources.cd_close
import com.abulubad.sojourn.core.localization.resources.cd_step_progress
import org.jetbrains.compose.resources.stringResource

/**
 * The screen's top bar (TopBar card). It owns the status-bar inset: screens never add their own top padding (I-01).
 *
 * - **Solid** (the default): `bg` fill, a bottom `hairline` and the [title].
 * - **Over a photo** ([overPhoto]): while [solidFraction] is 0 the bar is clear, its buttons are floating discs,
 *   and `scrimTop` sits behind the status bar so the system icons stay legible on any photo (I-02). As content
 *   scrolls, [solidFraction] rises to 1 and the bar fills in; the title appears only once the bar is solid, so
 *   text never sits on a clear bar. Feed it from [rememberTopBarSolidFraction].
 *
 * [solidFraction] is a lambda so scrolling redraws the bar without recomposing the screen.
 *
 * ```
 * val fraction = rememberTopBarSolidFraction(listState)
 * SojournTopBar(title = stay.title, onBack = onBack, overPhoto = true, solidFraction = fraction) {
 *     Action(SojournIcon.Share, stringResource(Res.string.action_share), onClick = onShare)
 * }
 * ```
 */
@Composable
fun SojournTopBar(
    title: String?,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    overPhoto: Boolean = false,
    solidFraction: () -> Float = { 1f },
    actions: @Composable SojournTopBarActions.() -> Unit = {},
) {
    val c = SojournTheme.colors
    val fraction = { if (overPhoto) solidFraction().coerceIn(0f, 1f) else 1f }
    // Only the switch between clear and solid recomposes; the fill and title alpha are read while drawing.
    val clear by remember(overPhoto, solidFraction) {
        derivedStateOf { overPhoto && solidFraction() < SojournTopBarDefaults.SOLID_SWITCH }
    }
    val scope = SojournTopBarActions(floating = clear)
    val statusBar = WindowInsets.statusBars
    Box(
        modifier
            .fillMaxWidth()
            .drawBehind {
                val f = fraction()
                drawRect(c.bg.copy(alpha = c.bg.alpha * f))
                // scrimTop protects the status bar only; the bar itself stays clear over the photo.
                val statusHeight = statusBar.getTop(this).toFloat()
                if (overPhoto && f < 1f && statusHeight > 0f) {
                    drawRect(
                        c.scrimTop.copy(alpha = c.scrimTop.alpha * (1f - f)),
                        size = Size(size.width, statusHeight),
                    )
                }
                val hairline = 1.dp.toPx()
                drawRect(
                    color = c.hairline.copy(alpha = c.hairline.alpha * f),
                    topLeft = Offset(0f, size.height - hairline),
                    size = Size(size.width, hairline),
                )
            }
            .windowInsetsPadding(statusBar),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(SojournSizes.barTop)
                .padding(horizontal = SojournSpacing.space1),
            horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space1),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) scope.Action(SojournIcon.Back, stringResource(Res.string.cd_back), onClick = onBack)
            Box(Modifier.weight(1f).padding(horizontal = SojournSpacing.space1)) {
                if (title != null && !clear) {
                    BasicText(
                        text = title,
                        modifier = Modifier
                            .graphicsLayer { alpha = fraction() }
                            .semantics { heading() },
                        style = SojournTheme.type.titleSm.copy(color = c.ink),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            scope.actions()
        }
    }
}

/**
 * The top bar of a full-screen flow taken one step at a time (ScreenSearchFlow card): no title, a progress bar of
 * [steps] segments with the first [step] filled, and one button that leaves the step. It owns the status-bar inset,
 * like [SojournTopBar].
 *
 * - On the first step the button is Close (it leaves the flow); after it, Back (one step back, mirrored in RTL).
 *   Both call [onBack], the same action as the system back gesture.
 * - A newly reached segment fills from the start edge over `base` (`ease-standard`), at once under reduce motion.
 * - Screen readers hear "Step 2 of 3" for the bar; the segments themselves are not read.
 */
@Composable
fun SojournStepTopBar(step: Int, steps: Int, onBack: () -> Unit, modifier: Modifier = Modifier) {
    require(step in 1..steps) { "Step $step is not within 1..$steps" }
    val c = SojournTheme.colors
    val first = step == 1
    Box(modifier.fillMaxWidth().background(c.bg).windowInsetsPadding(WindowInsets.statusBars)) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(SojournSizes.barTop)
                .padding(horizontal = SojournSpacing.space1),
            horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space1),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SojournIconButton(
                icon = if (first) SojournIcon.Close else SojournIcon.Back,
                contentDescription = stringResource(if (first) Res.string.cd_close else Res.string.cd_back),
                onClick = onBack,
            )
            StepProgress(step, steps, Modifier.weight(1f).padding(horizontal = SojournSpacing.space2))
            // Balances the button, so the progress bar sits in the middle of the screen.
            Spacer(Modifier.size(SojournSizes.touchMin))
        }
    }
}

@Composable
private fun StepProgress(step: Int, steps: Int, modifier: Modifier) {
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    val description = stringResource(Res.string.cd_step_progress, formatter.count(step), formatter.count(steps))
    val fillSpec = if (LocalReduceMotion.current) {
        snap()
    } else {
        tween<Float>(SojournMotion.base, easing = SojournMotion.standard)
    }
    Row(
        modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space1),
    ) {
        repeat(steps) { index ->
            val fill by animateFloatAsState(
                targetValue = if (index < step) 1f else 0f,
                animationSpec = fillSpec,
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(SojournTopBarDefaults.StepHeight)
                    .clip(SojournShapes.full)
                    // `borderControl`, not a sunken fill: the steps still to come must be visible in dark mode too.
                    .background(c.borderControl)
                    .drawBehind {
                        val width = size.width * fill
                        // The fill grows from the start edge: the left in English, the right in Arabic.
                        val x = if (layoutDirection == LayoutDirection.Ltr) 0f else size.width - width
                        drawRect(c.brand, topLeft = Offset(x, 0f), size = Size(width, size.height))
                    },
            )
        }
    }
}

/**
 * The actions a [SojournTopBar] may show. [Action] picks the right button for the bar's state: a floating disc
 * while the bar is clear over a photo, a plain icon button once it is solid.
 */
@Immutable
class SojournTopBarActions internal constructor(private val floating: Boolean) {

    /** One action; [contentDescription] names it ("Share"), [tint] marks a saved heart. */
    @Composable
    fun Action(
        icon: SojournIcon,
        contentDescription: String,
        onClick: () -> Unit,
        tint: SojournIconButtonTint = SojournIconButtonTint.Default,
    ) {
        SojournIconButton(
            icon = icon,
            contentDescription = contentDescription,
            onClick = onClick,
            variant = if (floating) SojournIconButtonVariant.Floating else SojournIconButtonVariant.Plain,
            tint = tint,
        )
    }
}
