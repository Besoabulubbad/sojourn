package com.abulubad.sojourn.feature.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.components.listrow.SojournChoiceRow
import com.abulubad.sojourn.core.designsystem.components.listrow.SojournListGroup
import com.abulubad.sojourn.core.designsystem.components.topbar.SojournTopBar
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.AppLocale
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.profile_appearance
import com.abulubad.sojourn.core.localization.resources.profile_language
import com.abulubad.sojourn.core.localization.resources.theme_system_detail
import com.abulubad.sojourn.core.model.ThemeMode
import com.abulubad.sojourn.core.ui.SojournScaffold
import org.jetbrains.compose.resources.stringResource

/** The Language page: English and العربية, each in its own script, no flags (ScreenProfile card). */
@Composable
fun LanguageScreen(
    state: LanguageUiState,
    onIntent: (LanguageIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsPage(stringResource(Res.string.profile_language), { onIntent(LanguageIntent.BackTapped) }, modifier) {
        SojournListGroup(choices = true) {
            AppLocale.entries.forEachIndexed { index, locale ->
                SojournChoiceRow(
                    stringResource(locale.nameResource()),
                    selected = locale == state.selected,
                    onClick = { onIntent(LanguageIntent.Chosen(locale)) },
                    divider = index > 0,
                )
            }
        }
    }
}

/** The Appearance page: System (the default, following the device), Light, Dark. */
@Composable
fun AppearanceScreen(
    state: AppearanceUiState,
    onIntent: (AppearanceIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsPage(stringResource(Res.string.profile_appearance), { onIntent(AppearanceIntent.BackTapped) }, modifier) {
        SojournListGroup(choices = true) {
            ThemeMode.entries.forEachIndexed { index, mode ->
                SojournChoiceRow(
                    stringResource(mode.nameResource()),
                    selected = mode == state.selected,
                    onClick = { onIntent(AppearanceIntent.Chosen(mode)) },
                    icon = mode.icon(),
                    detail = if (mode == ThemeMode.System) stringResource(Res.string.theme_system_detail) else null,
                    divider = index > 0,
                )
            }
        }
    }
}

/** A page under Profile: the top bar with its title and back, then its content, scrolling. */
@Composable
internal fun SettingsPage(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    SojournScaffold(modifier, topBar = { SojournTopBar(title = title, onBack = onBack) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(SojournSpacing.space4),
            content = content,
        )
    }
}
