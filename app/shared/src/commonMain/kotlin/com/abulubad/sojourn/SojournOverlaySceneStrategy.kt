package com.abulubad.sojourn

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.get
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.abulubad.sojourn.core.navigation.SojournScenes

/**
 * Shows an entry marked [SojournScenes.overlay] above the entries beneath it, after the official Navigation 3
 * bottom-sheet recipe (an `OverlayScene`), without Material's `ModalBottomSheet`: the entry draws its own
 * `SojournSheet`, scrim and motion. The entries beneath stay composed and visible, so Results shows through the
 * filter sheet's scrim. System back reaches the sheet's own handler, which animates it closed before it pops.
 */
internal class SojournOverlaySceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val last = entries.lastOrNull() ?: return null
        val below = entries.dropLast(1)
        // An overlay needs something to draw over; alone on a stack it is an ordinary screen.
        if (last.metadata[SojournScenes.OverlayKey] != true || below.isEmpty()) return null
        return OverlayEntryScene(last.contentKey, last, below)
    }
}

private data class OverlayEntryScene<T : Any>(
    override val key: Any,
    private val entry: NavEntry<T>,
    override val previousEntries: List<NavEntry<T>>,
) : OverlayScene<T> {
    override val overlaidEntries: List<NavEntry<T>> = previousEntries
    override val entries: List<NavEntry<T>> = listOf(entry)
    override val content: @Composable () -> Unit = { entry.Content() }
}
