package com.abulubad.sojourn.core.designsystem.components.citycard

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournCityCardPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournCityCardSamples.Cities()
}

@Preview
@Composable
fun SojournCityCardArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournCityCardSamples.Cities()
}

@Preview
@Composable
fun SojournCityRowPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournCityCardSamples.Rows()
}

@Preview
@Composable
fun SojournCityRowArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournCityCardSamples.Rows()
}
