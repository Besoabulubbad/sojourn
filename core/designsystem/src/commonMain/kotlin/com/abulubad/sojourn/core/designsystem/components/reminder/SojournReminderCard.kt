package com.abulubad.sojourn.core.designsystem.components.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.button.SojournButtonVariant
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_remind_me
import com.abulubad.sojourn.core.localization.resources.reminder_blocked_body
import com.abulubad.sojourn.core.localization.resources.reminder_blocked_title
import com.abulubad.sojourn.core.localization.resources.reminder_offer_body
import com.abulubad.sojourn.core.localization.resources.reminder_offer_title
import com.abulubad.sojourn.core.localization.resources.reminder_set_body
import com.abulubad.sojourn.core.localization.resources.reminder_set_title
import org.jetbrains.compose.resources.stringResource

/**
 * The arrival reminder on the booking confirmation and a booking's page: a local notification the evening before
 * check-in, offered with its reason, never sent anywhere. Offer has "Remind me" (the system then asks once); Set says
 * when it will show; Blocked says notifications are off in the phone's settings. Changes are announced.
 */
@Composable
fun SojournReminderCard(state: SojournReminderState, onRemind: () -> Unit, modifier: Modifier = Modifier) {
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    val (title, body) = when (state) {
        SojournReminderState.Offer ->
            stringResource(Res.string.reminder_offer_title) to stringResource(Res.string.reminder_offer_body)
        is SojournReminderState.Set -> stringResource(Res.string.reminder_set_title) to stringResource(
            Res.string.reminder_set_body,
            formatter.date(state.at.date),
            formatter.time(state.at.time),
        )
        SojournReminderState.Blocked ->
            stringResource(Res.string.reminder_blocked_title) to stringResource(Res.string.reminder_blocked_body)
    }
    Column(
        modifier
            .fillMaxWidth()
            .clip(SojournShapes.lg)
            .background(c.surface)
            .padding(SojournSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
    ) {
        Row(
            Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
            horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
            verticalAlignment = Alignment.Top,
        ) {
            SojournIcon(SojournIcon.Clock, contentDescription = null, tint = c.brandInk, size = SojournSizes.iconSm)
            Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space1)) {
                BasicText(title, style = SojournTheme.type.body.copy(color = c.ink, fontWeight = FontWeight.SemiBold))
                BasicText(body, style = SojournTheme.type.bodySm.copy(color = c.inkMuted))
            }
        }
        if (state == SojournReminderState.Offer) {
            SojournButton(
                stringResource(Res.string.action_remind_me),
                onClick = onRemind,
                modifier = Modifier.fillMaxWidth(),
                variant = SojournButtonVariant.Secondary,
            )
        }
    }
}
