package com.abulubad.sojourn.core.designsystem.components.segmented

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournSegmentedControlPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournSegmentedControlSamples.Rows()
}

@Preview
@Composable
fun SojournSegmentedControlArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournSegmentedControlSamples.Rows()
}
