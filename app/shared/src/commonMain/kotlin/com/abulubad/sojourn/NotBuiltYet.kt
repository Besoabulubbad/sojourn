package com.abulubad.sojourn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme

/**
 * What a destination shows until its feature lands (Phase 5 builds them one PR at a time): the brand name on the
 * screen colour. System back returns from it. Removed when the last feature is merged.
 */
@Composable
internal fun NotBuiltYet(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(SojournTheme.colors.bg), contentAlignment = Alignment.Center) {
        BasicText(text = BRAND, style = SojournTheme.type.display.copy(color = SojournTheme.colors.ink))
    }
}

/** The brand name is a mark, the same in every language. */
private const val BRAND = "Sojourn"
