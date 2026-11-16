package com.abulubad.sojourn.core.designsystem.components.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import com.abulubad.sojourn.core.designsystem.components.internal.PhotoPager
import com.abulubad.sojourn.core.designsystem.components.internal.PhotoSlot
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.cd_photo_counter
import com.abulubad.sojourn.core.model.Photo
import org.jetbrains.compose.resources.stringResource

/**
 * The swipeable photo header on Details (Gallery card). Full width at 3:2.
 *
 * - Every photo has three states: placeholder, loaded, failed with retry. The next two photos preload.
 * - Swipe follows reading direction: in Arabic the next photo comes from the left.
 * - The counter "1 / 13" is a bidi isolate, so it reads the same in Arabic (S-04); each photo is announced as
 *   "Photo 1 / 13", with its caption when the host wrote one.
 * - Tapping a photo calls [onOpen] with its index; the full-screen gallery arrives with the Details screen.
 */
@Composable
fun SojournGallery(photos: List<Photo>, onOpen: (index: Int) -> Unit, modifier: Modifier = Modifier) {
    require(photos.isNotEmpty()) { "A gallery has at least one photo slot." }
    val formatter = rememberSojournFormatter()
    PhotoPager(
        photos = photos,
        preload = SojournGalleryDefaults.PRELOAD,
        modifier = modifier.fillMaxWidth().aspectRatio(SojournGalleryDefaults.ASPECT_RATIO),
        page = { index, photo ->
            val counter = formatter.counter(index + 1, photos.size)
            val description = stringResource(Res.string.cd_photo_counter, counter)
            PhotoSlot(
                photo,
                contentDescription = photo.caption?.let { "$description, ${it.text}" } ?: description,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(role = Role.Button, onClick = { onOpen(index) }),
            )
        },
    ) { state ->
        Counter(
            formatter.counter(state.currentPage + 1, photos.size),
            Modifier.align(Alignment.BottomEnd).padding(SojournSpacing.space3),
        )
    }
}

@Composable
private fun Counter(text: String, modifier: Modifier) {
    BasicText(
        text,
        modifier = modifier
            .clearAndSetSemantics {}
            .background(SojournTheme.colors.scrim, SojournShapes.full)
            .padding(horizontal = SojournSpacing.space2, vertical = SojournSpacing.space1 / 2),
        // White on `scrim`, as the card specifies; `scrim` is dark in both themes.
        style = SojournTheme.type.labelSm.copy(color = Color.White, fontWeight = FontWeight.Medium),
    )
}
