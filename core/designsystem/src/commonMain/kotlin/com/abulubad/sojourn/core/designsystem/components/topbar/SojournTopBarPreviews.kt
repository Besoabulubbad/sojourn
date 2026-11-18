package com.abulubad.sojourn.core.designsystem.components.topbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournTopBarPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournTopBarSamples.States()
}

@Preview
@Composable
fun SojournTopBarArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournTopBarSamples.States()
}

@Preview
@Composable
fun SojournStepTopBarPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournTopBarSamples.Steps()
}

@Preview
@Composable
fun SojournStepTopBarArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournTopBarSamples.Steps()
}
