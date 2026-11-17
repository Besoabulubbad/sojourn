package com.abulubad.sojourn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme

/** The Explore tab's root until `feature:explore` replaces it: the brand name, centred on the screen colour. */
@Composable
internal fun ShellPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(SojournTheme.colors.bg), contentAlignment = Alignment.Center) {
        BasicText(text = BRAND, style = SojournTheme.type.display.copy(color = SojournTheme.colors.ink))
    }
}

/** The brand name is a mark, the same in every language. */
private const val BRAND = "Sojourn"
