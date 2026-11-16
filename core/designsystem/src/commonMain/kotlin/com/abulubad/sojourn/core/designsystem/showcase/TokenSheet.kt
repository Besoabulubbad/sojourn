package com.abulubad.sojourn.core.designsystem.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.SojournFormatter

/**
 * Renders one [section] of the token sheet, read from the active theme. Its screenshots are the Phase 2 exit test
 * and the showcase swatch sheet (`docs/showcase/`). Labels are token names, not user-facing copy, so they are
 * deliberately not string resources.
 */
@Composable
fun SojournTokenSheet(section: TokenSection, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(SojournTheme.colors.bg)
            .padding(SojournSpacing.space4),
    ) {
        Section(section.title) {
            when (section) {
                TokenSection.Color -> ColorSwatches()
                TokenSection.Type -> TypeScale()
                TokenSection.Spacing -> SpacingScale()
                TokenSection.Shape -> RadiusAndElevation()
                TokenSection.Icons -> IconGrid()
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3)) {
        BasicText(title, style = SojournTheme.type.titleMd.copy(color = SojournTheme.colors.ink))
        content()
    }
}

@Composable
private fun ColorSwatches() {
    val colors = SojournTheme.colors
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
    ) {
        colors.all.forEach { (name, color) ->
            Column(modifier = Modifier.width(SWATCH_WIDTH)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SojournSizes.touchMin)
                        .background(color, SojournShapes.sm)
                        .border(1.dp, colors.hairline, SojournShapes.sm),
                )
                BasicText(name, style = SojournTheme.type.caption.copy(color = colors.inkMuted))
            }
        }
    }
}

@Composable
private fun TypeScale() {
    val type = SojournTheme.type
    Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2)) {
        type.all.forEach { (name, style) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicText(
                    name,
                    modifier = Modifier.width(LABEL_WIDTH),
                    style = type.caption.copy(color = SojournTheme.colors.inkMuted),
                )
                BasicText(TYPE_SAMPLE, style = style.copy(color = SojournTheme.colors.ink))
            }
        }
    }
}

@Composable
private fun SpacingScale() {
    Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space1)) {
        SojournSpacing.all.forEach { (name, value) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicText(
                    name,
                    modifier = Modifier.width(LABEL_WIDTH),
                    style = SojournTheme.type.caption.copy(color = SojournTheme.colors.inkMuted),
                )
                Box(Modifier.size(width = value, height = SojournSpacing.space3).background(SojournTheme.colors.brand))
            }
        }
    }
}

@Composable
private fun RadiusAndElevation() {
    val colors = SojournTheme.colors
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
    ) {
        SojournShapes.all.forEach { (name, radius) ->
            Tile(name) {
                Box(
                    Modifier
                        .size(TILE)
                        .background(colors.brandTint, androidx.compose.foundation.shape.RoundedCornerShape(radius)),
                )
            }
        }
        SojournTheme.elevation.all.forEach { (name, shadow) ->
            Tile(name) {
                Box(
                    Modifier
                        .size(TILE)
                        .shadow(
                            shadow.elevation,
                            SojournShapes.md,
                            ambientColor = shadow.color,
                            spotColor = shadow.color,
                        )
                        .background(colors.surface, SojournShapes.md),
                )
            }
        }
    }
}

@Composable
private fun Tile(name: String, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        content()
        BasicText(name, style = SojournTheme.type.caption.copy(color = SojournTheme.colors.inkMuted))
    }
}

@Composable
private fun IconGrid() {
    CompositionLocalProvider(LocalContentColor provides SojournTheme.colors.ink) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        ) {
            SojournIcon.all.forEach { icon -> SojournIcon(icon, contentDescription = null) }
        }
    }
}

// The price is bidi-isolated exactly as SojournFormatter does (S-04), so it reads "£1,383" in both directions.
private val TYPE_SAMPLE = "Sojourn ${SojournFormatter.isolate("£1,383")} إقامة"
private val SWATCH_WIDTH: Dp = 88.dp
private val LABEL_WIDTH: Dp = 96.dp
private val TILE: Dp = 56.dp
