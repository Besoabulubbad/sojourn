package com.abulubad.sojourn.core.designsystem.components.policy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.components.internal.SampleListings
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.check_in_self_check_in_detail
import com.abulubad.sojourn.core.localization.resources.days
import com.abulubad.sojourn.core.localization.resources.nights
import com.abulubad.sojourn.core.localization.resources.policy_cancellation_detail
import com.abulubad.sojourn.core.localization.resources.policy_deposit_detail
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** The PolicyRow card's three policies: cancellation, deposit and check-in, each with its detail line. */
object SojournPolicyRowSamples {

    private const val NON_REFUNDED_NIGHTS = 7
    private const val RELEASE_DAYS = 7

    @Composable
    fun Policies(modifier: Modifier = Modifier) {
        val formatter = rememberSojournFormatter()
        val details = listOf(
            stringResource(
                Res.string.policy_cancellation_detail,
                pluralStringResource(Res.plurals.nights, NON_REFUNDED_NIGHTS, formatter.count(NON_REFUNDED_NIGHTS)),
            ),
            stringResource(
                Res.string.policy_deposit_detail,
                pluralStringResource(Res.plurals.days, RELEASE_DAYS, formatter.count(RELEASE_DAYS)),
            ),
            stringResource(Res.string.check_in_self_check_in_detail),
        )
        Column(modifier.padding(horizontal = SojournSpacing.space4)) {
            SampleListings.canalPolicies.forEachIndexed { index, policy ->
                SojournPolicyRow(policy, detail = details[index], divider = index > 0)
            }
        }
    }
}
