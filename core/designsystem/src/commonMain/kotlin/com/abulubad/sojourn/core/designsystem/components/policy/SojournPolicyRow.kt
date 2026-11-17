package com.abulubad.sojourn.core.designsystem.components.policy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.check_in_host
import com.abulubad.sojourn.core.localization.resources.check_in_lockbox
import com.abulubad.sojourn.core.localization.resources.check_in_self_check_in
import com.abulubad.sojourn.core.localization.resources.days
import com.abulubad.sojourn.core.localization.resources.nights
import com.abulubad.sojourn.core.localization.resources.policy_check_in
import com.abulubad.sojourn.core.localization.resources.policy_check_out
import com.abulubad.sojourn.core.localization.resources.policy_deposit
import com.abulubad.sojourn.core.localization.resources.policy_deposit_non_refundable
import com.abulubad.sojourn.core.localization.resources.policy_free_cancellation
import com.abulubad.sojourn.core.localization.resources.policy_free_cancellation_before
import com.abulubad.sojourn.core.localization.resources.policy_minimum_stay
import com.abulubad.sojourn.core.model.CheckInMethod
import com.abulubad.sojourn.core.model.Policy
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * One policy with the number a guest needs (PolicyRow card, N-03): cancellation states a date, the deposit an
 * amount, check-in a time and a method. `Policy` cannot be built without its number, so a vague line such as
 * "Security deposit required" cannot reach the screen.
 *
 * [detail] is an optional second line from string resources ("A key code arrives by message on the day.").
 * In a list, every row after the first passes [divider] to draw the `hairline` above it.
 */
@Composable
fun SojournPolicyRow(
    policy: Policy,
    modifier: Modifier = Modifier,
    detail: String? = null,
    divider: Boolean = false,
) {
    val c = SojournTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .then(
                if (divider) {
                    Modifier.drawBehind { drawRect(c.hairline, size = Size(size.width, 1.dp.toPx())) }
                } else {
                    Modifier
                },
            )
            .defaultMinSize(minHeight = SojournSizes.touchMin)
            .padding(vertical = SojournSpacing.space3)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        verticalAlignment = Alignment.Top,
    ) {
        SojournIcon(
            policy.icon(),
            contentDescription = null,
            tint = c.inkMuted,
            size = SojournSizes.iconSm,
            modifier = Modifier.padding(top = SojournPolicyRowDefaults.IconNudge),
        )
        Column(Modifier.weight(1f)) {
            BasicText(
                policyTitle(policy),
                style = SojournTheme.type.body.copy(color = c.ink, fontWeight = FontWeight.SemiBold),
            )
            if (detail != null) BasicText(detail, style = SojournTheme.type.bodySm.copy(color = c.inkMuted))
        }
    }
}

private fun Policy.icon(): SojournIcon = when (this) {
    is Policy.FreeCancellation, is Policy.FreeCancellationBefore -> SojournIcon.Calendar
    is Policy.Deposit -> SojournIcon.Deposit
    is Policy.CheckIn -> SojournIcon.Key
    is Policy.CheckOut -> SojournIcon.DoorOpen
    is Policy.MinimumStay -> SojournIcon.Clock
}

@Composable
private fun policyTitle(policy: Policy): String {
    val formatter = rememberSojournFormatter()
    return when (policy) {
        is Policy.FreeCancellation -> stringResource(Res.string.policy_free_cancellation, formatter.date(policy.until))
        is Policy.FreeCancellationBefore -> stringResource(
            Res.string.policy_free_cancellation_before,
            pluralStringResource(Res.plurals.days, policy.daysBeforeCheckIn, formatter.count(policy.daysBeforeCheckIn)),
        )
        is Policy.Deposit -> stringResource(
            if (policy.refundable) Res.string.policy_deposit else Res.string.policy_deposit_non_refundable,
            formatter.money(policy.amount),
        )
        is Policy.CheckIn -> stringResource(
            Res.string.policy_check_in,
            formatter.time(policy.from),
            stringResource(
                when (policy.method) {
                    CheckInMethod.SelfCheckIn -> Res.string.check_in_self_check_in
                    CheckInMethod.Host -> Res.string.check_in_host
                    CheckInMethod.Lockbox -> Res.string.check_in_lockbox
                },
            ),
        )
        is Policy.CheckOut -> stringResource(Res.string.policy_check_out, formatter.time(policy.by))
        is Policy.MinimumStay -> stringResource(
            Res.string.policy_minimum_stay,
            pluralStringResource(Res.plurals.nights, policy.nights, formatter.count(policy.nights)),
        )
    }
}
