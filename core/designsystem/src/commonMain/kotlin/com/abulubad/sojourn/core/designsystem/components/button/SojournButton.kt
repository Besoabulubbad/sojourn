package com.abulubad.sojourn.core.designsystem.components.button

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.internal.focusRing
import com.abulubad.sojourn.core.designsystem.components.internal.pressScale
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.state_loading
import org.jetbrains.compose.resources.stringResource

/**
 * The one way to trigger an action (Button card).
 *
 * - [text] comes from string resources; its label is a verb ("Reserve", "Choose dates", "Show 14 stays").
 * - At least `touch-min` tall. Labels never truncate: a long label wraps and the button grows.
 * - Prefer a working button to a disabled one ("Choose dates", not a disabled "Book now"). When disabled is
 *   unavoidable it uses `surfaceSunken` with `inkMuted` text, which still meets 4.5:1.
 * - [loading] keeps the label and width, shows a spinner and blocks a second tap.
 * - Pressing scales to 0.98 and darkens 8 % over `dur-instant`; keyboard focus draws the 2 dp `focus` ring.
 *
 * ```
 * SojournButton(stringResource(Res.string.action_reserve), onClick = { onIntent(Reserve) })
 * ```
 */
@Composable
fun SojournButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SojournButtonVariant = SojournButtonVariant.Primary,
    leadingIcon: SojournIcon? = null,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = buttonColors(variant, enabled)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = SojournShapes.md
    val loadingDescription = stringResource(Res.string.state_loading)
    Row(
        modifier = modifier
            .pressScale(interactionSource)
            .focusRing(interactionSource, cornerRadius = SojournShapes.all.getValue("radius-md"))
            .defaultMinSize(minHeight = SojournSizes.touchMin)
            .clip(shape)
            .background(colors.container)
            .then(if (colors.border != null) Modifier.border(1.dp, colors.border, shape) else Modifier)
            .background(
                if (pressed) {
                    Color.Black.copy(
                        alpha = SojournButtonDefaults.PRESSED_OVERLAY_ALPHA,
                    )
                } else {
                    Color.Transparent
                },
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !loading,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { if (loading) stateDescription = loadingDescription }
            .padding(
                horizontal = horizontalPadding(variant),
                vertical = SojournSpacing.space2,
            ),
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            loading -> Spinner(colors.content)
            leadingIcon != null -> SojournIcon(
                leadingIcon,
                contentDescription = null,
                tint = colors.content,
                size = SojournSizes.iconSm,
            )
        }
        BasicText(
            text = text,
            style = SojournTheme.type.body.copy(
                color = colors.content,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

/** `space5` side padding; the ghost variant sits inline with content and uses `space3`. */
private fun horizontalPadding(variant: SojournButtonVariant) =
    if (variant == SojournButtonVariant.Ghost) SojournSpacing.space3 else SojournSpacing.space5

private class ButtonColors(val container: Color, val content: Color, val border: Color?)

@Composable
private fun buttonColors(variant: SojournButtonVariant, enabled: Boolean): ButtonColors {
    val c = SojournTheme.colors
    if (!enabled) return ButtonColors(c.surfaceSunken, c.inkMuted, c.hairline)
    return when (variant) {
        SojournButtonVariant.Primary -> ButtonColors(c.brand, c.onBrand, null)
        SojournButtonVariant.Secondary -> ButtonColors(c.surface, c.ink, c.borderControl)
        SojournButtonVariant.Ghost -> ButtonColors(Color.Transparent, c.ink, null)
        SojournButtonVariant.Danger -> ButtonColors(c.dangerFill, c.onDanger, null)
    }
}

/** A ring with a gap at the end, turning once every `SPINNER_REVOLUTION_MILLIS`. */
@Composable
private fun Spinner(color: Color) {
    val transition = rememberInfiniteTransition()
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = FULL_TURN,
        animationSpec = infiniteRepeatable(
            tween(SojournButtonDefaults.SPINNER_REVOLUTION_MILLIS, easing = LinearEasing),
            RepeatMode.Restart,
        ),
    )
    Canvas(Modifier.size(SojournButtonDefaults.SpinnerSize).rotate(angle)) {
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = SPINNER_SWEEP,
            useCenter = false,
            style = Stroke(width = SojournButtonDefaults.SpinnerStroke.toPx(), cap = StrokeCap.Round),
        )
    }
}

private const val FULL_TURN = 360f
private const val SPINNER_SWEEP = 270f
