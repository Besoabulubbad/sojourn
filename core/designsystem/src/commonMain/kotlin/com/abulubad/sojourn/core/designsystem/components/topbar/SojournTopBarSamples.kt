package com.abulubad.sojourn.core.designsystem.components.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButtonTint
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_share
import com.abulubad.sojourn.core.localization.resources.action_unsave
import com.abulubad.sojourn.core.localization.resources.tab_saved
import org.jetbrains.compose.resources.stringResource

/** The TopBar card's two states: clear over a photo, and solid with a title. */
object SojournTopBarSamples {

    @Composable
    fun States(modifier: Modifier = Modifier) {
        val c = SojournTheme.colors
        Column(
            modifier.padding(SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
        ) {
            // A stand-in for a listing photo: the clear bar must stay legible on whatever is behind it.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(PHOTO_HEIGHT)
                    .clip(SojournShapes.lg)
                    .background(Brush.verticalGradient(listOf(c.brandTint, c.brand))),
            ) {
                SojournTopBar(title = null, onBack = {}, overPhoto = true, solidFraction = { 0f }) {
                    Action(SojournIcon.Share, stringResource(Res.string.action_share), onClick = {})
                    Action(
                        SojournIcon.SavedFilled,
                        stringResource(Res.string.action_unsave),
                        onClick = {},
                        tint = SojournIconButtonTint.Saved,
                    )
                }
            }
            SojournTopBar(title = stringResource(Res.string.tab_saved), onBack = {}) {
                Action(SojournIcon.Share, stringResource(Res.string.action_share), onClick = {})
            }
        }
    }

    /** The search flow's bar on its first step (Close) and its second (Back, two of three filled). */
    @Composable
    fun Steps(modifier: Modifier = Modifier) {
        Column(modifier.padding(vertical = SojournSpacing.space4)) {
            SojournStepTopBar(step = 1, steps = 3, onBack = {})
            SojournStepTopBar(step = 2, steps = 3, onBack = {})
        }
    }

    private val PHOTO_HEIGHT = 150.dp
}
