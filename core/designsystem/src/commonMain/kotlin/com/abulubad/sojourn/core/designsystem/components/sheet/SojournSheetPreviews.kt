package com.abulubad.sojourn.core.designsystem.components.sheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournSheetPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournSheetSamples.Filters()
}

@Preview
@Composable
fun SojournSheetArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournSheetSamples.Filters()
}

@Preview
@Composable
fun SojournSheetPeekPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournSheetSamples.Peek()
}
