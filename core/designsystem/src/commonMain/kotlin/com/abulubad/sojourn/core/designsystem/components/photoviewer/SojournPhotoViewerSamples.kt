package com.abulubad.sojourn.core.designsystem.components.photoviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.internal.SampleListings
import com.abulubad.sojourn.core.designsystem.components.internal.WithSamplePhotos
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme

/** The viewer on its second photo, whole and uncropped. */
object SojournPhotoViewerSamples {

    @Composable
    fun SecondPhoto(modifier: Modifier = Modifier) = WithSamplePhotos {
        SojournPhotoViewer(
            SampleListings.canalPhotos,
            modifier = modifier
                .fillMaxWidth()
                .height(VIEWER_HEIGHT)
                .background(SojournTheme.colors.bg),
            initialIndex = 1,
        )
    }
}

/** A phone-height viewer, so the uncropped photo shows the space around it. */
private val VIEWER_HEIGHT = 560.dp
