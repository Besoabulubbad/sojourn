package com.abulubad.sojourn.core.designsystem.components.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.cd_photo_retry
import com.abulubad.sojourn.core.localization.resources.photo_failed
import com.abulubad.sojourn.core.model.Photo
import org.jetbrains.compose.resources.stringResource

/**
 * One photo slot with the brand book's three states: a `surfaceSunken` placeholder while loading, the photo, and
 * "Photo did not load" with a retry tap when it fails. [showFailureText] is false where the slot is too small
 * for a sentence (the compact map card); the icon and the retry stay. [contentScale] is `Fit` in the full-screen
 * viewer, where the whole photo shows.
 */
@Composable
internal fun PhotoSlot(
    photo: Photo,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    showFailureText: Boolean = true,
    contentScale: ContentScale = ContentScale.Crop,
) {
    var attempt by remember(photo.id) { mutableIntStateOf(0) }
    var failed by remember(photo.id) { mutableStateOf(false) }
    // A cropped photo fills its slot, so the placeholder is the slot; a fitted one sits on the screen's own colour.
    val fill = if (contentScale == ContentScale.Crop) SojournTheme.colors.surfaceSunken else Color.Transparent
    Box(modifier.background(fill)) {
        // A new attempt is a new request; Coil does not cache failures.
        key(attempt) {
            AsyncImage(
                model = photo.url,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                onState = { failed = it is AsyncImagePainter.State.Error },
            )
        }
        if (failed) {
            PhotoFailed(showText = showFailureText, onRetry = { attempt++ })
        }
    }
}

@Composable
private fun PhotoFailed(showText: Boolean, onRetry: () -> Unit) {
    val c = SojournTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .background(c.surfaceSunken)
            .clickable(
                onClickLabel = stringResource(Res.string.cd_photo_retry),
                role = Role.Button,
                onClick = onRetry,
            )
            .padding(SojournSpacing.space2),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SojournIcon(SojournIcon.ImageFailed, contentDescription = null, tint = c.inkMuted)
        if (showText) {
            BasicText(
                stringResource(Res.string.photo_failed),
                style = SojournTheme.type.bodySm.copy(color = c.inkMuted, textAlign = TextAlign.Center),
            )
        }
    }
}
