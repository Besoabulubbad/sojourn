package com.abulubad.sojourn.core.designsystem.components.viewtoggle

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournViewTogglePreview() = SojournPreview(SojournVariants.EnLight) {
    SojournViewToggleSamples.Pills()
}

@Preview
@Composable
fun SojournViewToggleArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournViewToggleSamples.Pills()
}
