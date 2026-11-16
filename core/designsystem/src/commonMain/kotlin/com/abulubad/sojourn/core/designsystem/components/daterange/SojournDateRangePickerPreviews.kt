package com.abulubad.sojourn.core.designsystem.components.daterange

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournDateRangePickerPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournDateRangePickerSamples.March()
}

@Preview
@Composable
fun SojournDateRangePickerArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournDateRangePickerSamples.March()
}
