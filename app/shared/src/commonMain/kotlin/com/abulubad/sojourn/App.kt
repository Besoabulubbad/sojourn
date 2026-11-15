package com.abulubad.sojourn

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Root of the shared UI. Placeholder until Phase 5 wires SojournTheme, Koin and the root NavDisplay
 * (see docs/plan.md). Uses no Material and no tokens on purpose: there is nothing to style yet.
 */
@Composable
fun App(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BasicText(text = "Sojourn")
    }
}
