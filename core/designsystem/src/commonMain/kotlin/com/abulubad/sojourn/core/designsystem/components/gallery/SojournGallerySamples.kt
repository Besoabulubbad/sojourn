package com.abulubad.sojourn.core.designsystem.components.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.components.internal.SampleListings
import com.abulubad.sojourn.core.designsystem.components.internal.SamplePhotos
import com.abulubad.sojourn.core.designsystem.components.internal.WithSamplePhotos
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.model.Photo
import com.abulubad.sojourn.core.model.PhotoId

/** The Gallery card's two states: a loaded header with its counter, and a photo that failed with retry. */
object SojournGallerySamples {

    @Composable
    fun States(modifier: Modifier = Modifier) = WithSamplePhotos {
        Column(
            modifier.padding(vertical = SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
        ) {
            SojournGallery(SampleListings.canalPhotos, onOpen = {})
            SojournGallery(listOf(Photo(PhotoId("failed"), SamplePhotos.FAILED)), onOpen = {})
        }
    }
}
