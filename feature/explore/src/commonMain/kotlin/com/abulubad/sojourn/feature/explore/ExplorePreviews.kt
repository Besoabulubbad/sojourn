package com.abulubad.sojourn.feature.explore

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun ExploreScreenPreview() = SojournPreview(SojournVariants.EnLight) { ExploreSamples.Screen(ExploreSamples.content) }

@Preview
@Composable
fun ExploreScreenArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    ExploreSamples.Screen(ExploreSamples.content)
}

@Preview
@Composable
fun ExploreScreenOfflinePreview() = SojournPreview(SojournVariants.EnLight) {
    ExploreSamples.Screen(ExploreSamples.offlineCached)
}
