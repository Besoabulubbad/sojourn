package com.abulubad.sojourn.core.designsystem.components.emptystate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_clear_filters
import com.abulubad.sojourn.core.localization.resources.empty_no_matches_body
import com.abulubad.sojourn.core.localization.resources.empty_no_matches_title
import org.jetbrains.compose.resources.stringResource

/** The EmptyState card's example: no stays match the search. */
object SojournEmptyStateSamples {
    @Composable
    fun NoMatches(modifier: Modifier = Modifier) {
        SojournEmptyState(
            icon = SojournIcon.NoResults,
            title = stringResource(Res.string.empty_no_matches_title),
            body = stringResource(Res.string.empty_no_matches_body),
            modifier = modifier,
            action = SojournEmptyStateAction(stringResource(Res.string.action_clear_filters)) {},
        )
    }
}
