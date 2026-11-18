package com.abulubad.sojourn.core.designsystem.components.divider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.filters_amenities
import com.abulubad.sojourn.core.localization.resources.filters_bedrooms
import org.jetbrains.compose.resources.stringResource

/** Two filter sections and the rule between them. */
object SojournDividerSamples {

    @Composable
    fun Sections(modifier: Modifier = Modifier) {
        val style = SojournTheme.type.label.copy(color = SojournTheme.colors.inkMuted)
        Column(
            modifier.fillMaxWidth().background(SojournTheme.colors.surface).padding(SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
        ) {
            BasicText(stringResource(Res.string.filters_bedrooms), style = style)
            SojournDivider()
            BasicText(stringResource(Res.string.filters_amenities), style = style)
        }
    }
}
