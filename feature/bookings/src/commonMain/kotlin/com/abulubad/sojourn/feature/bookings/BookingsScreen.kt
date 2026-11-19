package com.abulubad.sojourn.feature.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyState
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyStateAction
import com.abulubad.sojourn.core.designsystem.components.gallery.SojournGalleryDefaults
import com.abulubad.sojourn.core.designsystem.components.segmented.SojournSegment
import com.abulubad.sojourn.core.designsystem.components.segmented.SojournSegmentedControl
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheetState
import com.abulubad.sojourn.core.designsystem.components.skeleton.SojournSkeleton
import com.abulubad.sojourn.core.designsystem.components.snackbar.SojournSnackbarHost
import com.abulubad.sojourn.core.designsystem.components.snackbar.SojournSnackbarHostState
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_explore_stays
import com.abulubad.sojourn.core.localization.resources.booking_status_upcoming
import com.abulubad.sojourn.core.localization.resources.bookings_empty_past_body
import com.abulubad.sojourn.core.localization.resources.bookings_empty_past_title
import com.abulubad.sojourn.core.localization.resources.bookings_empty_upcoming_body
import com.abulubad.sojourn.core.localization.resources.bookings_empty_upcoming_title
import com.abulubad.sojourn.core.localization.resources.bookings_past
import com.abulubad.sojourn.core.localization.resources.tab_bookings
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournScaffold
import com.abulubad.sojourn.core.ui.SojournTabBar
import org.jetbrains.compose.resources.stringResource

/**
 * The Bookings tab (ScreenBookings card): "Bookings", then Upcoming or Past.
 *
 * - Upcoming: each stay as a card with its countdown, "Directions" and "Check-in guide"; the first (the next arrival)
 *   has its terms under it: free cancellation and after, the deposit, check-in.
 * - Past: completed and cancelled stays, as a record.
 * - A card opens the booking's page. Bookings are on the device, so the tab works offline and has no error page.
 */
@Composable
fun BookingsScreen(
    state: BookingsUiState,
    onIntent: (BookingsIntent) -> Unit,
    modifier: Modifier = Modifier,
    guideSheet: SojournSheetState = rememberHalfSheetState(),
    snackbar: SojournSnackbarHostState = SojournSnackbarHostState(),
) {
    SojournScaffold(modifier, bottomBar = { SojournTabBar() }) { padding ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = SojournSpacing.space4,
                    end = SojournSpacing.space4,
                    top = padding.calculateTopPadding() + SojournSpacing.space2,
                    bottom = padding.calculateBottomPadding() + SojournSpacing.space6,
                ),
                verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
            ) {
                item("title") {
                    BasicText(
                        stringResource(Res.string.tab_bookings),
                        modifier = Modifier.semantics { heading() },
                        style = SojournTheme.type.titleLg.copy(color = SojournTheme.colors.ink),
                    )
                }
                item("tabs") {
                    SojournSegmentedControl(
                        options = listOf(
                            SojournSegment(BookingsTab.Upcoming, stringResource(Res.string.booking_status_upcoming)),
                            SojournSegment(BookingsTab.Past, stringResource(Res.string.bookings_past)),
                        ),
                        selected = state.tab,
                        onSelect = { onIntent(BookingsIntent.TabSelected(it)) },
                    )
                }
                when {
                    state.bookings == ScreenState.Loading -> item("loading") {
                        SojournSkeleton(
                            Modifier.fillMaxWidth().aspectRatio(SojournGalleryDefaults.ASPECT_RATIO),
                            SojournShapes.lg,
                        )
                    }
                    state.shown.isEmpty() -> item("empty") { Empty(state.tab, onIntent) }
                    else -> itemsIndexed(state.shown, key = { _, shown -> shown.booking.id.value }) { index, item ->
                        BookingCard(
                            item = item,
                            today = state.today,
                            onDirections = { onIntent(BookingsIntent.DirectionsTapped(item)) },
                            onGuide = { onIntent(BookingsIntent.GuideTapped(item)) },
                            onClick = { onIntent(BookingsIntent.BookingTapped(item)) },
                        )
                        if (index == 0 && state.tab == BookingsTab.Upcoming) {
                            BookingPolicies(item, Modifier.padding(top = SojournSpacing.space2))
                        }
                    }
                }
            }
            SojournSnackbarHost(
                snackbar,
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = padding.calculateBottomPadding() + SojournSpacing.space3)
                    .padding(horizontal = SojournSpacing.space3),
            )
        }
    }
    state.guide?.let { item ->
        GuideSheet(
            item = item,
            today = state.today,
            sheetState = guideSheet,
            onCopy = { text, what -> onIntent(BookingsIntent.CopyTapped(text, what)) },
            onDismiss = { onIntent(BookingsIntent.GuideDismissed) },
        )
    }
}

@Composable
private fun Empty(tab: BookingsTab, onIntent: (BookingsIntent) -> Unit) {
    if (tab == BookingsTab.Upcoming) {
        SojournEmptyState(
            icon = SojournIcon.Bookings,
            title = stringResource(Res.string.bookings_empty_upcoming_title),
            body = stringResource(Res.string.bookings_empty_upcoming_body),
            modifier = Modifier.padding(top = SojournSpacing.space10),
            action = SojournEmptyStateAction(stringResource(Res.string.action_explore_stays)) {
                onIntent(BookingsIntent.ExploreTapped)
            },
        )
    } else {
        SojournEmptyState(
            icon = SojournIcon.Bookings,
            title = stringResource(Res.string.bookings_empty_past_title),
            body = stringResource(Res.string.bookings_empty_past_body),
            modifier = Modifier.padding(top = SojournSpacing.space10),
        )
    }
}
