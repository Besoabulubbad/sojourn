package com.abulubad.sojourn.core.designsystem.preview

import androidx.compose.runtime.Composable
import com.abulubad.sojourn.core.designsystem.components.internal.SamplePhotos
import com.abulubad.sojourn.core.designsystem.components.internal.WithSamplePhotos

/**
 * Photo URLs for screen previews and screenshots in feature modules. Inside [WithSojournSamplePhotos] they load
 * synchronously from images bundled with the design system, so a screenshot never waits on the network;
 * [FAILED] never loads, to show a slot's failed state.
 */
object SojournSamplePhotos {
    const val LONDON = SamplePhotos.LONDON
    const val PARIS = SamplePhotos.PARIS
    const val ALGIERS = SamplePhotos.ALGIERS
    const val LIVING = SamplePhotos.LIVING
    const val APARTMENT = SamplePhotos.APARTMENT
    const val KITCHEN = SamplePhotos.KITCHEN
    const val BEDROOM = SamplePhotos.BEDROOM
    const val FAILED = SamplePhotos.FAILED
}

/** Answers [SojournSamplePhotos] URLs from the bundle for everything inside [content]. */
@Composable
fun WithSojournSamplePhotos(content: @Composable () -> Unit) = WithSamplePhotos(content)
