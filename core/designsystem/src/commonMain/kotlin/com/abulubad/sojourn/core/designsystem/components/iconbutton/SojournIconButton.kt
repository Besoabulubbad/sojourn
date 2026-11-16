package com.abulubad.sojourn.core.designsystem.components.iconbutton

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.internal.focusRing
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.localization.rememberSojournFormatter

/**
 * An icon-only action with a 48 dp target (IconButton card).
 *
 * - [contentDescription] is required and localized (C-05): a call without it does not compile.
 * - Directional icons mirror inside [SojournIcon]; call sites never flip them.
 * - Over a photo or the map use [SojournIconButtonVariant.Floating]: it carries its own `surface` disc.
 * - [badge] shows a count at the top-end corner (the number of active filters); screen readers hear it after the
 *   description. The badge text is `label-sm`, the brand book's smallest size (the card's 11 px is below it).
 */
@Composable
fun SojournIconButton(
    icon: SojournIcon,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SojournIconButtonVariant = SojournIconButtonVariant.Plain,
    tint: SojournIconButtonTint = SojournIconButtonTint.Default,
    badge: Int? = null,
    enabled: Boolean = true,
) {
    val c = SojournTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val formatter = rememberSojournFormatter()
    val badgeText = badge?.takeIf { it > 0 }?.let { formatter.count(it) }
    val badgeStyle = with(LocalDensity.current) {
        val base = SojournTheme.type.labelSm
        base.copy(fontSize = base.fontSize.value.dp.toSp(), lineHeight = base.lineHeight.value.dp.toSp())
    }
    val iconColor = when {
        !enabled -> c.inkSubtle
        tint == SojournIconButtonTint.Saved -> c.save
        else -> c.ink
    }
    Box(
        modifier = modifier
            .size(SojournSizes.touchMin)
            .focusRing(interactionSource, cornerRadius = null)
            .clip(SojournShapes.full)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                this.contentDescription = contentDescription
                if (badgeText != null) stateDescription = badgeText
            },
        contentAlignment = Alignment.Center,
    ) {
        val disc = when (variant) {
            SojournIconButtonVariant.Plain -> Modifier
            SojournIconButtonVariant.Outline ->
                Modifier
                    .size(SojournSizes.touchMin)
                    .clip(SojournShapes.full)
                    .background(c.surface)
                    .border(1.dp, if (enabled) c.borderControl else c.hairline, SojournShapes.full)
            SojournIconButtonVariant.Floating ->
                Modifier
                    .size(SojournIconButtonDefaults.FloatingDisc)
                    .shadow(
                        SojournTheme.elevation.level2.elevation,
                        SojournShapes.full,
                        ambientColor = SojournTheme.elevation.level2.color,
                        spotColor = SojournTheme.elevation.level2.color,
                    )
                    .background(c.surface, SojournShapes.full)
        }
        Box(disc, contentAlignment = Alignment.Center) {
            SojournIcon(icon, contentDescription = null, tint = iconColor)
        }
        if (badgeText != null) {
            BasicText(
                text = badgeText,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(SojournIconButtonDefaults.BadgeInset)
                    .defaultMinSize(SojournIconButtonDefaults.BadgeMinSize, SojournIconButtonDefaults.BadgeMinSize)
                    .background(c.brand, SojournShapes.full)
                    .padding(horizontal = SojournIconButtonDefaults.BadgePadding),
                // The badge is part of the icon, which does not scale with text: at 200 % it would cover the icon.
                // The count is still read out through the button's state description.
                style = badgeStyle.copy(
                    color = c.onBrand,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}
