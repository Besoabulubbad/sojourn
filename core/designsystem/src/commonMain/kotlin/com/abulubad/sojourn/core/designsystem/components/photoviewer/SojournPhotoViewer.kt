package com.abulubad.sojourn.core.designsystem.components.photoviewer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.abulubad.sojourn.core.designsystem.components.internal.PhotoPager
import com.abulubad.sojourn.core.designsystem.components.internal.PhotoSlot
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.cd_photo_counter
import com.abulubad.sojourn.core.model.Photo
import org.jetbrains.compose.resources.stringResource

/**
 * The full-screen gallery's photos (Gallery card: "tapping opens the full-screen gallery"): every photo whole, never
 * cropped, on the screen's background, swiped in the layout direction. It starts on [initialIndex] and reports the
 * photo shown through [onPageChange], so the screen's top bar can carry the counter. Each photo is announced as
 * "Photo 3 of 13" with its caption; a photo that fails offers retry, as in the header.
 */
@Composable
fun SojournPhotoViewer(
    photos: List<Photo>,
    modifier: Modifier = Modifier,
    initialIndex: Int = 0,
    onPageChange: (index: Int) -> Unit = {},
) {
    require(photos.isNotEmpty()) { "A viewer has at least one photo slot." }
    val state = rememberPagerState(initialPage = initialIndex.coerceIn(photos.indices)) { photos.size }
    val currentOnPageChange by rememberUpdatedState(onPageChange)
    LaunchedEffect(state) { snapshotFlow { state.currentPage }.collect { currentOnPageChange(it) } }
    val formatter = rememberSojournFormatter()
    PhotoPager(
        photos = photos,
        state = state,
        preload = SojournPhotoViewerDefaults.PRELOAD,
        modifier = modifier.fillMaxSize(),
        page = { index, photo ->
            val description = stringResource(Res.string.cd_photo_counter, formatter.counter(index + 1, photos.size))
            PhotoSlot(
                photo,
                contentDescription = photo.caption?.let { "$description, ${it.text}" } ?: description,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        },
    )
}
