package com.abulubad.sojourn.core.designsystem.components.rating

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.rating_count
import com.abulubad.sojourn.core.localization.resources.rating_spoken
import com.abulubad.sojourn.core.model.Rating
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Star, score and review count (Rating card): ★ 4.8 (32).
 *
 * Renders **nothing** when [rating] is null or has fewer than three reviews (N-04): there is no empty, zero or "new"
 * rating. Screen readers hear one phrase, "Rated 4.8 out of 5, 32 reviews". The score and count come from
 * `SojournFormatter`, with Western digits in bidi isolates. [showCount] = false drops the count where the card is
 * compact.
 *
 * ```
 * SojournRating(stay.rating)
 * ```
 */
@Composable
fun SojournRating(
    rating: Rating?,
    modifier: Modifier = Modifier,
    showCount: Boolean = true,
) {
    if (rating == null || !SojournRatingDefaults.isShown(rating)) return
    val formatter = rememberSojournFormatter()
    val score = formatter.rating(rating.score)
    val count = formatter.count(rating.count)
    val spoken = pluralStringResource(Res.plurals.rating_spoken, rating.count, score, count)
    val textStyle = SojournTheme.type.bodySm
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = spoken },
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SojournIcon(
            icon = SojournIcon.StarFilled,
            contentDescription = null,
            tint = SojournTheme.colors.accentInk,
            size = SojournRatingDefaults.StarSize,
        )
        BasicText(score, style = textStyle.copy(color = SojournTheme.colors.ink, fontWeight = FontWeight.SemiBold))
        if (showCount) {
            BasicText(
                stringResource(Res.string.rating_count, count),
                style = textStyle.copy(color = SojournTheme.colors.inkMuted),
            )
        }
    }
}
