package com.abulubad.sojourn.core.designsystem.components.bookingbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournBookingBarPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournBookingBarSamples.States()
}

@Preview
@Composable
fun SojournBookingBarArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournBookingBarSamples.States()
}
