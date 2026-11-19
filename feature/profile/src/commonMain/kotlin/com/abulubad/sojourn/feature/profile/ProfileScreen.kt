package com.abulubad.sojourn.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.abulubad.sojourn.core.designsystem.components.listrow.SojournListGroup
import com.abulubad.sojourn.core.designsystem.components.listrow.SojournListRow
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.AppLocale
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.language_ar
import com.abulubad.sojourn.core.localization.resources.language_en
import com.abulubad.sojourn.core.localization.resources.points
import com.abulubad.sojourn.core.localization.resources.profile_appearance
import com.abulubad.sojourn.core.localization.resources.profile_guest
import com.abulubad.sojourn.core.localization.resources.profile_guest_detail
import com.abulubad.sojourn.core.localization.resources.profile_help
import com.abulubad.sojourn.core.localization.resources.profile_language
import com.abulubad.sojourn.core.localization.resources.profile_rewards
import com.abulubad.sojourn.core.localization.resources.theme_dark
import com.abulubad.sojourn.core.localization.resources.theme_light
import com.abulubad.sojourn.core.localization.resources.theme_system
import com.abulubad.sojourn.core.model.ThemeMode
import com.abulubad.sojourn.core.ui.SojournScaffold
import com.abulubad.sojourn.core.ui.SojournTabBar
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The Profile tab (ScreenProfile card). There is no sign-in: the header says the guest's bookings and saves live on
 * this device. Rewards comes first with the balance (not a tab), then Language (named in its own script), Appearance
 * and Help. No sign-out and no currency: prices stay in each city's currency.
 */
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = rememberSojournFormatter()
    SojournScaffold(modifier, bottomBar = { SojournTabBar() }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = SojournSpacing.space4, vertical = SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space6),
        ) {
            GuestHeader()
            SojournListGroup {
                SojournListRow(
                    stringResource(Res.string.profile_rewards),
                    onClick = { onIntent(ProfileIntent.RewardsTapped) },
                    icon = SojournIcon.Rewards,
                    value = state.points?.let { pluralStringResource(Res.plurals.points, it, formatter.count(it)) },
                )
                SojournListRow(
                    stringResource(Res.string.profile_language),
                    onClick = { onIntent(ProfileIntent.LanguageTapped) },
                    icon = SojournIcon.Language,
                    value = stringResource(state.locale.nameResource()),
                    divider = true,
                )
                SojournListRow(
                    stringResource(Res.string.profile_appearance),
                    onClick = { onIntent(ProfileIntent.AppearanceTapped) },
                    icon = state.themeMode.icon(),
                    value = stringResource(state.themeMode.nameResource()),
                    divider = true,
                )
                SojournListRow(
                    stringResource(Res.string.profile_help),
                    onClick = { onIntent(ProfileIntent.HelpTapped) },
                    icon = SojournIcon.Info,
                    divider = true,
                )
            }
        }
    }
}

@Composable
private fun GuestHeader() {
    val c = SojournTheme.colors
    Row(
        Modifier.semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(SojournSizes.touchMin).clip(CircleShape).background(c.brandTint),
            contentAlignment = Alignment.Center,
        ) {
            SojournIcon(SojournIcon.Profile, contentDescription = null, tint = c.brandInk)
        }
        Column(Modifier.weight(1f)) {
            BasicText(
                stringResource(Res.string.profile_guest),
                modifier = Modifier.semantics { heading() },
                style = SojournTheme.type.titleLg.copy(color = c.ink),
            )
            BasicText(
                stringResource(Res.string.profile_guest_detail),
                style = SojournTheme.type.bodySm.copy(color = c.inkMuted),
            )
        }
    }
}

/** A language's name in its own script, the same in either UI language ("English", "العربية"). */
internal fun AppLocale.nameResource(): StringResource = when (this) {
    AppLocale.EN_GB -> Res.string.language_en
    AppLocale.AR -> Res.string.language_ar
}

internal fun ThemeMode.nameResource(): StringResource = when (this) {
    ThemeMode.System -> Res.string.theme_system
    ThemeMode.Light -> Res.string.theme_light
    ThemeMode.Dark -> Res.string.theme_dark
}

internal fun ThemeMode.icon(): SojournIcon = when (this) {
    ThemeMode.System -> SojournIcon.ThemeSystem
    ThemeMode.Light -> SojournIcon.ThemeLight
    ThemeMode.Dark -> SojournIcon.ThemeDark
}
