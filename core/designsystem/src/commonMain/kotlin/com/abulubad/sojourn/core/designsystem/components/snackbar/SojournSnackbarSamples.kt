package com.abulubad.sojourn.core.designsystem.components.snackbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_retry
import com.abulubad.sojourn.core.localization.resources.action_undo
import com.abulubad.sojourn.core.localization.resources.offline_banner
import com.abulubad.sojourn.core.localization.resources.saved_removed
import org.jetbrains.compose.resources.stringResource

/** The Snackbar card's examples: an undo snackbar and the offline banner. */
object SojournSnackbarSamples {
    @Composable
    fun SnackbarAndBanner(modifier: Modifier = Modifier) {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4)) {
            SojournSnackbar(
                message = stringResource(Res.string.saved_removed),
                modifier = Modifier.padding(SojournSpacing.space4),
                actionLabel = stringResource(Res.string.action_undo),
            )
            SojournOfflineBanner(
                message = stringResource(Res.string.offline_banner),
                actionLabel = stringResource(Res.string.action_retry),
            )
        }
    }
}
