package com.abulubad.sojourn.core.designsystem.components.listrow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.language_en
import com.abulubad.sojourn.core.localization.resources.points
import com.abulubad.sojourn.core.localization.resources.profile_appearance
import com.abulubad.sojourn.core.localization.resources.profile_language
import com.abulubad.sojourn.core.localization.resources.profile_rewards
import com.abulubad.sojourn.core.localization.resources.theme_dark
import com.abulubad.sojourn.core.localization.resources.theme_light
import com.abulubad.sojourn.core.localization.resources.theme_system
import com.abulubad.sojourn.core.localization.resources.theme_system_detail
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** Profile's rows with their values, and Appearance's choices with System selected. */
object SojournListRowSamples {

    private const val POINTS = 1_240

    @Composable
    fun Rows(modifier: Modifier = Modifier) {
        val formatter = rememberSojournFormatter()
        Column(
            modifier.padding(SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
        ) {
            SojournListGroup {
                SojournListRow(
                    stringResource(Res.string.profile_rewards),
                    onClick = {},
                    icon = SojournIcon.Rewards,
                    value = pluralStringResource(Res.plurals.points, POINTS, formatter.count(POINTS)),
                )
                SojournListRow(
                    stringResource(Res.string.profile_language),
                    onClick = {},
                    icon = SojournIcon.Language,
                    value = stringResource(Res.string.language_en),
                    divider = true,
                )
                SojournListRow(
                    stringResource(Res.string.profile_appearance),
                    onClick = {},
                    icon = SojournIcon.ThemeSystem,
                    value = stringResource(Res.string.theme_system),
                    divider = true,
                )
            }
            SojournListGroup(choices = true) {
                SojournChoiceRow(
                    stringResource(Res.string.theme_system),
                    selected = true,
                    onClick = {},
                    icon = SojournIcon.ThemeSystem,
                    detail = stringResource(Res.string.theme_system_detail),
                )
                SojournChoiceRow(
                    stringResource(Res.string.theme_light),
                    selected = false,
                    onClick = {},
                    icon = SojournIcon.ThemeLight,
                    divider = true,
                )
                SojournChoiceRow(
                    stringResource(Res.string.theme_dark),
                    selected = false,
                    onClick = {},
                    icon = SojournIcon.ThemeDark,
                    divider = true,
                )
            }
        }
    }
}
