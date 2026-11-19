package com.abulubad.sojourn.feature.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.button.SojournButtonVariant
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButton
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButtonVariant
import com.abulubad.sojourn.core.designsystem.components.map.SojournMap
import com.abulubad.sojourn.core.designsystem.components.map.SojournMapCamera
import com.abulubad.sojourn.core.designsystem.components.map.SojournMapState
import com.abulubad.sojourn.core.designsystem.components.map.SojournPinGroup
import com.abulubad.sojourn.core.designsystem.components.map.rememberSojournMapState
import com.abulubad.sojourn.core.designsystem.components.map.sojournPinGroups
import com.abulubad.sojourn.core.designsystem.components.pricepin.SojournClusterPin
import com.abulubad.sojourn.core.designsystem.components.pricepin.SojournPinState
import com.abulubad.sojourn.core.designsystem.components.pricepin.SojournPricePin
import com.abulubad.sojourn.core.designsystem.components.pricepin.SojournPricePinDefaults
import com.abulubad.sojourn.core.designsystem.components.searchbar.SojournSearchBar
import com.abulubad.sojourn.core.designsystem.components.snackbar.SojournOfflineBanner
import com.abulubad.sojourn.core.designsystem.components.staycard.SojournStayCard
import com.abulubad.sojourn.core.designsystem.components.staycard.SojournStayCardLayout
import com.abulubad.sojourn.core.designsystem.components.viewtoggle.SojournResultsView
import com.abulubad.sojourn.core.designsystem.components.viewtoggle.SojournSearchAreaPill
import com.abulubad.sojourn.core.designsystem.components.viewtoggle.SojournViewToggle
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournMotion
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.domain.StayResult
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_change_search
import com.abulubad.sojourn.core.localization.resources.action_clear_filters
import com.abulubad.sojourn.core.localization.resources.action_retry
import com.abulubad.sojourn.core.localization.resources.action_show_all_stays
import com.abulubad.sojourn.core.localization.resources.cd_filters
import com.abulubad.sojourn.core.localization.resources.map_area_empty_body
import com.abulubad.sojourn.core.localization.resources.map_area_empty_title
import com.abulubad.sojourn.core.localization.resources.offline_banner
import com.abulubad.sojourn.core.localization.resources.results_empty_filters_body
import com.abulubad.sojourn.core.localization.resources.results_empty_search_body
import com.abulubad.sojourn.core.localization.resources.results_empty_title
import com.abulubad.sojourn.core.localization.resources.state_error_body
import com.abulubad.sojourn.core.localization.resources.state_error_title
import com.abulubad.sojourn.core.localization.resources.state_loading
import com.abulubad.sojourn.core.localization.resources.state_offline_body
import com.abulubad.sojourn.core.localization.resources.state_offline_title
import com.abulubad.sojourn.core.model.GeoPoint
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournScaffold
import com.abulubad.sojourn.core.ui.SojournTabBar
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * The map (ScreenMap card): "where, and for how much".
 *
 * - The search summary and the filter button with its count sit on top; "Search this area" appears under them once
 *   the guest has panned or zoomed, and only then.
 * - Pins carry the price the card shows (default, seen, selected); overlapping pins merge into a cluster with a
 *   count, and tapping a cluster zooms in on it. Selecting a pin raises one compact card; swiping the card moves the
 *   selection to the next pin, and the map follows it when it is out of view.
 * - "List" is always visible above the card. The attribution sits just above both and is never covered.
 * - The map is not mirrored in RTL; the search bar, pill, toggle and card are.
 * - Offline, cached pins stay under the banner. An empty search, an empty area and an error say so in a note above
 *   "List", with one way forward; the map stays usable.
 */
@Composable
fun MapScreen(state: MapUiState, onIntent: (MapIntent) -> Unit, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    var headerHeight by remember { mutableStateOf(0.dp) }
    var dockHeight by remember { mutableStateOf(0.dp) }
    SojournScaffold(modifier, bottomBar = { SojournTabBar() }) { padding ->
        Box(Modifier.fillMaxSize()) {
            val start = state.cityCenter ?: state.found.firstOrNull()?.stay?.location?.center
            if (start == null) {
                Box(Modifier.fillMaxSize().background(SojournTheme.colors.mapLand))
            } else {
                Pins(
                    state = state,
                    onIntent = onIntent,
                    mapState = rememberSojournMapState(start),
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding() + headerHeight,
                        bottom = padding.calculateBottomPadding() + dockHeight,
                    ),
                )
            }
            Header(
                state,
                onIntent,
                padding,
                Modifier.onSizeChanged { headerHeight = with(density) { it.height.toDp() } },
            )
            Dock(
                state,
                onIntent,
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = padding.calculateBottomPadding())
                    .onSizeChanged { dockHeight = with(density) { it.height.toDp() } },
            )
        }
    }
}

/** The map and its pins; fits the stays once, then the guest's camera is the guest's. */
@Composable
private fun Pins(
    state: MapUiState,
    onIntent: (MapIntent) -> Unit,
    mapState: SojournMapState,
    contentPadding: PaddingValues,
) {
    val scope = rememberCoroutineScope()
    var fitted by rememberSaveable { mutableStateOf(false) }
    val stays = state.stays
    LaunchedEffect(stays, fitted) {
        if (fitted || stays.isEmpty()) return@LaunchedEffect
        // Every stay found, the selected one (Details' "Show on map") among them with its card raised.
        mapState.show(stays.map { it.position }, animate = false)
        fitted = true
    }
    LaunchedEffect(state.selected) {
        val point = state.selectedStay?.position ?: return@LaunchedEffect
        val bounds = mapState.visibleBounds ?: return@LaunchedEffect
        if (point !in bounds) mapState.moveTo(SojournMapCamera(point, mapState.camera.zoom))
    }
    // Regroup by quarter zoom levels, not on every frame of a pinch.
    val zoom = (mapState.camera.zoom * ZOOM_STEPS).roundToInt() / ZOOM_STEPS
    // Pins grow with the guest's text size, so the distance at which they merge grows with them.
    val distance = SojournPricePinDefaults.ClusterDistance * LocalDensity.current.fontScale
    val groups = remember(stays, zoom, state.selected, distance) {
        sojournPinGroups(stays, zoom, { it.position }, keep = { it.stay.id == state.selected }, distance = distance)
    }
    SojournMap(
        state = mapState,
        contentPadding = contentPadding,
        onGuestMoved = { onIntent(MapIntent.GuestMoved(it)) },
        onMapTapped = { onIntent(MapIntent.MapTapped) },
    ) {
        groups.forEach { group ->
            when (group) {
                is SojournPinGroup.Single -> key(group.item.stay.id) {
                    val id = group.item.stay.id
                    SojournPricePin(
                        price = group.item.price,
                        state = when (id) {
                            state.selected -> SojournPinState.Selected
                            in state.seen -> SojournPinState.Seen
                            else -> SojournPinState.Default
                        },
                        onClick = { onIntent(MapIntent.PinTapped(id)) },
                        modifier = Modifier.at(group.position),
                    )
                }
                is SojournPinGroup.Cluster -> key(group.items.first().stay.id, group.items.size) {
                    SojournClusterPin(
                        count = group.items.size,
                        onClick = { scope.launch { mapState.show(group.positions) } },
                        modifier = Modifier.at(group.position),
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(state: MapUiState, onIntent: (MapIntent) -> Unit, padding: PaddingValues, modifier: Modifier) {
    val direction = LocalLayoutDirection.current
    Column(
        modifier.padding(
            start = padding.calculateStartPadding(direction),
            end = padding.calculateEndPadding(direction),
            top = padding.calculateTopPadding(),
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
    ) {
        Row(
            Modifier.padding(horizontal = SojournSpacing.space2, vertical = SojournSpacing.space1),
            horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SojournSearchBar(
                query = state.query,
                onClick = { onIntent(MapIntent.SearchTapped) },
                onBack = { onIntent(MapIntent.BackTapped) },
                modifier = Modifier.weight(1f),
            )
            SojournIconButton(
                icon = SojournIcon.Filters,
                contentDescription = stringResource(Res.string.cd_filters),
                onClick = { onIntent(MapIntent.FiltersTapped) },
                variant = SojournIconButtonVariant.Outline,
                badge = state.filters.activeCount.takeIf { it > 0 },
            )
        }
        if (state.results is ScreenState.Offline && state.found.isNotEmpty()) {
            SojournOfflineBanner(stringResource(Res.string.offline_banner))
        }
        when {
            state.showSearchArea -> SojournSearchAreaPill(onClick = { onIntent(MapIntent.SearchAreaTapped) })
            state.results == ScreenState.Loading -> Label(stringResource(Res.string.state_loading))
        }
    }
}

/** What docks at the bottom: a note when there is nothing to show, "List", and the selected stay's card. */
@Composable
private fun Dock(state: MapUiState, onIntent: (MapIntent) -> Unit, modifier: Modifier) {
    Column(
        modifier.fillMaxWidth().padding(bottom = SojournSpacing.space3),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
    ) {
        Note(state, onIntent)
        SojournViewToggle(current = SojournResultsView.Map, onToggle = { onIntent(MapIntent.ListTapped) })
        Cards(state, onIntent)
    }
}

/** One compact card at a time; a swipe settles on the next stay and selects its pin. */
@Composable
private fun Cards(state: MapUiState, onIntent: (MapIntent) -> Unit) {
    val stays = state.stays
    val selectedIndex = stays.indexOfFirst { it.stay.id == state.selected }
    AnimatedVisibility(
        visible = selectedIndex >= 0,
        enter = slideInVertically(tween(SojournMotion.base, easing = SojournMotion.emphasized)) { it } +
            fadeIn(tween(SojournMotion.base, easing = SojournMotion.emphasized)),
        exit = slideOutVertically(tween(SojournMotion.fast, easing = SojournMotion.exit)) { it } +
            fadeOut(tween(SojournMotion.fast, easing = SojournMotion.exit)),
    ) {
        key(stays) {
            val pager = rememberPagerState(initialPage = selectedIndex.coerceAtLeast(0)) { stays.size }
            val selected by rememberUpdatedState(state.selected)
            LaunchedEffect(selectedIndex) {
                if (selectedIndex >= 0 && selectedIndex != pager.currentPage) pager.animateScrollToPage(selectedIndex)
            }
            LaunchedEffect(pager) {
                snapshotFlow { pager.settledPage }.collect { page ->
                    val id = stays.getOrNull(page)?.stay?.id ?: return@collect
                    if (id != selected && selected != null) onIntent(MapIntent.CardShown(id))
                }
            }
            HorizontalPager(
                state = pager,
                contentPadding = PaddingValues(horizontal = SojournSpacing.space3),
                pageSpacing = SojournSpacing.space2,
            ) { page ->
                val result = stays[page]
                SojournStayCard(
                    stay = result.stay,
                    price = result.price,
                    saved = result.saved,
                    onClick = { onIntent(MapIntent.StayTapped(result)) },
                    onToggleSave = { onIntent(MapIntent.SaveTapped(result.stay.id)) },
                    layout = SojournStayCardLayout.Compact,
                    // Only the card in view is read: the neighbours peek in at the edges with their content off
                    // screen, and would be empty buttons to a screen reader. The pager still scrolls to them.
                    modifier = if (page == pager.currentPage) Modifier else Modifier.clearAndSetSemantics {},
                )
            }
        }
    }
}

/** Why there are no pins, and one way forward that changes what is shown. */
@Composable
private fun Note(state: MapUiState, onIntent: (MapIntent) -> Unit) {
    val filtered = state.filters.activeCount > 0
    when (val results = state.results) {
        ScreenState.Empty -> NoteCard(
            Res.string.results_empty_title,
            if (filtered) Res.string.results_empty_filters_body else Res.string.results_empty_search_body,
            if (filtered) Res.string.action_clear_filters else Res.string.action_change_search,
        ) { onIntent(if (filtered) MapIntent.ClearFiltersTapped else MapIntent.SearchTapped) }
        is ScreenState.Error ->
            NoteCard(Res.string.state_error_title, Res.string.state_error_body, Res.string.action_retry) {
                onIntent(MapIntent.RetryTapped)
            }
        is ScreenState.Offline -> if (results.cached == null) {
            NoteCard(Res.string.state_offline_title, Res.string.state_offline_body, Res.string.action_retry) {
                onIntent(MapIntent.RetryTapped)
            }
        }
        else -> if (state.found.isNotEmpty() && state.stays.isEmpty()) {
            NoteCard(
                Res.string.map_area_empty_title,
                Res.string.map_area_empty_body,
                Res.string.action_show_all_stays,
            ) { onIntent(MapIntent.ShowAllTapped) }
        }
    }
}

@Composable
private fun NoteCard(title: StringResource, body: StringResource, action: StringResource, onAction: () -> Unit) {
    val c = SojournTheme.colors
    Column(
        Modifier
            .padding(horizontal = SojournSpacing.space3)
            .fillMaxWidth()
            .clip(SojournShapes.lg)
            .background(c.surface)
            .padding(SojournSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
    ) {
        BasicText(stringResource(title), style = SojournTheme.type.titleSm.copy(color = c.ink))
        BasicText(stringResource(body), style = SojournTheme.type.bodySm.copy(color = c.inkMuted))
        SojournButton(stringResource(action), onClick = onAction, variant = SojournButtonVariant.Secondary)
    }
}

/** A quiet status pill under the search bar ("Loading"). */
@Composable
private fun Label(text: String) {
    val c = SojournTheme.colors
    BasicText(
        text,
        modifier = Modifier
            .clip(SojournShapes.full)
            .background(c.surface)
            .padding(horizontal = SojournSpacing.space3, vertical = SojournSpacing.space1),
        style = SojournTheme.type.labelSm.copy(color = c.inkMuted),
    )
}

private val StayResult.position: GeoPoint get() = stay.location.center

private const val ZOOM_STEPS = 4.0
