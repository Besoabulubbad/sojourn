package com.abulubad.sojourn.feature.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.abulubad.sojourn.core.designsystem.amenity.icon
import com.abulubad.sojourn.core.designsystem.amenity.label
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.button.SojournButtonVariant
import com.abulubad.sojourn.core.designsystem.components.chip.SojournFactChip
import com.abulubad.sojourn.core.designsystem.components.divider.SojournDivider
import com.abulubad.sojourn.core.designsystem.components.hosttext.HostText
import com.abulubad.sojourn.core.designsystem.components.keyfacts.SojournKeyFacts
import com.abulubad.sojourn.core.designsystem.components.policy.SojournPolicyRow
import com.abulubad.sojourn.core.designsystem.components.rating.SojournRating
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.domain.StayDetails
import com.abulubad.sojourn.core.localization.LocalAppLocale
import com.abulubad.sojourn.core.localization.localizedPlace
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_read_less
import com.abulubad.sojourn.core.localization.resources.action_read_more
import com.abulubad.sojourn.core.localization.resources.action_show_on_map
import com.abulubad.sojourn.core.localization.resources.action_show_original
import com.abulubad.sojourn.core.localization.resources.action_translate_to
import com.abulubad.sojourn.core.localization.resources.amenity_wifi
import com.abulubad.sojourn.core.localization.resources.check_in_self_check_in_detail
import com.abulubad.sojourn.core.localization.resources.details_about
import com.abulubad.sojourn.core.localization.resources.details_amenities
import com.abulubad.sojourn.core.localization.resources.details_good_to_know
import com.abulubad.sojourn.core.localization.resources.details_includes
import com.abulubad.sojourn.core.localization.resources.details_reviews
import com.abulubad.sojourn.core.localization.resources.details_translated_from
import com.abulubad.sojourn.core.localization.resources.details_where
import com.abulubad.sojourn.core.localization.resources.details_written_in
import com.abulubad.sojourn.core.localization.resources.host_language_ar
import com.abulubad.sojourn.core.localization.resources.host_language_en
import com.abulubad.sojourn.core.localization.resources.host_language_fr
import com.abulubad.sojourn.core.localization.resources.host_language_pt
import com.abulubad.sojourn.core.localization.resources.inclusion_linen
import com.abulubad.sojourn.core.localization.resources.inclusion_utilities
import com.abulubad.sojourn.core.localization.resources.inclusion_weekly_cleaning
import com.abulubad.sojourn.core.localization.resources.minutes_walk
import com.abulubad.sojourn.core.localization.resources.review_byline
import com.abulubad.sojourn.core.localization.resources.transit_public
import com.abulubad.sojourn.core.localization.resources.transit_walk
import com.abulubad.sojourn.core.model.CheckInMethod
import com.abulubad.sojourn.core.model.HostText
import com.abulubad.sojourn.core.model.Policy
import com.abulubad.sojourn.core.model.PriceInclusion
import com.abulubad.sojourn.core.model.Review
import com.abulubad.sojourn.core.model.Stay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** The card's sections, in its order, below the gallery. */
@Composable
internal fun Sections(details: StayDetails, state: DetailsUiState, onIntent: (DetailsIntent) -> Unit) {
    val stay = details.stay
    Column(
        Modifier.padding(horizontal = SojournSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space5),
    ) {
        Heading(details)
        if (stay.inclusions.isNotEmpty()) Includes(stay)
        SojournDivider()
        Where(stay, onIntent)
        SojournDivider()
        Amenities(stay)
        SojournDivider()
        About(stay, state, onIntent)
        SojournDivider()
        GoodToKnow(stay)
        if (details.reviews.isNotEmpty()) {
            SojournDivider()
            Reviews(details)
        }
    }
}

@Composable
private fun Heading(details: StayDetails) {
    val stay = details.stay
    val c = SojournTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2)) {
        IconLine(SojournIcon.MapPin, localizedPlace(stay.cityId, stay.areaKey), muted = true)
        HostText(
            stay.title,
            modifier = Modifier.semantics { heading() },
            style = SojournTheme.type.titleLg.copy(color = c.ink),
        )
        details.rating?.let { SojournRating(it) }
    }
    SojournKeyFacts(stay.facts)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Includes(stay: Stay) {
    Section(Res.string.details_includes) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
        ) {
            PriceInclusion.entries.filter { it in stay.inclusions }.forEach { inclusion ->
                SojournFactChip(stringResource(inclusion.label), icon = SojournIcon.Check)
            }
        }
    }
}

@Composable
private fun Where(stay: Stay, onIntent: (DetailsIntent) -> Unit) {
    val formatter = rememberSojournFormatter()
    Section(Res.string.details_where) {
        IconLine(SojournIcon.MapPin, localizedPlace(stay.cityId, stay.areaKey))
        stay.walkMinutesToTransit?.let { minutes ->
            val walk = pluralStringResource(Res.plurals.minutes_walk, minutes, formatter.count(minutes))
            val destination = stringResource(Res.string.transit_public)
            IconLine(SojournIcon.Walk, stringResource(Res.string.transit_walk, walk, destination))
        }
        SojournButton(
            stringResource(Res.string.action_show_on_map),
            onClick = { onIntent(DetailsIntent.ShowOnMapTapped) },
            variant = SojournButtonVariant.Secondary,
            leadingIcon = SojournIcon.Map,
        )
    }
}

@Composable
private fun Amenities(stay: Stay) {
    Section(Res.string.details_amenities) {
        // Two columns, ranked for longer stays; a row's two cells share its width.
        AmenityRanking.filter { it in stay.amenities }.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3)) {
                row.forEach { amenity ->
                    IconLine(amenity.icon, stringResource(amenity.label), modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Row(Modifier.weight(1f)) {}
            }
        }
    }
}

@Composable
private fun About(stay: Stay, state: DetailsUiState, onIntent: (DetailsIntent) -> Unit) {
    val appLanguage = LocalAppLocale.current.tag.substringBefore('-')
    val text = state.description(appLanguage) ?: return
    val hostLanguage = stay.description.languageTag.substringBefore('-')
    val readerLanguage = hostLanguageName(appLanguage)
    val canTranslate = readerLanguage != null && hostLanguage != appLanguage &&
        stay.descriptionTranslations.any { it.languageTag == appLanguage }
    val showingTranslation = text != stay.description
    val excerpt = sentenceExcerpt(text.text, DESCRIPTION_EXCERPT)
    val shown = if (state.expanded || excerpt == null) text else HostText(excerpt, text.languageTag)
    Section(Res.string.details_about) {
        hostLanguageName(hostLanguage)?.let { language ->
            val note = if (showingTranslation) Res.string.details_translated_from else Res.string.details_written_in
            BasicText(
                stringResource(note, stringResource(language)),
                style = SojournTheme.type.labelSm.copy(color = SojournTheme.colors.brandInk),
            )
        }
        // Full width, so a paragraph aligns by its own language, not the app's (an English text in Arabic starts left).
        HostText(
            shown,
            modifier = Modifier.fillMaxWidth(),
            style = SojournTheme.type.bodyLg.copy(color = SojournTheme.colors.ink),
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (excerpt != null) {
                SojournButton(
                    stringResource(if (state.expanded) Res.string.action_read_less else Res.string.action_read_more),
                    onClick = { onIntent(DetailsIntent.ReadMoreTapped) },
                    variant = SojournButtonVariant.Ghost,
                )
            }
            if (canTranslate && readerLanguage != null) {
                val label = if (showingTranslation) {
                    stringResource(Res.string.action_show_original)
                } else {
                    stringResource(Res.string.action_translate_to, stringResource(readerLanguage))
                }
                SojournButton(
                    label,
                    onClick = { onIntent(DetailsIntent.TranslateTapped) },
                    variant = SojournButtonVariant.Secondary,
                    leadingIcon = SojournIcon.Language,
                )
            }
        }
    }
}

@Composable
private fun GoodToKnow(stay: Stay) {
    Section(Res.string.details_good_to_know) {
        Column {
            stay.policies.forEachIndexed { index, policy ->
                val detail = (policy as? Policy.CheckIn)?.takeIf { it.method == CheckInMethod.SelfCheckIn }
                    ?.let { stringResource(Res.string.check_in_self_check_in_detail) }
                SojournPolicyRow(policy, detail = detail, divider = index > 0)
            }
        }
    }
}

@Composable
private fun Reviews(details: StayDetails) {
    val formatter = rememberSojournFormatter()
    val c = SojournTheme.colors
    Section(Res.string.details_reviews) {
        details.rating?.let { SojournRating(it) }
        details.reviews.forEach { review: Review ->
            Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space1)) {
                BasicText(
                    stringResource(Res.string.review_byline, review.author, formatter.monthYear(review.month)),
                    style = SojournTheme.type.label.copy(color = c.inkMuted),
                )
                HostText(
                    review.text,
                    modifier = Modifier.fillMaxWidth(),
                    style = SojournTheme.type.body.copy(color = c.ink),
                )
            }
        }
    }
}

@Composable
private fun Section(title: StringResource, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3)) {
        BasicText(
            stringResource(title),
            modifier = Modifier.semantics { heading() },
            style = SojournTheme.type.titleMd.copy(color = SojournTheme.colors.ink),
        )
        content()
    }
}

@Composable
private fun IconLine(icon: SojournIcon, text: String, modifier: Modifier = Modifier, muted: Boolean = false) {
    val c = SojournTheme.colors
    val color = if (muted) c.inkMuted else c.ink
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SojournIcon(icon, contentDescription = null, tint = color, size = SojournSizes.iconSm)
        BasicText(text, style = SojournTheme.type.body.copy(color = color))
    }
}

private val PriceInclusion.label: StringResource
    get() = when (this) {
        PriceInclusion.Wifi -> Res.string.amenity_wifi
        PriceInclusion.Utilities -> Res.string.inclusion_utilities
        PriceInclusion.Linen -> Res.string.inclusion_linen
        PriceInclusion.WeeklyCleaning -> Res.string.inclusion_weekly_cleaning
    }

/** A host language's name in the reader's language ("Written in French"); null for one the app cannot name. */
private fun hostLanguageName(language: String): StringResource? = when (language) {
    "en" -> Res.string.host_language_en
    "fr" -> Res.string.host_language_fr
    "pt" -> Res.string.host_language_pt
    "ar" -> Res.string.host_language_ar
    else -> null
}

/** Descriptions longer than this show their first sentences and "Read more". */
private const val DESCRIPTION_EXCERPT = 240
