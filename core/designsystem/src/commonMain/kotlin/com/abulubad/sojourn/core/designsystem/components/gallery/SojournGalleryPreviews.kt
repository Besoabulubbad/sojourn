package com.abulubad.sojourn.core.designsystem.components.gallery

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournGalleryPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournGallerySamples.States()
}

@Preview
@Composable
fun SojournGalleryArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournGallerySamples.States()
}
