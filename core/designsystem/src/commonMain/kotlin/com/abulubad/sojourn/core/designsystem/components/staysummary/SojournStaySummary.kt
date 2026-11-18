package com.abulubad.sojourn.core.designsystem.components.staysummary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.abulubad.sojourn.core.designsystem.components.hosttext.HostText
import com.abulubad.sojourn.core.designsystem.components.internal.PhotoSlot
import com.abulubad.sojourn.core.designsystem.components.rating.SojournRating
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.model.Photo
import com.abulubad.sojourn.core.model.Rating
import com.abulubad.sojourn.core.model.HostText as HostTextModel

/**
 * Which stay a step is about (the ScreenCheckout card's header, the booking confirmation): a small photo, the
 * host's [title] in its own language and direction (S-06), the [place] ("Paddington, London") and the [rating] when
 * it is shown (N-04 hides it under three reviews).
 *
 * - Not a button: it names the stay, it does not open it. A screen reader reads it once, title then place then rating.
 * - The title keeps to two lines; the photo slot shows placeholder, loaded and failed states like every photo.
 */
@Composable
fun SojournStaySummary(
    photo: Photo?,
    title: HostTextModel,
    place: String,
    modifier: Modifier = Modifier,
    rating: Rating? = null,
) {
    val c = SojournTheme.colors
    Row(
        modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (photo != null) {
            PhotoSlot(
                photo,
                contentDescription = null,
                modifier = Modifier.size(SojournStaySummaryDefaults.PhotoSize).clip(SojournShapes.md),
                showFailureText = false,
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SojournSpacing.space1)) {
            HostText(
                title,
                style = SojournTheme.type.body.copy(color = c.ink, fontWeight = FontWeight.SemiBold),
                maxLines = SojournStaySummaryDefaults.TITLE_LINES,
            )
            BasicText(place, style = SojournTheme.type.bodySm.copy(color = c.inkMuted))
            SojournRating(rating)
        }
    }
}
