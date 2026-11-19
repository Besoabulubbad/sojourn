package com.abulubad.sojourn.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.abulubad.sojourn.core.designsystem.components.divider.SojournDivider
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyState
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyStateAction
import com.abulubad.sojourn.core.designsystem.components.hosttext.HostText
import com.abulubad.sojourn.core.designsystem.components.listrow.SojournListGroup
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.domain.EarnedPoints
import com.abulubad.sojourn.core.domain.Rewards
import com.abulubad.sojourn.core.localization.localizedPlace
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_explore_stays
import com.abulubad.sojourn.core.localization.resources.points
import com.abulubad.sojourn.core.localization.resources.profile_rewards
import com.abulubad.sojourn.core.localization.resources.rewards_balance
import com.abulubad.sojourn.core.localization.resources.rewards_earned
import com.abulubad.sojourn.core.localization.resources.rewards_empty_body
import com.abulubad.sojourn.core.localization.resources.rewards_empty_title
import com.abulubad.sojourn.core.localization.resources.rewards_how
import com.abulubad.sojourn.core.localization.resources.rewards_later
import com.abulubad.sojourn.core.localization.resources.rewards_stay_detail
import com.abulubad.sojourn.core.ui.ScreenState
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The Rewards page (ScreenProfile card's first row): the balance, how points are earned (a point per pound or euro
 * paid, or per 100 DA, once a stay is completed) and the stays that earned them. Spending points comes later; the
 * page says so rather than offering what does not exist.
 */
@Composable
fun RewardsScreen(
    state: RewardsUiState,
    onIntent: (RewardsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsPage(stringResource(Res.string.profile_rewards), { onIntent(RewardsIntent.BackTapped) }, modifier) {
        when (val rewards = state.rewards) {
            is ScreenState.Content -> Earned(rewards.data)
            ScreenState.Loading -> Unit
            else -> {
                SojournEmptyState(
                    icon = SojournIcon.Rewards,
                    title = stringResource(Res.string.rewards_empty_title),
                    body = stringResource(Res.string.rewards_empty_body),
                    modifier = Modifier.padding(vertical = SojournSpacing.space6),
                    action = SojournEmptyStateAction(stringResource(Res.string.action_explore_stays)) {
                        onIntent(RewardsIntent.ExploreTapped)
                    },
                )
                HowItWorks()
            }
        }
    }
}

@Composable
private fun Earned(rewards: Rewards) {
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space6)) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(SojournShapes.lg)
                .background(c.surface)
                .padding(SojournSpacing.space4)
                .semantics(mergeDescendants = true) {},
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space1),
        ) {
            BasicText(
                stringResource(Res.string.rewards_balance),
                style = SojournTheme.type.bodySm.copy(color = c.inkMuted),
            )
            BasicText(
                pluralStringResource(Res.plurals.points, rewards.points, formatter.count(rewards.points)),
                style = SojournTheme.type.titleLg.copy(color = c.ink),
            )
        }
        HowItWorks()
        Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3)) {
            BasicText(
                stringResource(Res.string.rewards_earned),
                modifier = Modifier.semantics { heading() },
                style = SojournTheme.type.titleSm.copy(color = c.ink),
            )
            SojournListGroup {
                rewards.earned.forEachIndexed { index, earned ->
                    if (index > 0) SojournDivider(Modifier.padding(horizontal = SojournSpacing.space4))
                    EarnedRow(earned)
                }
            }
        }
    }
}

@Composable
private fun HowItWorks() {
    val c = SojournTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2)) {
        BasicText(stringResource(Res.string.rewards_how), style = SojournTheme.type.body.copy(color = c.ink))
        BasicText(stringResource(Res.string.rewards_later), style = SojournTheme.type.bodySm.copy(color = c.inkMuted))
    }
}

/** A completed stay: its title as the host wrote it, where and when, and the points it earned, each on its line. */
@Composable
private fun EarnedRow(earned: EarnedPoints) {
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    val booking = earned.item.booking
    val stay = earned.item.stay
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = SojournSpacing.space4, vertical = SojournSpacing.space3)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space1),
    ) {
        if (stay != null) {
            HostText(
                stay.title,
                style = SojournTheme.type.body.copy(color = c.ink, fontWeight = FontWeight.Medium),
                maxLines = 2,
            )
        }
        val dates = formatter.dateRange(booking.dates)
        BasicText(
            if (stay != null) {
                stringResource(Res.string.rewards_stay_detail, localizedPlace(stay.cityId, stay.areaKey), dates)
            } else {
                dates
            },
            style = SojournTheme.type.bodySm.copy(color = c.inkMuted),
        )
        BasicText(
            pluralStringResource(Res.plurals.points, earned.points, formatter.count(earned.points)),
            style = SojournTheme.type.bodySm.copy(color = c.brandInk, fontWeight = FontWeight.SemiBold),
        )
    }
}
