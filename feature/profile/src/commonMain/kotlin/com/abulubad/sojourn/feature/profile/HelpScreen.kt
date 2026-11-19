package com.abulubad.sojourn.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.abulubad.sojourn.core.designsystem.components.divider.SojournDivider
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.help_cancel_a
import com.abulubad.sojourn.core.localization.resources.help_cancel_q
import com.abulubad.sojourn.core.localization.resources.help_deposit_a
import com.abulubad.sojourn.core.localization.resources.help_deposit_q
import com.abulubad.sojourn.core.localization.resources.help_key_a
import com.abulubad.sojourn.core.localization.resources.help_key_q
import com.abulubad.sojourn.core.localization.resources.help_offline_a
import com.abulubad.sojourn.core.localization.resources.help_offline_q
import com.abulubad.sojourn.core.localization.resources.help_price_a
import com.abulubad.sojourn.core.localization.resources.help_price_q
import com.abulubad.sojourn.core.localization.resources.profile_help
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The Help page: short answers to what guests ask most (the price, cancelling, the deposit, the key code, offline),
 * each question a heading so a screen reader can jump between them. Static text, so it needs no ViewModel.
 */
@Composable
fun HelpScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val c = SojournTheme.colors
    SettingsPage(stringResource(Res.string.profile_help), onBack, modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4)) {
            answers.forEachIndexed { index, (question, answer) ->
                if (index > 0) SojournDivider()
                Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2)) {
                    BasicText(
                        stringResource(question),
                        modifier = Modifier.semantics { heading() },
                        style = SojournTheme.type.titleSm.copy(color = c.ink),
                    )
                    BasicText(stringResource(answer), style = SojournTheme.type.body.copy(color = c.inkMuted))
                }
            }
        }
    }
}

private val answers: List<Pair<StringResource, StringResource>> = listOf(
    Res.string.help_price_q to Res.string.help_price_a,
    Res.string.help_cancel_q to Res.string.help_cancel_a,
    Res.string.help_deposit_q to Res.string.help_deposit_a,
    Res.string.help_key_q to Res.string.help_key_a,
    Res.string.help_offline_q to Res.string.help_offline_a,
)
