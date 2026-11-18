package com.abulubad.sojourn.core.designsystem.components.continuerow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.abulubad.sojourn.core.designsystem.components.internal.PhotoSlot
import com.abulubad.sojourn.core.designsystem.components.internal.focusRing
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.model.Photo

/**
 * A row that resumes something (Explore's "Continue your search", or the next upcoming stay): a small photo, a
 * title, a one-line-or-more subtitle and a forward chevron that follows reading direction (L-05). The whole row is
 * one button, read as title then subtitle. The photo slot shows placeholder, loaded and failed-with-retry states.
 */
@Composable
fun SojournContinueRow(
    photo: Photo?,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = SojournTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = SojournSizes.touchMin)
            .focusRing(interactionSource, cornerRadius = SojournShapes.all.getValue("radius-lg"))
            .clip(SojournShapes.lg)
            .background(c.surface)
            .clickable(
                role = Role.Button,
                indication = ripple(),
                interactionSource = interactionSource,
                onClick = onClick,
            )
            .padding(SojournSpacing.space3),
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (photo != null) {
            PhotoSlot(
                photo,
                contentDescription = null,
                modifier = Modifier.size(SojournContinueRowDefaults.PhotoSize).clip(SojournShapes.md),
                showFailureText = false,
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SojournSpacing.space1)) {
            BasicText(
                title,
                style = SojournTheme.type.body.copy(color = c.ink, fontWeight = FontWeight.SemiBold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                subtitle,
                style = SojournTheme.type.bodySm.copy(color = c.inkMuted),
                // Dates, nights and guests stay visible with large text.
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        SojournIcon(SojournIcon.ChevronEnd, contentDescription = null, tint = c.inkMuted, size = SojournSizes.iconSm)
    }
}
