package com.abulubad.sojourn.feature.explore

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.button.SojournButtonVariant
import com.abulubad.sojourn.core.designsystem.components.chip.SojournChip
import com.abulubad.sojourn.core.designsystem.components.citycard.SojournCityCard
import com.abulubad.sojourn.core.designsystem.components.citycard.SojournCityCardDefaults
import com.abulubad.sojourn.core.designsystem.components.continuerow.SojournContinueRow
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyState
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyStateAction
import com.abulubad.sojourn.core.designsystem.components.searchbar.SojournSearchBar
import com.abulubad.sojourn.core.designsystem.components.searchbar.searchSummaryText
import com.abulubad.sojourn.core.designsystem.components.skeleton.SojournSkeleton
import com.abulubad.sojourn.core.designsystem.components.snackbar.SojournOfflineBanner
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.LocalReduceMotion
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournMotion
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.localizedCityName
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_retry
import com.abulubad.sojourn.core.localization.resources.explore_cities_empty_body
import com.abulubad.sojourn.core.localization.resources.explore_cities_offline_body
import com.abulubad.sojourn.core.localization.resources.explore_cities_title
import com.abulubad.sojourn.core.localization.resources.explore_continue_title
import com.abulubad.sojourn.core.localization.resources.explore_greeting
import com.abulubad.sojourn.core.localization.resources.explore_length_few_nights
import com.abulubad.sojourn.core.localization.resources.explore_length_longer
import com.abulubad.sojourn.core.localization.resources.explore_length_month
import com.abulubad.sojourn.core.localization.resources.explore_length_title
import com.abulubad.sojourn.core.localization.resources.explore_length_week
import com.abulubad.sojourn.core.localization.resources.explore_next_stay_title
import com.abulubad.sojourn.core.localization.resources.explore_see_all
import com.abulubad.sojourn.core.localization.resources.nights
import com.abulubad.sojourn.core.localization.resources.offline_banner
import com.abulubad.sojourn.core.localization.resources.search_anywhere
import com.abulubad.sojourn.core.localization.resources.search_summary_undated
import com.abulubad.sojourn.core.localization.resources.state_empty_title
import com.abulubad.sojourn.core.localization.resources.state_error_body
import com.abulubad.sojourn.core.localization.resources.state_error_title
import com.abulubad.sojourn.core.localization.resources.state_loading
import com.abulubad.sojourn.core.localization.resources.state_offline_title
import com.abulubad.sojourn.core.model.City
import com.abulubad.sojourn.core.model.StayLength
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournScaffold
import com.abulubad.sojourn.core.ui.SojournTabBar
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Explore, the first screen (ScreenExplore card), top to bottom: a one-line greeting, the search bar (the only search
 * entry), the stay-length shortcuts, the "Continue" row when there is something to continue, and the cities.
 * Stateless: [state] in, [onIntent] out.
 *
 * Offline with cached cities shows them under the offline banner; with nothing cached, the cities section shows the
 * offline or error state with a retry, while search and shortcuts stay usable.
 */
@Composable
fun ExploreScreen(state: ExploreUiState, onIntent: (ExploreIntent) -> Unit, modifier: Modifier = Modifier) {
    SojournScaffold(modifier, bottomBar = { SojournTabBar() }) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + SojournSpacing.space2,
                bottom = padding.calculateBottomPadding() + SojournSpacing.space6,
            ),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space6),
        ) {
            val cities = state.cities
            if (cities is ScreenState.Offline && cities.cached != null) {
                item("offline") {
                    SojournOfflineBanner(
                        message = stringResource(Res.string.offline_banner),
                        actionLabel = stringResource(Res.string.action_retry),
                        onAction = { onIntent(ExploreIntent.RetryTapped) },
                    )
                }
            }
            item("search") { SearchArea(onSearch = { onIntent(ExploreIntent.SearchTapped) }) }
            item("lengths") { StayLengths(onSelect = { onIntent(ExploreIntent.LengthTapped(it)) }) }
            state.resume?.let { resume ->
                item("resume") { Resume(resume, onClick = { onIntent(ExploreIntent.ResumeTapped) }) }
            }
            item("cities") { Cities(cities, onIntent) }
        }
    }
}

@Composable
private fun SearchArea(onSearch: () -> Unit) {
    Column(
        Modifier.padding(horizontal = SojournSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
    ) {
        BasicText(
            stringResource(Res.string.explore_greeting),
            modifier = Modifier.semantics { heading() },
            // The card asks for one line; at display size the greeting needs two on a phone, and wrapping beats
            // clipping (I-04, 200 % text).
            style = SojournTheme.type.display.copy(color = SojournTheme.colors.ink),
        )
        SojournSearchBar(query = null, onClick = onSearch)
    }
}

@Composable
private fun StayLengths(onSelect: (StayLength) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3)) {
        BasicText(
            stringResource(Res.string.explore_length_title),
            modifier = Modifier.padding(horizontal = SojournSpacing.space4),
            style = SojournTheme.type.label.copy(color = SojournTheme.colors.inkMuted),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = SojournSpacing.space4),
            horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
        ) {
            items(LENGTH_SHORTCUTS.size) { index ->
                val (length, label) = LENGTH_SHORTCUTS[index]
                SojournChip(label = stringResource(label), selected = false, onClick = { onSelect(length) })
            }
        }
    }
}

/** The shortcuts, in the card's words; each opens the search flow with that length pre-set. */
private val LENGTH_SHORTCUTS: List<Pair<StayLength, StringResource>> = listOf(
    StayLength.Weekend to Res.string.explore_length_few_nights,
    StayLength.Week to Res.string.explore_length_week,
    StayLength.Month to Res.string.explore_length_month,
    StayLength.Longer to Res.string.explore_length_longer,
)

@Composable
private fun Resume(resume: ExploreResume, onClick: () -> Unit) {
    val (heading, title, subtitle) = when (resume) {
        is ExploreResume.LastSearch -> Triple(
            Res.string.explore_continue_title,
            resume.query.cityId?.let { localizedCityName(it) } ?: stringResource(Res.string.search_anywhere),
            searchSummaryText(resume.query),
        )
        is ExploreResume.NextStay -> {
            val formatter = rememberSojournFormatter()
            val dates = resume.booking.dates
            val nights = pluralStringResource(Res.plurals.nights, dates.nights, formatter.count(dates.nights))
            Triple(
                Res.string.explore_next_stay_title,
                resume.cityId?.let { localizedCityName(it) } ?: resume.booking.reference,
                stringResource(Res.string.search_summary_undated, formatter.dateRange(dates), nights),
            )
        }
    }
    Column(
        Modifier.padding(horizontal = SojournSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
    ) {
        SectionTitle(stringResource(heading), small = true)
        SojournContinueRow(photo = resume.photo, title = title, subtitle = subtitle, onClick = onClick)
    }
}

@Composable
private fun Cities(cities: ScreenState<List<City>>, onIntent: (ExploreIntent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3)) {
        Row(
            Modifier.fillMaxWidth().padding(start = SojournSpacing.space4, end = SojournSpacing.space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle(stringResource(Res.string.explore_cities_title), modifier = Modifier.weight(1f))
            SojournButton(
                text = stringResource(Res.string.explore_see_all),
                onClick = { onIntent(ExploreIntent.SeeAllCitiesTapped) },
                variant = SojournButtonVariant.Ghost,
            )
        }
        val retry = SojournEmptyStateAction(stringResource(Res.string.action_retry)) {
            onIntent(ExploreIntent.RetryTapped)
        }
        when (cities) {
            ScreenState.Loading -> CitySkeletons()
            is ScreenState.Content -> CityRow(cities.data, onIntent)
            is ScreenState.Offline -> cities.cached?.let { CityRow(it, onIntent) } ?: SojournEmptyState(
                icon = SojournIcon.Offline,
                title = stringResource(Res.string.state_offline_title),
                body = stringResource(Res.string.explore_cities_offline_body),
                action = retry,
            )
            ScreenState.Empty -> SojournEmptyState(
                icon = SojournIcon.City,
                title = stringResource(Res.string.state_empty_title),
                body = stringResource(Res.string.explore_cities_empty_body),
            )
            is ScreenState.Error -> SojournEmptyState(
                icon = SojournIcon.Alert,
                title = stringResource(Res.string.state_error_title),
                body = stringResource(Res.string.state_error_body),
                action = retry,
            )
        }
    }
}

@Composable
private fun CityRow(cities: List<City>, onIntent: (ExploreIntent) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = SojournSpacing.space4),
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
    ) {
        itemsIndexed(cities, key = { _, city -> city.id.value }) { index, city ->
            SojournCityCard(
                city = city,
                onClick = { onIntent(ExploreIntent.CityTapped(city.id)) },
                modifier = Modifier.width(SojournCityCardDefaults.Width).fadeIn(index),
            )
        }
    }
}

/** Cards fade in on first load with a short stagger, at most five (ScreenExplore card); none under reduce motion. */
@Composable
private fun Modifier.fadeIn(index: Int): Modifier {
    if (LocalReduceMotion.current || index >= MAX_STAGGERED) return this
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val alpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(SojournMotion.fast, delayMillis = index * CARD_STAGGER, easing = SojournMotion.standard),
    )
    return graphicsLayer { this.alpha = alpha }
}

/** 40 ms between cards, the card's stagger: two fifths of the `instant` duration. */
private const val CARD_STAGGER = SojournMotion.instant * 2 / 5
private const val MAX_STAGGERED = 5
private const val SKELETON_CARDS = 3

@Composable
private fun CitySkeletons() {
    val loading = stringResource(Res.string.state_loading)
    Row(
        Modifier.padding(horizontal = SojournSpacing.space4).clearAndSetSemantics { contentDescription = loading },
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
    ) {
        repeat(SKELETON_CARDS) {
            Column(
                Modifier.width(SojournCityCardDefaults.Width),
                verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
            ) {
                SojournSkeleton(
                    Modifier.fillMaxWidth().height(SojournCityCardDefaults.PhotoHeight),
                    SojournShapes.lg,
                )
                SojournSkeleton(Modifier.fillMaxWidth(SKELETON_TITLE).height(SojournSpacing.space4))
                SojournSkeleton(Modifier.fillMaxWidth().height(SojournSpacing.space3))
            }
        }
    }
}

private const val SKELETON_TITLE = 0.6f

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier, small: Boolean = false) {
    BasicText(
        text,
        modifier = modifier.semantics { heading() },
        style = (if (small) SojournTheme.type.titleSm else SojournTheme.type.titleMd)
            .copy(color = SojournTheme.colors.ink),
    )
}
