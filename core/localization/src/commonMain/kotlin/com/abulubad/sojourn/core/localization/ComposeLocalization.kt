package com.abulubad.sojourn.core.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.list_pair
import com.abulubad.sojourn.core.localization.resources.list_series
import org.jetbrains.compose.resources.stringResource

/** The [SojournFormatter] for the current [LocalAppLocale]; composables format money, dates and counts through it. */
@Composable
fun rememberSojournFormatter(): SojournFormatter {
    val locale = LocalAppLocale.current
    return remember(locale) { SojournFormatter(locale) }
}

/**
 * Joins already-localized names into one phrase from resources (`utilities`, `cleaning fee and utilities`,
 * `city tax, cleaning fee and utilities`), so lists are never built by concatenation in code (S-02).
 */
@Composable
fun localizedList(items: List<String>): String = when (items.size) {
    0 -> ""
    1 -> items.single()
    else -> {
        val head = items.dropLast(1).reduce { acc, item -> stringResource(Res.string.list_series, acc, item) }
        stringResource(Res.string.list_pair, head, items.last())
    }
}
