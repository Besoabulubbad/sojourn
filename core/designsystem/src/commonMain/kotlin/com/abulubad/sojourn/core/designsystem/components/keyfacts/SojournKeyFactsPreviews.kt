package com.abulubad.sojourn.core.designsystem.components.keyfacts

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournKeyFactsPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournKeyFactsSamples.StudioAndFlat()
}

@Preview
@Composable
fun SojournKeyFactsArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournKeyFactsSamples.StudioAndFlat()
}
