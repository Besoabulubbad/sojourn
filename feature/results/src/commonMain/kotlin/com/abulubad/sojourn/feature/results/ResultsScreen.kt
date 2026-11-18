package com.abulubad.sojourn.feature.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.button.SojournButtonVariant
import com.abulubad.sojourn.core.designsystem.components.chip.SojournChip
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyState
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyStateAction
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButton
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButtonVariant
import com.abulubad.sojourn.core.designsystem.components.refresh.SojournPullToRefresh
import com.abulubad.sojourn.core.designsystem.components.searchbar.SojournSearchBar
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheet
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheetDetent
import com.abulubad.sojourn.core.designsystem.components.sheet.rememberSojournSheetState
import com.abulubad.sojourn.core.designsystem.components.skeleton.SojournStayCardSkeleton
import com.abulubad.sojourn.core.designsystem.components.snackbar.SojournOfflineBanner
import com.abulubad.sojourn.core.designsystem.components.staycard.SojournStayCard
import com.abulubad.sojourn.core.designsystem.components.viewtoggle.SojournResultsView
import com.abulubad.sojourn.core.designsystem.components.viewtoggle.SojournViewToggle
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.domain.SearchResults
import com.abulubad.sojourn.core.localization.localizedCityName
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_change_search
import com.abulubad.sojourn.core.localization.resources.action_clear_filters
import com.abulubad.sojourn.core.localization.resources.action_retry
import com.abulubad.sojourn.core.localization.resources.amenity_washer
import com.abulubad.sojourn.core.localization.resources.amenity_workspace
import com.abulubad.sojourn.core.localization.resources.cd_filters
import com.abulubad.sojourn.core.localization.resources.facts_studio
import com.abulubad.sojourn.core.localization.resources.offline_banner
import com.abulubad.sojourn.core.localization.resources.priced_days_ago
import com.abulubad.sojourn.core.localization.resources.priced_hours_ago
import com.abulubad.sojourn.core.localization.resources.results_empty_filters_body
import com.abulubad.sojourn.core.localization.resources.results_empty_search_body
import com.abulubad.sojourn.core.localization.resources.results_empty_title
import com.abulubad.sojourn.core.localization.resources.results_free_cancellation
import com.abulubad.sojourn.core.localization.resources.results_in_city
import com.abulubad.sojourn.core.localization.resources.results_sort
import com.abulubad.sojourn.core.localization.resources.results_sort_title
import com.abulubad.sojourn.core.localization.resources.sort_distance
import com.abulubad.sojourn.core.localization.resources.sort_price_low_to_high
import com.abulubad.sojourn.core.localization.resources.sort_rating
import com.abulubad.sojourn.core.localization.resources.sort_recommended
import com.abulubad.sojourn.core.localization.resources.state_error_body
import com.abulubad.sojourn.core.localization.resources.state_error_title
import com.abulubad.sojourn.core.localization.resources.state_loading
import com.abulubad.sojourn.core.localization.resources.state_offline_body
import com.abulubad.sojourn.core.localization.resources.state_offline_title
import com.abulubad.sojourn.core.localization.resources.stays
import com.abulubad.sojourn.core.model.SortOrder
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournScaffold
import com.abulubad.sojourn.core.ui.SojournTabBar
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Results (ScreenResults card), top to bottom: the search summary (tap to change the search) beside the filter
 * button and its count, the quick-filter chips, the count and Sort, then the stays. The "Map" toggle floats above
 * the bottom nav. Stateless: [state] in, [onIntent] out.
 *
 * - Pull to refresh runs the search again; the list stays on screen while it does.
 * - Offline with cached stays: the banner above them, and a dated price older than an hour says its age.
 * - Empty: what happened and one way forward that changes the query: clear the filters, or change the search.
 * - An error replaces only the list; the summary, chips and Map stay usable.
 */
@Composable
fun ResultsScreen(state: ResultsUiState, onIntent: (ResultsIntent) -> Unit, modifier: Modifier = Modifier) {
    SojournScaffold(modifier, bottomBar = { SojournTabBar() }) { padding ->
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Header(state, onIntent, padding)
                SojournPullToRefresh(
                    refreshing = state.pulling,
                    onRefresh = { onIntent(ResultsIntent.Pulled) },
                    modifier = Modifier.weight(1f),
                ) {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = listPadding(padding),
                        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space5),
                    ) {
                        list(state, onIntent)
                    }
                }
            }
            SojournViewToggle(
                current = SojournResultsView.List,
                onToggle = { onIntent(ResultsIntent.MapTapped) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = padding.calculateBottomPadding() + SojournSpacing.space3),
            )
            if (state.sortOpen) SortSheet(state.filters.sort, onIntent)
        }
    }
}

@Composable
private fun Header(state: ResultsUiState, onIntent: (ResultsIntent) -> Unit, padding: PaddingValues) {
    val direction = LocalLayoutDirection.current
    Column(
        Modifier.padding(
            start = padding.calculateStartPadding(direction),
            end = padding.calculateEndPadding(direction),
            top = padding.calculateTopPadding(),
        ),
    ) {
        Row(
            Modifier.padding(
                start = SojournSpacing.space2,
                end = SojournSpacing.space2,
                top = SojournSpacing.space1,
                bottom = SojournSpacing.space2,
            ),
            horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SojournSearchBar(
                query = state.query,
                onClick = { onIntent(ResultsIntent.SearchTapped) },
                onBack = { onIntent(ResultsIntent.BackTapped) },
                modifier = Modifier.weight(1f),
            )
            SojournIconButton(
                icon = SojournIcon.Filters,
                contentDescription = stringResource(Res.string.cd_filters),
                onClick = { onIntent(ResultsIntent.FiltersTapped) },
                variant = SojournIconButtonVariant.Outline,
                badge = state.filters.activeCount.takeIf { it > 0 },
            )
        }
        LazyRow(
            contentPadding = PaddingValues(
                start = SojournSpacing.space4,
                end = SojournSpacing.space4,
                bottom = SojournSpacing.space3,
            ),
            horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
        ) {
            items(QuickFilter.entries) { filter ->
                SojournChip(
                    label = stringResource(filter.label),
                    selected = filter.isOn(state.filters),
                    onClick = { onIntent(ResultsIntent.QuickFilterTapped(filter)) },
                )
            }
        }
    }
}

private val QuickFilter.label: StringResource
    get() = when (this) {
        QuickFilter.Studio -> Res.string.facts_studio
        QuickFilter.Workspace -> Res.string.amenity_workspace
        QuickFilter.Washer -> Res.string.amenity_washer
        QuickFilter.FreeCancellation -> Res.string.results_free_cancellation
    }

private fun LazyListScope.list(state: ResultsUiState, onIntent: (ResultsIntent) -> Unit) {
    val retry = { onIntent(ResultsIntent.RetryTapped) }
    when (val results = state.results) {
        ScreenState.Loading -> item("loading") { Skeletons() }
        is ScreenState.Content -> stays(results.data, state, onIntent)
        is ScreenState.Offline -> {
            val cached = results.cached
            if (cached != null && cached.stays.isNotEmpty()) {
                item("offline") { OfflineBanner(retry) }
                stays(cached, state, onIntent)
            } else {
                item("offline_empty") {
                    Message(SojournIcon.Offline, Res.string.state_offline_title, Res.string.state_offline_body, retry)
                }
            }
        }
        ScreenState.Empty -> item("empty") { Empty(state, onIntent) }
        is ScreenState.Error -> item("error") {
            Message(SojournIcon.Alert, Res.string.state_error_title, Res.string.state_error_body, retry)
        }
    }
}

private fun LazyListScope.stays(results: SearchResults, state: ResultsUiState, onIntent: (ResultsIntent) -> Unit) {
    item("count") { CountAndSort(results.stays.size, state, onIntent) }
    items(results.stays, key = { it.stay.id.value }) { result ->
        SojournStayCard(
            stay = result.stay,
            price = result.price,
            saved = result.saved,
            onClick = { onIntent(ResultsIntent.StayTapped(result)) },
            onToggleSave = { onIntent(ResultsIntent.SaveTapped(result.stay.id)) },
            modifier = Modifier.padding(horizontal = SojournSpacing.space4),
            priceNote = state.staleHours[result.stay.id]?.let { stalePriceNote(it) },
        )
    }
}

/** "Priced 3 hours ago"; from a day on, "Priced 2 days ago". */
@Composable
private fun stalePriceNote(hours: Long): String {
    val formatter = rememberSojournFormatter()
    val days = (hours / HOURS_PER_DAY).toInt()
    return if (days >= 1) {
        pluralStringResource(Res.plurals.priced_days_ago, days, formatter.count(days))
    } else {
        pluralStringResource(Res.plurals.priced_hours_ago, hours.toInt(), formatter.count(hours.toInt()))
    }
}

private const val HOURS_PER_DAY = 24

@Composable
private fun CountAndSort(count: Int, state: ResultsUiState, onIntent: (ResultsIntent) -> Unit) {
    val formatter = rememberSojournFormatter()
    val label = state.query.cityId
        ?.let { city ->
            pluralStringResource(Res.plurals.results_in_city, count, formatter.count(count), localizedCityName(city))
        }
        ?: pluralStringResource(Res.plurals.stays, count, formatter.count(count))
    Row(
        Modifier.fillMaxWidth().padding(start = SojournSpacing.space4, end = SojournSpacing.space1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            label,
            modifier = Modifier.weight(1f),
            style = SojournTheme.type.label.copy(color = SojournTheme.colors.ink),
        )
        SojournButton(
            text = stringResource(Res.string.results_sort),
            onClick = { onIntent(ResultsIntent.SortTapped) },
            variant = SojournButtonVariant.Ghost,
        )
    }
}

@Composable
private fun OfflineBanner(onRetry: () -> Unit) {
    SojournOfflineBanner(
        message = stringResource(Res.string.offline_banner),
        actionLabel = stringResource(Res.string.action_retry),
        onAction = onRetry,
        modifier = Modifier.padding(horizontal = SojournSpacing.space4),
    )
}

@Composable
private fun Empty(state: ResultsUiState, onIntent: (ResultsIntent) -> Unit) {
    // One way forward that changes the query: the filters when some are on, otherwise the search itself.
    val filtered = state.filters.activeCount > 0
    SojournEmptyState(
        icon = SojournIcon.NoResults,
        title = stringResource(Res.string.results_empty_title),
        body = stringResource(
            if (filtered) Res.string.results_empty_filters_body else Res.string.results_empty_search_body,
        ),
        action = if (filtered) {
            SojournEmptyStateAction(stringResource(Res.string.action_clear_filters)) {
                onIntent(ResultsIntent.ClearFiltersTapped)
            }
        } else {
            SojournEmptyStateAction(stringResource(Res.string.action_change_search)) {
                onIntent(ResultsIntent.SearchTapped)
            }
        },
    )
}

@Composable
private fun Message(icon: SojournIcon, title: StringResource, body: StringResource, onRetry: () -> Unit) {
    SojournEmptyState(
        icon = icon,
        title = stringResource(title),
        body = stringResource(body),
        action = SojournEmptyStateAction(stringResource(Res.string.action_retry), onRetry),
    )
}

@Composable
private fun Skeletons() {
    val loading = stringResource(Res.string.state_loading)
    Column(
        Modifier
            .padding(horizontal = SojournSpacing.space4)
            .clearAndSetSemantics { contentDescription = loading },
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space5),
    ) {
        repeat(SKELETON_CARDS) { SojournStayCardSkeleton() }
    }
}

private const val SKELETON_CARDS = 2

@Composable
private fun SortSheet(selected: SortOrder, onIntent: (ResultsIntent) -> Unit) {
    val sheet = rememberSojournSheetState(SojournSheetDetent.Half, setOf(SojournSheetDetent.Half))
    SojournSheet(
        state = sheet,
        title = stringResource(Res.string.results_sort_title),
        onDismiss = { onIntent(ResultsIntent.SortDismissed) },
    ) {
        SortOrder.entries.forEach { order ->
            val isSelected = order == selected
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onIntent(ResultsIntent.SortSelected(order)) },
                    )
                    .padding(vertical = SojournSpacing.space3),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    stringResource(order.label),
                    modifier = Modifier.weight(1f),
                    style = SojournTheme.type.body.copy(
                        color = SojournTheme.colors.ink,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                )
                if (isSelected) {
                    SojournIcon(
                        SojournIcon.Check,
                        contentDescription = null,
                        tint = SojournTheme.colors.brand,
                        size = SojournSizes.icon,
                    )
                }
            }
        }
    }
}

private val SortOrder.label: StringResource
    get() = when (this) {
        SortOrder.Recommended -> Res.string.sort_recommended
        SortOrder.PriceLowToHigh -> Res.string.sort_price_low_to_high
        SortOrder.Rating -> Res.string.sort_rating
        SortOrder.DistanceToCentre -> Res.string.sort_distance
    }

/** The list clears the bottom nav and the floating Map toggle above it. */
private fun listPadding(padding: PaddingValues) = PaddingValues(
    bottom = padding.calculateBottomPadding() + SojournSizes.touchMin + SojournSpacing.space8,
)
