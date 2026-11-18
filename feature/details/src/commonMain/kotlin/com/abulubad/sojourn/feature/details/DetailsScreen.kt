package com.abulubad.sojourn.feature.details

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.designsystem.components.bookingbar.SojournBookingBar
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyState
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyStateAction
import com.abulubad.sojourn.core.designsystem.components.gallery.SojournGallery
import com.abulubad.sojourn.core.designsystem.components.gallery.SojournGalleryDefaults
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButtonTint
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheetDetent
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheetState
import com.abulubad.sojourn.core.designsystem.components.sheet.rememberSojournSheetState
import com.abulubad.sojourn.core.designsystem.components.skeleton.SojournSkeleton
import com.abulubad.sojourn.core.designsystem.components.snackbar.SojournOfflineBanner
import com.abulubad.sojourn.core.designsystem.components.topbar.SojournTopBar
import com.abulubad.sojourn.core.designsystem.components.topbar.rememberTopBarSolidFraction
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.domain.StayDetails
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_back
import com.abulubad.sojourn.core.localization.resources.action_retry
import com.abulubad.sojourn.core.localization.resources.action_share
import com.abulubad.sojourn.core.localization.resources.cd_save
import com.abulubad.sojourn.core.localization.resources.cd_saved_heart
import com.abulubad.sojourn.core.localization.resources.details_gone_body
import com.abulubad.sojourn.core.localization.resources.details_gone_title
import com.abulubad.sojourn.core.localization.resources.offline_banner
import com.abulubad.sojourn.core.localization.resources.state_error_body
import com.abulubad.sojourn.core.localization.resources.state_error_title
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournScaffold
import org.jetbrains.compose.resources.stringResource

/**
 * Details (ScreenDetails card): the page that earns the booking.
 *
 * - The gallery runs under the clear top bar; once the title scrolls under it the bar turns solid and shows the
 *   title, so text never sits on a clear bar.
 * - Sections in the card's order: place and title, rating, key facts, what the price includes, where you'll stay,
 *   amenities ranked for longer stays, about this stay (in the host's language, with translate), good to know
 *   (policies with their numbers), then reviews when there are any.
 * - The booking bar is docked and never disabled: the total and "Reserve" with dates, the "from" price and
 *   "Choose dates" without. The content's bottom padding clears it and the navigation bar.
 * - Loading shows the page's shape; a stay the catalogue dropped says so with a way back; offline keeps the cached
 *   page under the banner.
 */
@Composable
fun DetailsScreen(
    state: DetailsUiState,
    onIntent: (DetailsIntent) -> Unit,
    modifier: Modifier = Modifier,
    datesSheet: SojournSheetState = rememberSojournSheetState(),
    priceSheet: SojournSheetState = rememberPriceSheetState(),
    scroll: ScrollState = rememberScrollState(),
) {
    val fraction = rememberTopBarSolidFraction(scroll)
    Box(modifier.fillMaxSize()) {
        SojournScaffold(
            topBar = { TopBar(state, onIntent, fraction) },
            bottomBar = {
                val price = state.price ?: return@SojournScaffold
                SojournBookingBar(
                    price = price,
                    onPrimary = { onIntent(DetailsIntent.PrimaryTapped) },
                    onPriceDetails = { onIntent(DetailsIntent.PriceDetailsTapped) },
                    dates = state.quote?.dates,
                )
            },
        ) { padding ->
            when (val details = state.details) {
                ScreenState.Loading -> Loading(padding)
                is ScreenState.Content -> Page(details.data, state, onIntent, scroll, padding, offline = false)
                is ScreenState.Offline -> details.cached?.let { Page(it, state, onIntent, scroll, padding, true) }
                    ?: Failed(AppError.Offline, onIntent, padding)
                is ScreenState.Error -> Failed(details.error, onIntent, padding)
                // A stay is never empty: Details maps no stay to an error.
                ScreenState.Empty -> Failed(AppError.NotFound(""), onIntent, padding)
            }
        }
        Sheets(state, onIntent, datesSheet, priceSheet)
    }
}

@Composable
private fun TopBar(state: DetailsUiState, onIntent: (DetailsIntent) -> Unit, fraction: () -> Float) {
    val stay = state.stay
    val saved = (state.details as? ScreenState.Content)?.data?.saved
        ?: (state.details as? ScreenState.Offline)?.cached?.saved
        ?: false
    SojournTopBar(
        title = stay?.title?.text,
        onBack = { onIntent(DetailsIntent.BackTapped) },
        overPhoto = stay != null,
        solidFraction = if (stay != null) fraction else ({ 1f }),
    ) {
        if (stay == null) return@SojournTopBar
        Action(
            SojournIcon.Share,
            stringResource(Res.string.action_share),
            onClick = { onIntent(DetailsIntent.ShareTapped) },
        )
        Action(
            icon = if (saved) SojournIcon.SavedFilled else SojournIcon.Saved,
            contentDescription = stringResource(if (saved) Res.string.cd_saved_heart else Res.string.cd_save),
            onClick = { onIntent(DetailsIntent.SaveTapped) },
            tint = if (saved) SojournIconButtonTint.Saved else SojournIconButtonTint.Default,
        )
    }
}

@Composable
private fun Page(
    details: StayDetails,
    state: DetailsUiState,
    onIntent: (DetailsIntent) -> Unit,
    scroll: ScrollState,
    padding: PaddingValues,
    offline: Boolean,
) {
    // The gallery runs under the top bar: only the bottom (booking bar) padding applies.
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(bottom = padding.calculateBottomPadding() + SojournSpacing.space6),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space5),
    ) {
        SojournGallery(details.stay.photos, onOpen = { onIntent(DetailsIntent.PhotoTapped(it)) })
        if (offline) {
            SojournOfflineBanner(
                stringResource(Res.string.offline_banner),
                modifier = Modifier.padding(horizontal = SojournSpacing.space4),
            )
        }
        Sections(details, state, onIntent)
    }
}

@Composable
private fun Loading(padding: PaddingValues) {
    Column(
        Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
    ) {
        SojournSkeleton(
            Modifier.fillMaxWidth().aspectRatio(SojournGalleryDefaults.ASPECT_RATIO),
            // Square, as the gallery it stands in for.
            shape = RectangleShape,
        )
        Column(
            Modifier.padding(horizontal = SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        ) {
            SojournSkeleton(Modifier.fillMaxWidth(SKELETON_SHORT).height(SojournSpacing.space4))
            SojournSkeleton(Modifier.fillMaxWidth().height(SojournSpacing.space6))
            SojournSkeleton(Modifier.fillMaxWidth(SKELETON_MEDIUM).height(SojournSpacing.space4))
            SojournSkeleton(Modifier.fillMaxWidth().height(SojournSpacing.space10), SojournShapes.md)
        }
    }
}

@Composable
private fun Failed(error: AppError, onIntent: (DetailsIntent) -> Unit, padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding).padding(SojournSpacing.space4)) {
        if (error is AppError.NotFound) {
            SojournEmptyState(
                icon = SojournIcon.NoResults,
                title = stringResource(Res.string.details_gone_title),
                body = stringResource(Res.string.details_gone_body),
                action = SojournEmptyStateAction(stringResource(Res.string.action_back)) {
                    onIntent(DetailsIntent.BackTapped)
                },
            )
        } else {
            SojournEmptyState(
                icon = if (error == AppError.Offline) SojournIcon.Offline else SojournIcon.Alert,
                title = stringResource(Res.string.state_error_title),
                body = stringResource(Res.string.state_error_body),
                action = SojournEmptyStateAction(stringResource(Res.string.action_retry)) {
                    onIntent(DetailsIntent.RetryTapped)
                },
            )
        }
    }
}

/** "Price details" is a few lines: it opens at half height, with the page still in view above it. */
@Composable
internal fun rememberPriceSheetState(): SojournSheetState = rememberSojournSheetState(
    SojournSheetDetent.Half,
    setOf(SojournSheetDetent.Half, SojournSheetDetent.Full),
)

private const val SKELETON_SHORT = 0.4f
private const val SKELETON_MEDIUM = 0.6f
