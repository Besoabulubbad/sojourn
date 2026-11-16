package com.abulubad.sojourn.core.designsystem.components.bottomnav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing

/**
 * The app's four destinations (BottomNav card): Explore, Saved, Bookings, Profile.
 *
 * - Order follows reading direction: the first tab is at the start, so Explore sits on the right in Arabic.
 * - The active tab changes three things at once — the `brandTint` pill, the icon tint and the label weight —
 *   never colour alone. Each tab is announced as a tab with its selected state.
 * - The bar owns the navigation-bar inset (I-01). It is hidden on full-screen flows and on Details, where the
 *   booking bar takes its place.
 * - Labels stay on one line and never break a word: with large text they grow as far as the tab's width
 *   allows, and never shrink below their base size.
 *
 * ```
 * SojournBottomNav(tabs, selected = current, onSelect = { onIntent(SelectTab(it)) })
 * ```
 */
@Composable
fun <T> SojournBottomNav(
    tabs: List<SojournNavTab<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(tabs.size in SojournBottomNavDefaults.TabCount) { "A bottom nav has two to four tabs." }
    val c = SojournTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .background(c.surface)
            .drawBehind { drawRect(c.hairline, size = Size(size.width, 1.dp.toPx())) }
            .windowInsetsPadding(WindowInsets.navigationBars)
            .defaultMinSize(minHeight = SojournSizes.barBottom)
            .height(IntrinsicSize.Min)
            .selectableGroup(),
    ) {
        tabs.forEach { tab ->
            NavItem(tab, selected = tab.value == selected, onClick = { onSelect(tab.value) })
        }
    }
}

@Composable
private fun <T> RowScope.NavItem(
    tab: SojournNavTab<T>,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val c = SojournTheme.colors
    Column(
        Modifier
            .weight(1f)
            .fillMaxHeight()
            .selectable(
                selected = selected,
                interactionSource = null,
                indication = ripple(bounded = false),
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(vertical = SojournSpacing.space2, horizontal = SojournSpacing.space1),
        verticalArrangement = Arrangement.spacedBy(SojournBottomNavDefaults.LabelGap, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(SojournBottomNavDefaults.PillWidth, SojournBottomNavDefaults.PillHeight)
                .background(if (selected) c.brandTint else c.surface, SojournShapes.full),
            contentAlignment = Alignment.Center,
        ) {
            SojournIcon(
                tab.icon,
                contentDescription = null,
                tint = if (selected) c.brandInk else c.inkMuted,
                size = SojournSizes.iconSm,
            )
        }
        val style = SojournTheme.type.labelSm
        // The label grows with the user's text size as far as the tab allows, on one line, and never below its
        // base size: four tabs cannot fit "المحفوظات" at 200 %, and a label must not break mid-word.
        val baseSize = with(LocalDensity.current) { style.fontSize.value.dp.toSp() }
        BasicText(
            text = tab.label,
            style = style.copy(
                color = if (selected) c.ink else c.inkMuted,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
            softWrap = false,
            autoSize = TextAutoSize.StepBased(minFontSize = baseSize, maxFontSize = style.fontSize),
        )
    }
}
