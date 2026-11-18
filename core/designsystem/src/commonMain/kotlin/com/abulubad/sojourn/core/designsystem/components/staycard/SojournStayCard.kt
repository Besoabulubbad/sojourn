package com.abulubad.sojourn.core.designsystem.components.staycard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.abulubad.sojourn.core.designsystem.components.hosttext.HostText
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButton
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButtonTint
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButtonVariant
import com.abulubad.sojourn.core.designsystem.components.internal.PageDots
import com.abulubad.sojourn.core.designsystem.components.internal.PhotoPager
import com.abulubad.sojourn.core.designsystem.components.internal.PhotoSlot
import com.abulubad.sojourn.core.designsystem.components.internal.rememberSavePopScale
import com.abulubad.sojourn.core.designsystem.components.keyfacts.factsSummary
import com.abulubad.sojourn.core.designsystem.components.price.SojournPrice
import com.abulubad.sojourn.core.designsystem.components.price.SojournPriceSize
import com.abulubad.sojourn.core.designsystem.components.rating.SojournRating
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.theme.rememberSojournHaptics
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.localizedAreaName
import com.abulubad.sojourn.core.localization.localizedCityName
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.area_in_city
import com.abulubad.sojourn.core.localization.resources.cd_save
import com.abulubad.sojourn.core.localization.resources.cd_unsave
import com.abulubad.sojourn.core.localization.resources.price_long_stay
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.Stay
import org.jetbrains.compose.resources.stringResource

/**
 * One stay in a list (StayCard card). Reading order: area, rating, title, facts, price.
 *
 * - The area is always present ("Paddington, London"; the city alone if the area has no translation yet).
 * - The host's title keeps its own language and direction through [HostText] (S-06); it is the only unlocalized
 *   text on the card.
 * - Facts come from one plural-aware template: "Studio · 1 bed · 1 bath · Sleeps 2" (N-01).
 * - [price] is a `PriceDisplay`: a card cannot be built from a bare number (P-01). [priceNote] is a muted line
 *   under it, for what the screen knows about the price: offline, how old it is ("Priced 3 hours ago").
 * - [SojournStayCardLayout.Full]: up to five photos swipe inside the card with dots, the `accent` long-stay badge
 *   when the stay has a discount, and the floating save heart. No shadow in lists.
 * - [SojournStayCardLayout.Compact]: for the map; one photo at the start on a raised card.
 * - Saving pops the heart (0.8 → 1.3 → 1 over `base`) with a light haptic tick (motion.md).
 */
@Composable
fun SojournStayCard(
    stay: Stay,
    price: PriceDisplay,
    saved: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
    layout: SojournStayCardLayout = SojournStayCardLayout.Full,
    priceNote: String? = null,
) {
    when (layout) {
        SojournStayCardLayout.Full -> FullCard(stay, price, priceNote, saved, onClick, onToggleSave, modifier)
        SojournStayCardLayout.Compact -> CompactCard(stay, price, priceNote, saved, onClick, onToggleSave, modifier)
    }
}

@Composable
private fun FullCard(
    stay: Stay,
    price: PriceDisplay,
    priceNote: String?,
    saved: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier,
) {
    val photos = stay.photos.take(SojournStayCardDefaults.MAX_PHOTOS)
    Column(
        modifier.fillMaxWidth().cardClick(onClick),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
    ) {
        PhotoPager(
            photos = photos,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(SojournStayCardDefaults.PHOTO_ASPECT_RATIO)
                .clip(SojournShapes.lg),
        ) { state ->
            if (stay.longStayDiscountPercent != null) {
                // The badge stops short of the heart; with large text it wraps instead of running under it.
                LongStayBadge(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(
                            start = SojournSpacing.space3,
                            top = SojournSpacing.space3,
                            end = SojournSizes.touchMin + SojournSpacing.space2,
                        ),
                )
            }
            SaveButton(saved, onToggleSave, Modifier.align(Alignment.TopEnd).padding(SojournSpacing.space1))
            PageDots(
                photos.size,
                state.currentPage,
                Modifier.align(Alignment.BottomCenter).padding(bottom = SojournSpacing.space2),
            )
        }
        Details(stay, price, priceNote, compact = false)
    }
}

@Composable
private fun CompactCard(
    stay: Stay,
    price: PriceDisplay,
    priceNote: String?,
    saved: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier,
) {
    val c = SojournTheme.colors
    val elevation = SojournTheme.elevation.level3
    Box(
        modifier
            .fillMaxWidth()
            .shadow(elevation.elevation, SojournShapes.lg, ambientColor = elevation.color, spotColor = elevation.color)
            .background(c.surface, SojournShapes.lg)
            .cardClick(onClick)
            .padding(SojournSpacing.space2),
    ) {
        Details(
            stay,
            price,
            priceNote,
            compact = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = SojournStayCardDefaults.CompactPhotoMinHeight)
                .padding(start = SojournStayCardDefaults.CompactPhotoWidth + SojournSpacing.space3),
            trailing = { SaveButton(saved, onToggleSave, Modifier, floating = false) },
        )
        // The text sets the card's height and the photo takes it, laid out after the text: a loaded photo's own
        // height never stretches the card (it did through IntrinsicSize once the image was cached).
        Box(Modifier.matchParentSize()) {
            PhotoSlot(
                stay.photos.first(),
                contentDescription = null,
                showFailureText = false,
                modifier = Modifier
                    .width(SojournStayCardDefaults.CompactPhotoWidth)
                    .fillMaxHeight()
                    .clip(SojournShapes.md),
            )
        }
    }
}

@Composable
private fun Details(
    stay: Stay,
    price: PriceDisplay,
    priceNote: String?,
    compact: Boolean,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val c = SojournTheme.colors
    val type = SojournTheme.type
    Column(modifier, verticalArrangement = Arrangement.spacedBy(SojournStayCardDefaults.LineGap)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                areaLine(stay),
                modifier = Modifier.weight(1f),
                style = type.label.copy(color = c.inkMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            SojournRating(stay.rating, showCount = !compact)
            trailing?.invoke()
        }
        HostText(
            stay.title,
            modifier = Modifier.fillMaxWidth(),
            style = (if (compact) type.bodySm else type.body).copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
        )
        BasicText(factsSummary(stay.facts), style = type.bodySm.copy(color = c.inkMuted))
        SojournPrice(price, size = if (compact) SojournPriceSize.Compact else SojournPriceSize.Card)
        priceNote?.let { BasicText(it, style = type.bodySm.copy(color = c.inkMuted)) }
    }
}

@Composable
private fun areaLine(stay: Stay): String {
    val city = localizedCityName(stay.cityId)
    val area = localizedAreaName(stay.areaKey) ?: return city
    return stringResource(Res.string.area_in_city, area, city)
}

@Composable
private fun LongStayBadge(modifier: Modifier) {
    val c = SojournTheme.colors
    BasicText(
        stringResource(Res.string.price_long_stay),
        modifier = modifier
            .background(c.accent, SojournShapes.xs)
            .padding(horizontal = SojournSpacing.space2, vertical = SojournStayCardDefaults.LineGap),
        style = SojournTheme.type.labelSm.copy(color = c.onAccent, fontWeight = FontWeight.SemiBold),
    )
}

@Composable
private fun SaveButton(saved: Boolean, onToggle: () -> Unit, modifier: Modifier, floating: Boolean = true) {
    val haptics = rememberSojournHaptics()
    val scale = rememberSavePopScale(saved)
    SojournIconButton(
        icon = if (saved) SojournIcon.SavedFilled else SojournIcon.Saved,
        contentDescription = stringResource(if (saved) Res.string.cd_unsave else Res.string.cd_save),
        onClick = {
            haptics.tick()
            onToggle()
        },
        modifier = modifier.graphicsLayer {
            scaleX = scale()
            scaleY = scale()
        },
        variant = if (floating) SojournIconButtonVariant.Floating else SojournIconButtonVariant.Plain,
        tint = if (saved) SojournIconButtonTint.Saved else SojournIconButtonTint.Default,
    )
}

private fun Modifier.cardClick(onClick: () -> Unit): Modifier =
    clickable(role = Role.Button, indication = ripple(), interactionSource = null, onClick = onClick)
