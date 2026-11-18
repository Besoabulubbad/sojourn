package com.abulubad.sojourn.core.designsystem.components.refresh

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournPullToRefreshPreview() = SojournPreview(SojournVariants.EnLight) { SojournPullToRefreshSamples.Refreshing() }

@Preview
@Composable
fun SojournPullToRefreshArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournPullToRefreshSamples.Refreshing()
}
