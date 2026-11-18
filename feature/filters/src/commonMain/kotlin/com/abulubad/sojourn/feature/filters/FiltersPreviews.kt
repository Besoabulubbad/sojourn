package com.abulubad.sojourn.feature.filters

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun FiltersScreenPreview() = SojournPreview(SojournVariants.EnLight) { FiltersSamples.Screen(FiltersSamples.card) }

@Preview
@Composable
fun FiltersScreenArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    FiltersSamples.Screen(FiltersSamples.card)
}

@Preview
@Composable
fun FiltersScreenNoMatchPreview() = SojournPreview(SojournVariants.EnLight) {
    FiltersSamples.Screen(FiltersSamples.noMatch)
}
