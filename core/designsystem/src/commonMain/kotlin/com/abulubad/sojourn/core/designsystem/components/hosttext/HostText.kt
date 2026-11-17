package com.abulubad.sojourn.core.designsystem.components.hosttext

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.model.HostText as HostTextModel

/**
 * Renders text written by a host in its own language and direction (S-06): an Arabic title stays right-to-left in
 * the English UI and an English description stays left-to-right in the Arabic UI. The only way to show host text.
 * The translate action arrives with the Details screen.
 */
@Composable
fun HostText(
    text: HostTextModel,
    modifier: Modifier = Modifier,
    style: TextStyle = SojournTheme.type.body,
    maxLines: Int = Int.MAX_VALUE,
) {
    val direction = if (text.languageTag.substringBefore('-').lowercase() in RTL_LANGUAGES) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        BasicText(
            text = text.text,
            modifier = modifier,
            style = style.copy(
                color = SojournTheme.colors.ink,
                textDirection = if (direction == LayoutDirection.Rtl) TextDirection.Rtl else TextDirection.Ltr,
                textAlign = TextAlign.Start,
            ),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val RTL_LANGUAGES = setOf("ar", "he", "fa", "ur")
