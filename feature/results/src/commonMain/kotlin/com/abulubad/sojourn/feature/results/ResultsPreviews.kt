package com.abulubad.sojourn.feature.results

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun ResultsScreenPreview() = SojournPreview(SojournVariants.EnLight) { ResultsSamples.Screen(ResultsSamples.content) }

@Preview
@Composable
fun ResultsScreenArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    ResultsSamples.Screen(ResultsSamples.content)
}

@Preview
@Composable
fun ResultsScreenOfflinePreview() = SojournPreview(SojournVariants.EnLight) {
    ResultsSamples.Screen(ResultsSamples.offline)
}
