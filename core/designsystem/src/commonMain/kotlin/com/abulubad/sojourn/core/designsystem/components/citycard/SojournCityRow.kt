package com.abulubad.sojourn.core.designsystem.components.citycard

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
import com.abulubad.sojourn.core.designsystem.components.internal.PhotoSlot
import com.abulubad.sojourn.core.designsystem.components.internal.focusRing
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.localizedCityName
import com.abulubad.sojourn.core.model.City

/**
 * A city in the search flow's Where list (ScreenSearchFlow card): the [SojournCityCard] facts in a row, with a
 * square photo, the localized name (S-05), "214 stays · from £1,950 a month" and a forward chevron that follows
 * reading direction (L-05). The whole row is one button, read as name then summary.
 */
@Composable
fun SojournCityRow(city: City, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = SojournTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = SojournSizes.touchMin)
            .focusRing(interactionSource, cornerRadius = SojournShapes.all.getValue("radius-md"))
            .clip(SojournShapes.md)
            .clickable(
                role = Role.Button,
                indication = ripple(),
                interactionSource = interactionSource,
                onClick = onClick,
            )
            .padding(vertical = SojournSpacing.space2),
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PhotoSlot(
            city.photo,
            contentDescription = null,
            modifier = Modifier.size(SojournCityCardDefaults.RowPhotoSize).clip(SojournShapes.md),
            showFailureText = false,
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SojournSpacing.space1)) {
            BasicText(
                localizedCityName(city.id),
                style = SojournTheme.type.body.copy(color = c.ink, fontWeight = FontWeight.SemiBold),
            )
            BasicText(citySummary(city), style = SojournTheme.type.bodySm.copy(color = c.inkMuted))
        }
        SojournIcon(SojournIcon.ChevronEnd, contentDescription = null, tint = c.inkMuted, size = SojournSizes.iconSm)
    }
}
