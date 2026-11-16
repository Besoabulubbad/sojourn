package com.abulubad.sojourn.core.designsystem.components.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournSnackbarPreview() = SojournPreview(SojournVariants.EnLight) { SojournSnackbarSamples.SnackbarAndBanner() }

@Preview
@Composable
fun SojournSnackbarArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournSnackbarSamples.SnackbarAndBanner()
}
