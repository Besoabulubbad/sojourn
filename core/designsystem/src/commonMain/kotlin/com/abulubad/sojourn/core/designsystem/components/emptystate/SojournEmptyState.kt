package com.abulubad.sojourn.core.designsystem.components.emptystate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.button.SojournButtonVariant
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing

/**
 * What a screen shows when there is nothing to list (EmptyState card). The [title] says what happened, the [body]
 * says what to do, and at most one [action] changes the outcome. The icon sits in a 64 dp `brandTint` disc.
 */
@Composable
fun SojournEmptyState(
    icon: SojournIcon,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: SojournEmptyStateAction? = null,
) {
    val c = SojournTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SojournSpacing.space6, vertical = SojournSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
    ) {
        Box(Modifier.size(ICON_DISC).background(c.brandTint, SojournShapes.full), contentAlignment = Alignment.Center) {
            SojournIcon(icon, contentDescription = null, tint = c.brandInk)
        }
        BasicText(
            text = title,
            modifier = Modifier.semantics { heading() },
            style = SojournTheme.type.titleMd.copy(color = c.ink, textAlign = TextAlign.Center),
        )
        BasicText(
            text = body,
            style = SojournTheme.type.body.copy(color = c.inkMuted, textAlign = TextAlign.Center),
        )
        if (action != null) {
            SojournButton(action.label, action.onClick, variant = SojournButtonVariant.Secondary)
        }
    }
}

/** The icon disc (EmptyState card). */
private val ICON_DISC: Dp = 64.dp
