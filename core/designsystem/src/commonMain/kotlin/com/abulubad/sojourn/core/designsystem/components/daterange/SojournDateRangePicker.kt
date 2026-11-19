package com.abulubad.sojourn.core.designsystem.components.daterange

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButton
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.LocalReduceMotion
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.theme.rememberSojournHaptics
import com.abulubad.sojourn.core.designsystem.tokens.SojournMotion
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.cd_next_month
import com.abulubad.sojourn.core.localization.resources.cd_previous_month
import com.abulubad.sojourn.core.localization.resources.day_unavailable
import com.abulubad.sojourn.core.localization.resources.nights
import com.abulubad.sojourn.core.model.DateRange
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minusMonth
import kotlinx.datetime.plusMonth
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A month grid for choosing check-in and check-out (DateRangePicker card).
 *
 * - The first day of the week, month names and digits follow the in-app language: the English grid starts on
 *   Monday (UK), the Arabic grid on Saturday. The grid mirrors in RTL, so earlier days sit to the right.
 * - Unavailable days are struck through, not only dimmed, and cannot be focused or tapped.
 * - Tapping sets check-in, then check-out (rules in [nextSelection]); [onSelect] gets `null` while only check-in is
 *   chosen. The night count shows under the grid and is announced when a range completes: "15 nights". A screen
 *   whose footer shows the count instead (the search flow's `SojournActionBar`) turns it off with [showNightCount].
 */
@Composable
fun SojournDateRangePicker(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    selection: DateRange?,
    onSelect: (DateRange?) -> Unit,
    availability: (LocalDate) -> SojournDayState,
    modifier: Modifier = Modifier,
    minNights: Int = 1,
    showNightCount: Boolean = true,
) {
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    var checkIn by remember { mutableStateOf<LocalDate?>(null) }
    val isAvailable: (LocalDate) -> Boolean = { availability(it) == SojournDayState.Available }
    val weekDays = formatter.weekDays()
    val haptics = rememberSojournHaptics()
    val sweep = rememberRangeSweep(selection)
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.padding(vertical = SojournSpacing.space2), verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                formatter.monthYear(month),
                modifier = Modifier.weight(1f).semantics { heading() },
                style = SojournTheme.type.titleSm.copy(color = c.ink),
            )
            SojournIconButton(SojournIcon.ChevronStart, stringResource(Res.string.cd_previous_month), {
                onMonthChange(month.minusMonth())
            })
            SojournIconButton(SojournIcon.ChevronEnd, stringResource(Res.string.cd_next_month), {
                onMonthChange(month.plusMonth())
            })
        }
        Row(Modifier.clearAndSetSemantics {}) {
            weekDays.forEach { day ->
                Box(
                    Modifier.weight(1f).heightIn(min = SojournDateRangePickerDefaults.WeekdayHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        formatter.weekdayNarrow(day),
                        style = SojournTheme.type.caption.copy(color = c.inkMuted, fontWeight = FontWeight.Medium),
                    )
                }
            }
        }
        val leading = weekDays.indexOf(month.firstDay.dayOfWeek)
        val cells: List<LocalDate?> = List(leading) { null } + month.days.toList()
        cells.chunked(SojournDateRangePickerDefaults.DAYS_PER_WEEK).forEach { week ->
            Row {
                week.forEach { date ->
                    val cellModifier = Modifier.weight(1f)
                    if (date == null) {
                        Box(cellModifier)
                    } else {
                        DayCell(
                            date = date,
                            role = dayRole(date, selection, checkIn),
                            available = isAvailable(date),
                            modifier = cellModifier,
                            sweepPosition = selection?.let { sweepPosition(date, it) },
                            sweep = sweep,
                            onClick = {
                                haptics.tick()
                                when (val step = nextSelection(checkIn, date, minNights, isAvailable)) {
                                    is DateSelectionStep.CheckIn -> {
                                        checkIn = step.date
                                        onSelect(null)
                                    }
                                    is DateSelectionStep.Complete -> {
                                        checkIn = null
                                        onSelect(step.range)
                                    }
                                }
                            },
                        )
                    }
                }
                repeat(SojournDateRangePickerDefaults.DAYS_PER_WEEK - week.size) { Box(Modifier.weight(1f)) }
            }
        }
        if (selection != null && showNightCount) {
            BasicText(
                pluralStringResource(Res.plurals.nights, selection.nights, formatter.count(selection.nights)),
                modifier = Modifier
                    .padding(top = SojournSpacing.space2)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                style = SojournTheme.type.bodySm.copy(color = c.inkMuted),
            )
        }
    }
}

private enum class DayRole { None, CheckIn, CheckInOfRange, Between, CheckOut }

private fun dayRole(date: LocalDate, selection: DateRange?, checkIn: LocalDate?): DayRole = when {
    selection != null && date == selection.start -> DayRole.CheckInOfRange
    selection != null && date == selection.end -> DayRole.CheckOut
    selection != null && date > selection.start && date < selection.end -> DayRole.Between
    date == checkIn -> DayRole.CheckIn
    else -> DayRole.None
}

@Composable
private fun DayCell(
    date: LocalDate,
    role: DayRole,
    available: Boolean,
    modifier: Modifier,
    sweepPosition: Float?,
    sweep: () -> Float,
    onClick: () -> Unit,
) {
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    val unavailable = stringResource(Res.string.day_unavailable)
    val endpoint = role == DayRole.CheckIn || role == DayRole.CheckInOfRange || role == DayRole.CheckOut
    val description = formatter.date(date, withYear = true).let { if (available) it else "$it, $unavailable" }
    Box(
        modifier = modifier
            // The tap target is touch-min tall (48 dp); the disc and band inside stay DayHeight.
            .heightIn(min = SojournSizes.touchMin)
            .then(if (available) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics {
                contentDescription = description
                selected = endpoint
            },
        contentAlignment = Alignment.Center,
    ) {
        RangeTint(role, sweepPosition, sweep)
        if (endpoint) {
            Box(Modifier.size(SojournDateRangePickerDefaults.DayHeight).background(c.brand, SojournShapes.full))
        }
        BasicText(
            date.day.toString(),
            style = SojournTheme.type.label.copy(
                color = when {
                    endpoint -> c.onBrand
                    !available -> c.inkSubtle
                    role == DayRole.Between -> c.brandInk
                    else -> c.ink
                },
                fontWeight = dayWeight(endpoint, available),
                textDecoration = if (available) null else TextDecoration.LineThrough,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

private fun dayWeight(endpoint: Boolean, available: Boolean): FontWeight = when {
    endpoint -> FontWeight.SemiBold
    !available -> FontWeight.Normal
    else -> FontWeight.Medium
}

/** The `brandTint` band: full cells between, and from the disc's centre inward at either end (mirrors in RTL). */
@Composable
private fun BoxScope.RangeTint(role: DayRole, position: Float?, sweep: () -> Float) {
    val tint = SojournTheme.colors.brandTint
    if (role == DayRole.CheckIn || role == DayRole.None || position == null) return
    Box(
        Modifier
            .matchParentSize()
            .drawBehind {
                // The sweep is read while drawing, so the animation redraws cells without recomposing them.
                if (position > sweep()) return@drawBehind
                val half = size.width * SojournDateRangePickerDefaults.HALF
                val endHalf = if (layoutDirection == LayoutDirection.Ltr) half else 0f
                val startHalf = if (layoutDirection == LayoutDirection.Ltr) 0f else half
                // The band is as tall as the disc, centred in the taller tap target.
                val band = SojournDateRangePickerDefaults.DayHeight.toPx().coerceAtMost(size.height)
                val top = (size.height - band) / 2
                when (role) {
                    DayRole.Between -> drawRect(tint, Offset(0f, top), Size(size.width, band))
                    DayRole.CheckInOfRange -> drawRect(tint, Offset(endHalf, top), Size(half, band))
                    DayRole.CheckOut -> drawRect(tint, Offset(startHalf, top), Size(half, band))
                    DayRole.CheckIn, DayRole.None -> Unit
                }
            },
    )
}

/** Where [date] sits in [range], 0 at check-in and 1 at check-out; null outside it. */
private fun sweepPosition(date: LocalDate, range: DateRange): Float? {
    if (date < range.start || date > range.end) return null
    return range.start.daysUntil(date).toFloat() / range.nights
}

/**
 * The range tint sweep (motion.md): when a new range is chosen, the tint runs from check-in to check-out, `fast`
 * per week row and never longer than `base`. The first range shown and reduce motion appear at once.
 */
@Composable
private fun rememberRangeSweep(selection: DateRange?): () -> Float {
    val progress = remember { Animatable(1f) }
    var shown by remember { mutableStateOf(selection) }
    val reduceMotion = LocalReduceMotion.current
    LaunchedEffect(selection) {
        if (selection != null && selection != shown && !reduceMotion) {
            val rows = selection.nights / SojournDateRangePickerDefaults.DAYS_PER_WEEK + 1
            progress.snapTo(0f)
            progress.animateTo(
                1f,
                tween(minOf(rows * SojournMotion.fast, SojournMotion.base), easing = SojournMotion.standard),
            )
        }
        shown = selection
    }
    return { progress.value }
}
