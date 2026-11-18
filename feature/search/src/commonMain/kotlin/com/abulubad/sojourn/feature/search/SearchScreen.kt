package com.abulubad.sojourn.feature.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.abulubad.sojourn.core.designsystem.components.actionbar.SojournActionBar
import com.abulubad.sojourn.core.designsystem.components.chip.SojournChip
import com.abulubad.sojourn.core.designsystem.components.citycard.SojournCityCardDefaults
import com.abulubad.sojourn.core.designsystem.components.citycard.SojournCityRow
import com.abulubad.sojourn.core.designsystem.components.daterange.SojournDateRangePicker
import com.abulubad.sojourn.core.designsystem.components.daterange.SojournDayState
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyState
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyStateAction
import com.abulubad.sojourn.core.designsystem.components.segmented.SojournSegment
import com.abulubad.sojourn.core.designsystem.components.segmented.SojournSegmentedControl
import com.abulubad.sojourn.core.designsystem.components.skeleton.SojournSkeleton
import com.abulubad.sojourn.core.designsystem.components.snackbar.SojournOfflineBanner
import com.abulubad.sojourn.core.designsystem.components.stepper.SojournStepper
import com.abulubad.sojourn.core.designsystem.components.topbar.SojournStepTopBar
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.theme.SojournTransitions
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_next
import com.abulubad.sojourn.core.localization.resources.action_retry
import com.abulubad.sojourn.core.localization.resources.action_search
import com.abulubad.sojourn.core.localization.resources.cities_empty_body
import com.abulubad.sojourn.core.localization.resources.cities_offline_body
import com.abulubad.sojourn.core.localization.resources.guests_adults
import com.abulubad.sojourn.core.localization.resources.guests_children
import com.abulubad.sojourn.core.localization.resources.guests_children_hint
import com.abulubad.sojourn.core.localization.resources.length_choice_few_nights
import com.abulubad.sojourn.core.localization.resources.length_choice_longer
import com.abulubad.sojourn.core.localization.resources.length_choice_month
import com.abulubad.sojourn.core.localization.resources.length_choice_week
import com.abulubad.sojourn.core.localization.resources.nights
import com.abulubad.sojourn.core.localization.resources.offline_banner
import com.abulubad.sojourn.core.localization.resources.search_any_time
import com.abulubad.sojourn.core.localization.resources.search_any_time_note
import com.abulubad.sojourn.core.localization.resources.search_cities_label
import com.abulubad.sojourn.core.localization.resources.search_flexible_note
import com.abulubad.sojourn.core.localization.resources.search_mode_dates
import com.abulubad.sojourn.core.localization.resources.search_mode_flexible
import com.abulubad.sojourn.core.localization.resources.search_prompt_title
import com.abulubad.sojourn.core.localization.resources.search_starting_in
import com.abulubad.sojourn.core.localization.resources.search_stay_for
import com.abulubad.sojourn.core.localization.resources.search_when_title
import com.abulubad.sojourn.core.localization.resources.search_who_title
import com.abulubad.sojourn.core.localization.resources.state_empty_title
import com.abulubad.sojourn.core.localization.resources.state_error_body
import com.abulubad.sojourn.core.localization.resources.state_error_title
import com.abulubad.sojourn.core.localization.resources.state_loading
import com.abulubad.sojourn.core.localization.resources.state_offline_title
import com.abulubad.sojourn.core.model.City
import com.abulubad.sojourn.core.model.StayLength
import com.abulubad.sojourn.core.navigation.SearchStep
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournScaffold
import com.abulubad.sojourn.core.ui.StepBackHandler
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The search flow (ScreenSearchFlow card): Where, When and Who, one step at a time under a progress bar. Stateless:
 * [state] in, [onIntent] out.
 *
 * - Steps slide along the reading direction (`SojournTransitions.stepForward` / `stepBack`), mirrored in RTL.
 * - System back and the bar's button go one step back; on Where they close the flow.
 * - Where lists the cities offline-first: cached cities under the offline banner, otherwise the offline or error
 *   state with a retry. There is no search field while the list is this short (the card's 12-city rule).
 * - When's footer offers "Any time", a deliberate choice whose effect Who states, and shows the night count as
 *   soon as a range exists.
 */
@Composable
fun SearchScreen(state: SearchUiState, onIntent: (SearchIntent) -> Unit, modifier: Modifier = Modifier) {
    val back = { onIntent(SearchIntent.BackTapped) }
    StepBackHandler(enabled = state.step != SearchStep.Where, onBack = back)
    SojournScaffold(
        modifier,
        topBar = { SojournStepTopBar(step = state.step.ordinal + 1, steps = SearchStep.entries.size, onBack = back) },
        bottomBar = { Footer(state, onIntent) },
    ) { padding ->
        val forward = SojournTransitions.stepForward()
        val backward = SojournTransitions.stepBack()
        AnimatedContent(
            targetState = state.step,
            transitionSpec = { if (targetState > initialState) forward else backward },
        ) { step ->
            when (step) {
                SearchStep.Where -> WhereStep(state.cities, padding, onIntent)
                SearchStep.When -> WhenStep(state, padding, onIntent)
                SearchStep.Who -> WhoStep(state, padding, onIntent)
            }
        }
    }
}

@Composable
private fun Footer(state: SearchUiState, onIntent: (SearchIntent) -> Unit) {
    when (state.step) {
        SearchStep.Where -> Unit
        SearchStep.When -> {
            val formatter = rememberSojournFormatter()
            val nights = state.dates?.nights?.takeIf { state.mode == WhenMode.Dates }
            SojournActionBar(
                primary = stringResource(Res.string.action_next),
                onPrimary = { onIntent(SearchIntent.NextTapped) },
                primaryEnabled = state.canContinueFromWhen,
                secondary = stringResource(Res.string.search_any_time),
                onSecondary = { onIntent(SearchIntent.AnyTimeTapped) },
                note = nights?.let { pluralStringResource(Res.plurals.nights, it, formatter.count(it)) },
            )
        }
        SearchStep.Who -> SojournActionBar(
            primary = stringResource(Res.string.action_search),
            onPrimary = { onIntent(SearchIntent.SearchTapped) },
        )
    }
}

@Composable
private fun WhereStep(cities: ScreenState<List<City>>, padding: PaddingValues, onIntent: (SearchIntent) -> Unit) {
    val retry = SojournEmptyStateAction(stringResource(Res.string.action_retry)) { onIntent(SearchIntent.RetryTapped) }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = stepPadding(padding),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
    ) {
        item("title") { StepTitle(Res.string.search_prompt_title) }
        if (cities is ScreenState.Offline && cities.cached != null) {
            item("offline") {
                SojournOfflineBanner(
                    message = stringResource(Res.string.offline_banner),
                    actionLabel = stringResource(Res.string.action_retry),
                    onAction = { onIntent(SearchIntent.RetryTapped) },
                )
            }
        }
        item("label") { Label(Res.string.search_cities_label) }
        when (cities) {
            ScreenState.Loading -> item("loading") { CityRowSkeletons() }
            is ScreenState.Content -> cityRows(cities.data, onIntent)
            is ScreenState.Offline -> cities.cached?.let { cityRows(it, onIntent) } ?: item("offline_empty") {
                SojournEmptyState(
                    icon = SojournIcon.Offline,
                    title = stringResource(Res.string.state_offline_title),
                    body = stringResource(Res.string.cities_offline_body),
                    action = retry,
                )
            }
            ScreenState.Empty -> item("empty") {
                SojournEmptyState(
                    icon = SojournIcon.City,
                    title = stringResource(Res.string.state_empty_title),
                    body = stringResource(Res.string.cities_empty_body),
                )
            }
            is ScreenState.Error -> item("error") {
                SojournEmptyState(
                    icon = SojournIcon.Alert,
                    title = stringResource(Res.string.state_error_title),
                    body = stringResource(Res.string.state_error_body),
                    action = retry,
                )
            }
        }
    }
}

private fun LazyListScope.cityRows(
    cities: List<City>,
    onIntent: (SearchIntent) -> Unit,
) {
    items(cities, key = { it.id.value }) { city ->
        SojournCityRow(city, onClick = { onIntent(SearchIntent.CityTapped(city.id)) })
    }
}

@Composable
private fun CityRowSkeletons() {
    val loading = stringResource(Res.string.state_loading)
    Column(
        Modifier.clearAndSetSemantics { contentDescription = loading },
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
    ) {
        repeat(SKELETON_ROWS) {
            Row(horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3)) {
                SojournSkeleton(Modifier.size(SojournCityCardDefaults.RowPhotoSize), SojournShapes.md)
                Column(
                    Modifier.weight(1f).padding(top = SojournSpacing.space2),
                    verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
                ) {
                    SojournSkeleton(Modifier.fillMaxWidth(SKELETON_TITLE).height(SojournSpacing.space4))
                    SojournSkeleton(Modifier.fillMaxWidth().height(SojournSpacing.space3))
                }
            }
        }
    }
}

@Composable
private fun WhenStep(state: SearchUiState, padding: PaddingValues, onIntent: (SearchIntent) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(stepPadding(padding)),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
    ) {
        StepTitle(Res.string.search_when_title)
        SojournSegmentedControl(
            options = listOf(
                SojournSegment(WhenMode.Dates, stringResource(Res.string.search_mode_dates)),
                SojournSegment(WhenMode.Flexible, stringResource(Res.string.search_mode_flexible)),
            ),
            selected = state.mode,
            onSelect = { onIntent(SearchIntent.ModeSelected(it)) },
        )
        when (state.mode) {
            WhenMode.Dates -> SojournDateRangePicker(
                month = state.month,
                onMonthChange = { onIntent(SearchIntent.MonthShown(it)) },
                selection = state.dates,
                onSelect = { onIntent(SearchIntent.DatesSelected(it)) },
                availability = { day ->
                    if (day < state.today) SojournDayState.Unavailable else SojournDayState.Available
                },
                // The footer shows the count, beside "Next".
                showNightCount = false,
            )
            WhenMode.Flexible -> Flexible(state, onIntent)
        }
    }
}

@Composable
private fun Flexible(state: SearchUiState, onIntent: (SearchIntent) -> Unit) {
    val formatter = rememberSojournFormatter()
    Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3)) {
        Label(Res.string.search_stay_for)
        ChipRow {
            LENGTHS.forEach { (length, label) ->
                SojournChip(
                    label = stringResource(label),
                    selected = state.length == length,
                    onClick = { onIntent(SearchIntent.LengthSelected(length)) },
                )
            }
        }
        Label(Res.string.search_starting_in, Modifier.padding(top = SojournSpacing.space2))
        ChipRow {
            state.startMonths.forEach { month ->
                SojournChip(
                    // The year only when it is not this one: "January 2027" in December.
                    label = if (month.year == state.today.year) {
                        formatter.monthLong(month.month)
                    } else {
                        formatter.monthYear(month)
                    },
                    selected = state.startMonth == month,
                    onClick = { onIntent(SearchIntent.StartMonthSelected(month)) },
                )
            }
        }
        Note(SojournIcon.Calendar, Res.string.search_flexible_note, Modifier.padding(top = SojournSpacing.space2))
    }
}

/** The four lengths Explore's shortcuts use, in the same words. */
private val LENGTHS: List<Pair<StayLength, StringResource>> = listOf(
    StayLength.Weekend to Res.string.length_choice_few_nights,
    StayLength.Week to Res.string.length_choice_week,
    StayLength.Month to Res.string.length_choice_month,
    StayLength.Longer to Res.string.length_choice_longer,
)

@Composable
private fun WhoStep(state: SearchUiState, padding: PaddingValues, onIntent: (SearchIntent) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(stepPadding(padding)),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
    ) {
        StepTitle(Res.string.search_who_title)
        SojournStepper(
            value = state.guests.adults,
            onValueChange = { onIntent(SearchIntent.AdultsChanged(it)) },
            range = SearchUiState.Adults,
            label = stringResource(Res.string.guests_adults),
        )
        SojournStepper(
            value = state.guests.children,
            onValueChange = { onIntent(SearchIntent.ChildrenChanged(it)) },
            range = SearchUiState.Children,
            label = stringResource(Res.string.guests_children),
            hint = stringResource(Res.string.guests_children_hint),
        )
        if (state.anyTime) Note(SojournIcon.Info, Res.string.search_any_time_note)
    }
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
    ) { content() }
}

/** A tinted band with an icon: what a choice means for prices. */
@Composable
private fun Note(icon: SojournIcon, text: StringResource, modifier: Modifier = Modifier) {
    val c = SojournTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .background(c.surfaceSunken, SojournShapes.md)
            .padding(SojournSpacing.space3),
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SojournIcon(icon, contentDescription = null, tint = c.ink, size = SojournSizes.icon)
        BasicText(stringResource(text), Modifier.weight(1f), style = SojournTheme.type.bodySm.copy(color = c.ink))
    }
}

@Composable
private fun StepTitle(text: StringResource) {
    BasicText(
        stringResource(text),
        modifier = Modifier.padding(top = SojournSpacing.space2).semantics { heading() },
        style = SojournTheme.type.display.copy(color = SojournTheme.colors.ink),
    )
}

@Composable
private fun Label(text: StringResource, modifier: Modifier = Modifier) {
    BasicText(
        stringResource(text),
        modifier = modifier,
        style = SojournTheme.type.label.copy(color = SojournTheme.colors.inkMuted),
    )
}

/** The scaffold's padding (bars and side insets), plus the card's side margins and a little air at the end. */
@Composable
private fun stepPadding(padding: PaddingValues): PaddingValues {
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = padding.calculateStartPadding(direction) + SojournSpacing.space4,
        end = padding.calculateEndPadding(direction) + SojournSpacing.space4,
        top = padding.calculateTopPadding(),
        bottom = padding.calculateBottomPadding() + SojournSpacing.space6,
    )
}

private const val SKELETON_ROWS = 4
private const val SKELETON_TITLE = 0.5f
