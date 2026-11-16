package com.abulubad.sojourn.core.designsystem.components.pricepin

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournPricePinPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournPricePinSamples.States()
}

@Preview
@Composable
fun SojournPricePinArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournPricePinSamples.States()
}
