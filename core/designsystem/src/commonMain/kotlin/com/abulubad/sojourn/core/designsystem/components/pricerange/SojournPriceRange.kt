package com.abulubad.sojourn.core.designsystem.components.pricerange

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RangeSlider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.internal.ShareRow
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.SojournFormatter
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.nights
import com.abulubad.sojourn.core.localization.resources.price_range_max
import com.abulubad.sojourn.core.localization.resources.price_range_min
import com.abulubad.sojourn.core.localization.resources.price_range_month
import com.abulubad.sojourn.core.localization.resources.price_range_night
import com.abulubad.sojourn.core.localization.resources.price_range_total
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.MoneyRange
import com.abulubad.sojourn.core.model.PriceBasis
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToLong

/**
 * A two-thumb price range with a histogram and two editable fields (RangeSlider card).
 *
 * - [basis] is required and must equal the basis the cards on screen use (P-04); it sets the label: "Total price
 *   for 15 nights" (pass [nights] with [PriceBasis.Stay]), "Price per month" or "Price per night".
 * - [histogram] holds stay counts in equal buckets across [bounds]; bars inside the range are `brand`.
 * - Thumbs are 28 dp discs on a 48 dp tall slider. The fields take exact amounts and are the accessible path;
 *   an entry outside the bounds or past the other end is clamped.
 * - In RTL the low value is on the right; the component reads the layout direction, callers pass nothing.
 */
@Composable
fun SojournPriceRange(
    range: MoneyRange,
    bounds: MoneyRange,
    histogram: List<Int>,
    basis: PriceBasis,
    onChange: (MoneyRange) -> Unit,
    modifier: Modifier = Modifier,
    nights: Int? = null,
) {
    require(basis != PriceBasis.Stay || nights != null) { "A total-price range names its nights" }
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    val currency = bounds.min.currency
    val label = when (basis) {
        PriceBasis.Stay -> {
            val count = nights ?: 0
            stringResource(
                Res.string.price_range_total,
                pluralStringResource(Res.plurals.nights, count, formatter.count(count)),
            )
        }
        PriceBasis.Month -> stringResource(Res.string.price_range_month)
        PriceBasis.Night -> stringResource(Res.string.price_range_night)
    }
    val low = bounds.min.major.toFloat()
    val high = bounds.max.major.toFloat()
    val span = (high - low).coerceAtLeast(1f)
    val startFraction = (range.min.major - low) / span
    val endFraction = (range.max.major - low) / span

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2)) {
        BasicText(label, style = SojournTheme.type.label.copy(color = c.ink))
        Histogram(histogram, startFraction, endFraction)
        PriceSlider(range, low..high, startFraction, endFraction, onChange)
        ShareRow(gap = SojournSpacing.space3) {
            PriceField(
                label = stringResource(Res.string.price_range_min),
                amount = range.min,
                formatter = formatter,
                onCommit = { entered ->
                    val clamped = entered.coerceIn(bounds.min.major, range.max.major)
                    onChange(MoneyRange(Money.of(clamped, currency), range.max))
                },
            )
            PriceField(
                label = stringResource(Res.string.price_range_max),
                amount = range.max,
                formatter = formatter,
                onCommit = { entered ->
                    val clamped = entered.coerceIn(range.min.major, bounds.max.major)
                    onChange(MoneyRange(range.min, Money.of(clamped, currency)))
                },
            )
        }
    }
}

/** Material's range slider (gestures, keyboard, RTL, semantics) with the card's thumbs and track. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriceSlider(
    range: MoneyRange,
    bounds: ClosedFloatingPointRange<Float>,
    startFraction: Float,
    endFraction: Float,
    onChange: (MoneyRange) -> Unit,
) {
    val currency = range.min.currency
    RangeSlider(
        value = range.min.major.toFloat()..range.max.major.toFloat(),
        onValueChange = { value ->
            onChange(
                MoneyRange(
                    Money.of(value.start.roundToLong(), currency),
                    Money.of(value.endInclusive.roundToLong(), currency),
                ),
            )
        },
        valueRange = bounds,
        startThumb = { Thumb() },
        endThumb = { Thumb() },
        track = { Track(startFraction, endFraction) },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun Histogram(counts: List<Int>, startFraction: Float, endFraction: Float) {
    if (counts.isEmpty()) return
    val c = SojournTheme.colors
    val tallest = counts.max().coerceAtLeast(1)
    val barShape = RoundedCornerShape(
        topStart = SojournPriceRangeDefaults.BarRadius,
        topEnd = SojournPriceRangeDefaults.BarRadius,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SojournPriceRangeDefaults.HistogramHeight)
            .padding(horizontal = SojournPriceRangeDefaults.ThumbSize / 2)
            .clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(SojournPriceRangeDefaults.BarGap),
        verticalAlignment = Alignment.Bottom,
    ) {
        val minFraction = SojournPriceRangeDefaults.MIN_BAR_FRACTION
        counts.forEachIndexed { index, count ->
            val bucketStart = index.toFloat() / counts.size
            val bucketEnd = (index + 1).toFloat() / counts.size
            val inside = bucketEnd > startFraction && bucketStart < endFraction
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight((count.toFloat() / tallest).coerceAtLeast(minFraction))
                    .background(if (inside) c.brand else c.hairline, barShape),
            )
        }
    }
}

@Composable
private fun Thumb() {
    val c = SojournTheme.colors
    val shadow = SojournTheme.elevation.level2
    Box(
        Modifier
            .size(SojournPriceRangeDefaults.ThumbSize)
            .shadow(shadow.elevation, SojournShapes.full, ambientColor = shadow.color, spotColor = shadow.color)
            .background(c.surface, SojournShapes.full)
            .border(1.dp, c.borderControl, SojournShapes.full),
    )
}

/** The hairline track with the selected stretch in `brand`; drawn by hand, so it mirrors itself in RTL. */
@Composable
private fun Track(startFraction: Float, endFraction: Float) {
    val c = SojournTheme.colors
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Canvas(Modifier.fillMaxWidth().height(SojournPriceRangeDefaults.TrackHeight)) {
        val y = size.height / 2
        val stroke = size.height
        fun x(fraction: Float) = if (rtl) size.width * (1 - fraction) else size.width * fraction
        drawLine(c.hairline, Offset(0f, y), Offset(size.width, y), stroke, StrokeCap.Round)
        drawLine(c.brand, Offset(x(startFraction), y), Offset(x(endFraction), y), stroke, StrokeCap.Round)
    }
}

/**
 * An exact amount: shows the grouped amount (`2,400`), edits plain digits, commits on Done or when focus leaves.
 * Numbers run left to right in both languages; the symbol sits before (`£900`) or after (`900 £`) per language.
 */
@Composable
private fun PriceField(
    label: String,
    amount: Money,
    formatter: SojournFormatter,
    onCommit: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = SojournTheme.colors
    var focused by remember { mutableStateOf(false) }
    var text by remember(amount) { mutableStateOf(amount.major.toString()) }
    val commit = { text.toLongOrNull()?.let(onCommit) ?: Unit }
    val valueStyle = SojournTheme.type.body.copy(color = c.ink, fontWeight = FontWeight.SemiBold)
    val symbol = formatter.currencySymbol(amount.currency)
    Column(
        modifier
            .background(c.surface, SojournShapes.md)
            .border(1.dp, if (focused) c.focus else c.borderControl, SojournShapes.md)
            .padding(horizontal = SojournSpacing.space3, vertical = SojournPriceRangeDefaults.FieldVerticalPadding),
    ) {
        BasicText(label, style = SojournTheme.type.labelSm.copy(color = c.inkMuted))
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space1),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (formatter.currencyLeads) BasicText(symbol, style = valueStyle)
                BasicTextField(
                    value = if (focused) text else SojournFormatter.group(amount.major),
                    onValueChange = { entered -> text = entered.filter(Char::isDigit) },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { state ->
                            if (focused && !state.isFocused) commit()
                            focused = state.isFocused
                        }
                        .semantics { contentDescription = label },
                    textStyle = valueStyle.copy(
                        textAlign = if (formatter.currencyLeads) TextAlign.Start else TextAlign.End,
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { commit() }),
                    cursorBrush = SolidColor(c.brand),
                )
                if (!formatter.currencyLeads) BasicText(symbol, style = valueStyle)
            }
        }
    }
}
