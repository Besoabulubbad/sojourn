package com.abulubad.sojourn.core.designsystem.components.breakdown

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournPriceBreakdownPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournPriceBreakdownSamples.Quote()
}

@Preview
@Composable
fun SojournPriceBreakdownArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournPriceBreakdownSamples.Quote()
}
