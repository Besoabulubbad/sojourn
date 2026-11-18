package com.abulubad.sojourn.feature.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun DetailsScreenPreview() = SojournPreview(SojournVariants.EnLight) { DetailsSamples.Screen(DetailsSamples.withDates) }

@Preview
@Composable
fun DetailsScreenArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    DetailsSamples.Screen(DetailsSamples.withDates, fraction = 0.5f)
}

@Preview
@Composable
fun DetailsScreenNoDatesPreview() = SojournPreview(SojournVariants.ArLight) {
    DetailsSamples.Screen(DetailsSamples.withoutDates, fraction = 1f)
}

@Preview
@Composable
fun GalleryScreenPreview() = SojournPreview(SojournVariants.EnDark) { DetailsSamples.Gallery() }
