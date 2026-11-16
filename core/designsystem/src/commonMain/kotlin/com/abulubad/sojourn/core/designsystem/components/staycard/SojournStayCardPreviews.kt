package com.abulubad.sojourn.core.designsystem.components.staycard

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournStayCardPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournStayCardSamples.Full()
}

@Preview
@Composable
fun SojournStayCardArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournStayCardSamples.Full()
}
