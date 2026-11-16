package com.abulubad.sojourn.core.designsystem.components.bottomnav

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournBottomNavPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournBottomNavSamples.Tabs()
}

@Preview
@Composable
fun SojournBottomNavArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournBottomNavSamples.Tabs()
}
