package com.abulubad.sojourn.core.designsystem.components.photoviewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournPhotoViewerPreview() = SojournPreview(SojournVariants.EnLight) { SojournPhotoViewerSamples.SecondPhoto() }

@Preview
@Composable
fun SojournPhotoViewerArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournPhotoViewerSamples.SecondPhoto()
}
