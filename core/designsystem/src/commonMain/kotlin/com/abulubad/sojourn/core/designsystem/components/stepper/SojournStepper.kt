package com.abulubad.sojourn.core.designsystem.components.stepper

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.internal.focusRing
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.theme.rememberSojournHaptics
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.cd_decrease_label
import com.abulubad.sojourn.core.localization.resources.cd_increase_label
import org.jetbrains.compose.resources.stringResource

/**
 * A row with plus and minus for small counts: guests, beds, bathrooms (Stepper card).
 *
 * - [label] (and an optional [hint] such as "Ages 2 to 12") appears once, at the start of the row; the buttons
 *   carry it only in their descriptions ("Increase Adults").
 * - At a bound of [range] that button is disabled, and announced as such.
 * - Each button is a 40 dp disc in a 48 dp target; the value is announced politely when it changes.
 */
@Composable
fun SojournStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    label: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
) {
    val haptics = rememberSojournHaptics()
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            BasicText(label, style = SojournTheme.type.body.copy(color = c.ink, fontWeight = FontWeight.SemiBold))
            if (hint != null) BasicText(hint, style = SojournTheme.type.bodySm.copy(color = c.inkMuted))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepButton(
                icon = SojournIcon.Minus,
                description = stringResource(Res.string.cd_decrease_label, label),
                enabled = value > range.first,
                onClick = {
                    haptics.tick()
                    onValueChange(value - 1)
                },
            )
            BasicText(
                formatter.count(value),
                modifier = Modifier
                    .widthIn(min = SojournStepperDefaults.ValueMinWidth)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                style = SojournTheme.type.body.copy(
                    color = c.ink,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                ),
            )
            StepButton(
                icon = SojournIcon.Plus,
                description = stringResource(Res.string.cd_increase_label, label),
                enabled = value < range.last,
                onClick = {
                    haptics.tick()
                    onValueChange(value + 1)
                },
            )
        }
    }
}

@Composable
private fun StepButton(icon: SojournIcon, description: String, enabled: Boolean, onClick: () -> Unit) {
    val c = SojournTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(SojournSizes.touchMin)
            .focusRing(interactionSource, cornerRadius = null)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = SojournStepperDefaults.ButtonSize / 2),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(SojournStepperDefaults.ButtonSize)
                .background(c.surface, SojournShapes.full)
                .border(1.dp, if (enabled) c.borderControl else c.hairline, SojournShapes.full),
            contentAlignment = Alignment.Center,
        ) {
            SojournIcon(
                icon,
                contentDescription = null,
                tint = if (enabled) c.ink else c.inkSubtle,
                size = SojournSizes.iconSm,
            )
        }
    }
}
