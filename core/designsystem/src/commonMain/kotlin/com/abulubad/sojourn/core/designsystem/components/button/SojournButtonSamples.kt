package com.abulubad.sojourn.core.designsystem.components.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_cancel_booking
import com.abulubad.sojourn.core.localization.resources.action_choose_dates
import com.abulubad.sojourn.core.localization.resources.action_edit
import com.abulubad.sojourn.core.localization.resources.action_reserve
import com.abulubad.sojourn.core.localization.resources.action_search
import com.abulubad.sojourn.core.localization.resources.action_show_stays
import com.abulubad.sojourn.core.localization.resources.stays
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** The Button card's examples, shared by previews and screenshot cases. Labels come from string resources. */
object SojournButtonSamples {

    @Composable
    fun Variants(modifier: Modifier = Modifier) {
        Column(
            modifier.padding(SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        ) {
            SojournButton(stringResource(Res.string.action_reserve), onClick = {})
            SojournButton(
                stringResource(Res.string.action_choose_dates),
                onClick = {},
                variant = SojournButtonVariant.Secondary,
            )
            SojournButton(stringResource(Res.string.action_edit), onClick = {}, variant = SojournButtonVariant.Ghost)
            SojournButton(
                stringResource(Res.string.action_cancel_booking),
                onClick = {},
                variant = SojournButtonVariant.Danger,
            )
        }
    }

    @Composable
    fun States(modifier: Modifier = Modifier) {
        Column(
            modifier.padding(SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        ) {
            SojournButton(stringResource(Res.string.action_search), onClick = {}, leadingIcon = SojournIcon.Search)
            SojournButton(stringResource(Res.string.action_reserve), onClick = {}, loading = true)
            SojournButton(stringResource(Res.string.action_reserve), onClick = {}, enabled = false)
        }
    }

    /** A label that must wrap rather than truncate in a narrow slot. */
    @Composable
    fun LongLabel(modifier: Modifier = Modifier) {
        val formatter = rememberSojournFormatter()
        val count = pluralStringResource(Res.plurals.stays, SAMPLE_STAYS, formatter.count(SAMPLE_STAYS))
        Column(modifier.padding(SojournSpacing.space4)) {
            SojournButton(
                stringResource(Res.string.action_show_stays, count),
                onClick = {},
                modifier = Modifier.width(NARROW_SLOT),
            )
        }
    }

    private val NARROW_SLOT = 120.dp

    /** "Show 14 stays", the Button card's example. */
    private const val SAMPLE_STAYS = 14
}
