package com.abulubad.sojourn.core.designsystem.components.bookingcard

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.components.internal.SampleListings
import com.abulubad.sojourn.core.designsystem.components.internal.WithSamplePhotos
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.localizedPlace
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_check_in_guide
import com.abulubad.sojourn.core.localization.resources.action_directions
import com.abulubad.sojourn.core.localization.resources.adults
import com.abulubad.sojourn.core.localization.resources.bookings_check_in_in
import com.abulubad.sojourn.core.localization.resources.bookings_details
import com.abulubad.sojourn.core.localization.resources.days
import com.abulubad.sojourn.core.localization.resources.nights
import com.abulubad.sojourn.core.model.DateRange
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** The ScreenBookings card's upcoming booking, with its countdown and both actions. */
@Suppress("MagicNumber") // Sample data: the card's dates.
object SojournBookingCardSamples {

    @Composable
    fun Upcoming(modifier: Modifier = Modifier) = WithSamplePhotos {
        val stay = SampleListings.canalStudio
        val formatter = rememberSojournFormatter()
        val dates = DateRange(LocalDate(2027, 3, 12), LocalDate(2027, 3, 27))
        SojournBookingCard(
            photo = stay.photos.first(),
            title = stay.title,
            place = localizedPlace(stay.cityId, stay.areaKey),
            details = stringResource(
                Res.string.bookings_details,
                formatter.dateRange(dates),
                pluralStringResource(Res.plurals.nights, dates.nights, formatter.count(dates.nights)),
                pluralStringResource(Res.plurals.adults, ADULTS, formatter.count(ADULTS)),
            ),
            reference = "SJ-4F7K2Q",
            badge = stringResource(
                Res.string.bookings_check_in_in,
                pluralStringResource(Res.plurals.days, DAYS, formatter.count(DAYS)),
            ),
            modifier = modifier.padding(SojournSpacing.space4),
            onClick = {},
            actions = listOf(
                SojournBookingCardAction(stringResource(Res.string.action_directions), {}, SojournIcon.MapPin),
                SojournBookingCardAction(stringResource(Res.string.action_check_in_guide), {}, primary = true),
            ),
        )
    }

    private const val DAYS = 12
    private const val ADULTS = 2
}
