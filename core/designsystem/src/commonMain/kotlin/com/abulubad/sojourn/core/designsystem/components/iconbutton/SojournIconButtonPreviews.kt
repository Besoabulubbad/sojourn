package com.abulubad.sojourn.core.designsystem.components.iconbutton

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournIconButtonVariantsPreview() = SojournPreview(SojournVariants.EnLight) { SojournIconButtonSamples.Variants() }

@Preview
@Composable
fun SojournIconButtonVariantsArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournIconButtonSamples.Variants()
}
