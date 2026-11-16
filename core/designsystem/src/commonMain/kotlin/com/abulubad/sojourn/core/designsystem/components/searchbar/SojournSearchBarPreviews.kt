package com.abulubad.sojourn.core.designsystem.components.searchbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournSearchBarPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournSearchBarSamples.Forms()
}

@Preview
@Composable
fun SojournSearchBarArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournSearchBarSamples.Forms()
}
