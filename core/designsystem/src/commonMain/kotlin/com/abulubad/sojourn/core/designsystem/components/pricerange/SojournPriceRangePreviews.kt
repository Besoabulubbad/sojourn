package com.abulubad.sojourn.core.designsystem.components.pricerange

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournPriceRangePreview() = SojournPreview(SojournVariants.EnLight) {
    SojournPriceRangeSamples.Total()
}

@Preview
@Composable
fun SojournPriceRangeArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournPriceRangeSamples.Total()
}
