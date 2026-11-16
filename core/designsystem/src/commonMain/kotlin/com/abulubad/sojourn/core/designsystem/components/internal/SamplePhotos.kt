package com.abulubad.sojourn.core.designsystem.components.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalInspectionMode
import coil3.Image
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ImageRequest
import com.abulubad.sojourn.core.designsystem.resources.Res
import com.abulubad.sojourn.core.designsystem.resources.sample_apartment
import com.abulubad.sojourn.core.designsystem.resources.sample_bedroom
import com.abulubad.sojourn.core.designsystem.resources.sample_breakfast_table
import com.abulubad.sojourn.core.designsystem.resources.sample_dining_room
import com.abulubad.sojourn.core.designsystem.resources.sample_dining_view
import com.abulubad.sojourn.core.designsystem.resources.sample_kitchen
import com.abulubad.sojourn.core.designsystem.resources.sample_living
import com.abulubad.sojourn.core.designsystem.resources.sample_loft_bedroom
import com.abulubad.sojourn.core.designsystem.resources.sample_london
import com.abulubad.sojourn.core.designsystem.resources.sample_paris
import com.abulubad.sojourn.core.designsystem.resources.sample_reading_corner
import com.abulubad.sojourn.core.designsystem.resources.sample_window_nook
import org.jetbrains.compose.resources.imageResource

/**
 * Photos for the component samples (previews, screenshots, the showcase): CC0 images bundled with the design system
 * (THIRD_PARTY.md). A sample `Photo` uses one of these URLs, and [WithSamplePhotos] answers it from the bundle
 * through Coil's preview handler, synchronously, so screenshots never wait on the network. [FAILED] never loads.
 */
internal object SamplePhotos {
    const val LIVING = "sample://living"
    const val APARTMENT = "sample://apartment"
    const val KITCHEN = "sample://kitchen"
    const val BEDROOM = "sample://bedroom"
    const val LOFT_BEDROOM = "sample://loft_bedroom"
    const val WINDOW_NOOK = "sample://window_nook"
    const val READING_CORNER = "sample://reading_corner"
    const val BREAKFAST_TABLE = "sample://breakfast_table"
    const val DINING_VIEW = "sample://dining_view"
    const val DINING_ROOM = "sample://dining_room"
    const val LONDON = "sample://london"
    const val PARIS = "sample://paris"
    const val FAILED = "sample://failed"
}

@OptIn(ExperimentalCoilApi::class)
@Composable
internal fun WithSamplePhotos(content: @Composable () -> Unit) {
    val bitmaps = mapOf(
        SamplePhotos.LIVING to imageResource(Res.drawable.sample_living),
        SamplePhotos.APARTMENT to imageResource(Res.drawable.sample_apartment),
        SamplePhotos.KITCHEN to imageResource(Res.drawable.sample_kitchen),
        SamplePhotos.BEDROOM to imageResource(Res.drawable.sample_bedroom),
        SamplePhotos.LOFT_BEDROOM to imageResource(Res.drawable.sample_loft_bedroom),
        SamplePhotos.WINDOW_NOOK to imageResource(Res.drawable.sample_window_nook),
        SamplePhotos.READING_CORNER to imageResource(Res.drawable.sample_reading_corner),
        SamplePhotos.BREAKFAST_TABLE to imageResource(Res.drawable.sample_breakfast_table),
        SamplePhotos.DINING_VIEW to imageResource(Res.drawable.sample_dining_view),
        SamplePhotos.DINING_ROOM to imageResource(Res.drawable.sample_dining_room),
        SamplePhotos.LONDON to imageResource(Res.drawable.sample_london),
        SamplePhotos.PARIS to imageResource(Res.drawable.sample_paris),
    )
    val handler = remember(bitmaps) {
        object : AsyncImagePreviewHandler {
            override suspend fun handle(imageLoader: ImageLoader, request: ImageRequest): AsyncImagePainter.State {
                // Anything that is not a bundled sample (FAILED) goes through Coil as usual, and fails.
                val bitmap = bitmaps[request.data]
                    ?: return AsyncImagePreviewHandler.Default.handle(imageLoader, request)
                return AsyncImagePreviewHandler { bitmap.asCoilImage() }.handle(imageLoader, request)
            }
        }
    }
    // Coil consults the preview handler only in inspection mode.
    CompositionLocalProvider(
        LocalInspectionMode provides true,
        LocalAsyncImagePreviewHandler provides handler,
        content = content,
    )
}

/** Hands a decoded bundle image to Coil (Android and Skia bitmaps differ). */
internal expect fun ImageBitmap.asCoilImage(): Image
