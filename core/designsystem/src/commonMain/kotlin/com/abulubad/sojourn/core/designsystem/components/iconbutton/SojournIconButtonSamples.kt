package com.abulubad.sojourn.core.designsystem.components.iconbutton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.cd_back
import com.abulubad.sojourn.core.localization.resources.cd_close
import com.abulubad.sojourn.core.localization.resources.cd_filters
import com.abulubad.sojourn.core.localization.resources.cd_save
import com.abulubad.sojourn.core.localization.resources.cd_share
import com.abulubad.sojourn.core.localization.resources.cd_unsave
import org.jetbrains.compose.resources.stringResource

/** The IconButton card's examples: plain, saved, outline with a badge, floating over a photo, disabled. */
object SojournIconButtonSamples {

    @Composable
    fun Variants(modifier: Modifier = Modifier) {
        FlowRow(
            modifier = modifier.padding(SojournSpacing.space4),
            horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
        ) {
            SojournIconButton(SojournIcon.Back, stringResource(Res.string.cd_back), onClick = {})
            SojournIconButton(SojournIcon.Share, stringResource(Res.string.cd_share), onClick = {})
            SojournIconButton(SojournIcon.Saved, stringResource(Res.string.cd_save), onClick = {})
            SojournIconButton(
                SojournIcon.SavedFilled,
                stringResource(Res.string.cd_unsave),
                onClick = {},
                tint = SojournIconButtonTint.Saved,
            )
            SojournIconButton(
                SojournIcon.Filters,
                stringResource(Res.string.cd_filters),
                onClick = {},
                variant = SojournIconButtonVariant.Outline,
                badge = 2,
            )
            // The floating variant sits on a photo placeholder, as on Details and the map.
            Box(Modifier.background(SojournTheme.colors.photoWall, SojournShapes.sm).padding(SojournSpacing.space1)) {
                SojournIconButton(
                    SojournIcon.Back,
                    stringResource(Res.string.cd_back),
                    onClick = {},
                    variant = SojournIconButtonVariant.Floating,
                )
            }
            SojournIconButton(SojournIcon.Close, stringResource(Res.string.cd_close), onClick = {}, enabled = false)
        }
    }
}
