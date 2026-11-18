package com.abulubad.sojourn.core.designsystem.components.searchbar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButton
import com.abulubad.sojourn.core.designsystem.components.internal.focusRing
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.localizedCityName
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.cd_back
import com.abulubad.sojourn.core.localization.resources.guests
import com.abulubad.sojourn.core.localization.resources.nights
import com.abulubad.sojourn.core.localization.resources.search_any_dates
import com.abulubad.sojourn.core.localization.resources.search_length_from_month
import com.abulubad.sojourn.core.localization.resources.search_prompt_hint
import com.abulubad.sojourn.core.localization.resources.search_prompt_title
import com.abulubad.sojourn.core.localization.resources.search_summary_dated
import com.abulubad.sojourn.core.localization.resources.search_summary_undated
import com.abulubad.sojourn.core.localization.resources.stay_length_longer
import com.abulubad.sojourn.core.localization.resources.stay_length_month
import com.abulubad.sojourn.core.localization.resources.stay_length_week
import com.abulubad.sojourn.core.localization.resources.stay_length_weekend
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.StayLength
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The single entry point to search (SearchBar card). It is a button, not a text field: tapping opens the search flow.
 *
 * - With no [query] (Explore) it is the prompt: "Where to?" over "City · Dates or stay length · Guests".
 * - With a [query] (Results, Map) it is the summary: the city, then dates and nights, then guests, from one
 *   localized template. Passing [onBack] gives the compact form with a back button at the start.
 * - Never place a second search form beside it; the filter button sits next to it as a [SojournIconButton].
 */
@Composable
fun SojournSearchBar(
    query: SearchQuery?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val c = SojournTheme.colors
    val compact = onBack != null
    // `xl` (28 dp) is a pill at the bar's normal height; with large text the bar grows and becomes a rounded
    // rectangle instead of a capsule whose round ends would cut into the text.
    val elevation = if (compact) SojournTheme.elevation.level1 else SojournTheme.elevation.level2
    val interactionSource = remember { MutableInteractionSource() }
    val title = query?.cityId?.let { localizedCityName(it) } ?: stringResource(Res.string.search_prompt_title)
    val detail = if (query == null) stringResource(Res.string.search_prompt_hint) else searchSummaryText(query)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .focusRing(interactionSource, cornerRadius = SojournShapes.all.getValue("radius-xl"))
            .defaultMinSize(
                minHeight = if (compact) SojournSizes.touchMin else SojournSearchBarDefaults.PromptMinHeight,
            )
            .shadow(
                elevation.elevation,
                SojournShapes.xl,
                ambientColor = elevation.color,
                spotColor = elevation.color,
            )
            .clip(SojournShapes.xl)
            .background(c.surface)
            .border(1.dp, c.hairline, SojournShapes.xl)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(
                start = if (compact) SojournSpacing.space1 else SojournSpacing.space4,
                end = SojournSpacing.space4,
                top = if (compact) 0.dp else SojournSpacing.space2,
                bottom = if (compact) 0.dp else SojournSpacing.space2,
            ),
        horizontalArrangement = Arrangement.spacedBy(if (compact) SojournSpacing.space1 else SojournSpacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            SojournIconButton(SojournIcon.Back, stringResource(Res.string.cd_back), onClick = onBack)
        } else {
            SojournIcon(SojournIcon.Search, contentDescription = null, tint = c.ink)
        }
        Column(Modifier.weight(1f)) {
            BasicText(title, style = SojournTheme.type.body.copy(color = c.ink, fontWeight = FontWeight.SemiBold))
            BasicText(
                detail,
                style = SojournTheme.type.caption.copy(color = c.inkMuted),
                // Up to three lines keep dates, nights and guests visible with large text.
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * The one summary template for a search (the bar's summary form, Explore's "Continue your search"):
 * "12–27 Mar · 15 nights · 2 guests", or "A month · 2 guests" before dates are chosen, and "A month from March ·
 * 2 guests" for a flexible search with a start month.
 */
@Composable
fun searchSummaryText(query: SearchQuery): String {
    val formatter = rememberSojournFormatter()
    val guestCount = query.guests.total
    val guests = pluralStringResource(Res.plurals.guests, guestCount, formatter.count(guestCount))
    val dates = query.dates
    return if (dates != null) {
        val nights = pluralStringResource(Res.plurals.nights, dates.nights, formatter.count(dates.nights))
        stringResource(Res.string.search_summary_dated, formatter.dateRange(dates), nights, guests)
    } else {
        val length = query.length?.let { length ->
            val label = stayLengthLabel(length)
            val month = query.startMonth?.let { formatter.monthLong(it.month) }
            month?.let { stringResource(Res.string.search_length_from_month, label, it) } ?: label
        } ?: stringResource(Res.string.search_any_dates)
        stringResource(Res.string.search_summary_undated, length, guests)
    }
}

@Composable
private fun stayLengthLabel(length: StayLength): String = stringResource(
    when (length) {
        StayLength.Weekend -> Res.string.stay_length_weekend
        StayLength.Week -> Res.string.stay_length_week
        StayLength.Month -> Res.string.stay_length_month
        StayLength.Longer -> Res.string.stay_length_longer
    },
)
