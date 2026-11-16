package com.abulubad.sojourn.core.designsystem.components.stepper

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournStepperPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournStepperSamples.Guests()
}

@Preview
@Composable
fun SojournStepperArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournStepperSamples.Guests()
}
