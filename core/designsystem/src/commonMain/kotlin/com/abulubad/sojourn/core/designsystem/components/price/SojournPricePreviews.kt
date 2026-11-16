package com.abulubad.sojourn.core.designsystem.components.price

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing

@Preview
@Composable
fun SojournPriceDatedPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournPrice(SojournPriceSamples.dated, Modifier.padding(SojournSpacing.space4))
}

@Preview
@Composable
fun SojournPriceFromPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournPrice(SojournPriceSamples.fromMonth, Modifier.padding(SojournSpacing.space4))
}

@Preview
@Composable
fun SojournPriceCompactPreview() = SojournPreview(SojournVariants.EnDark) {
    SojournPrice(SojournPriceSamples.dated, Modifier.padding(SojournSpacing.space4), size = SojournPriceSize.Compact)
}
