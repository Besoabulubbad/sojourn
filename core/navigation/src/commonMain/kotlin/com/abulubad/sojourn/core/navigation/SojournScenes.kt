package com.abulubad.sojourn.core.navigation

import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.metadata

/**
 * How an entry is presented, as `NavEntry` metadata a feature sets in its `entry<Key>(metadata = …)` and
 * `app:shared`'s scene strategy reads (ADR 0005).
 */
object SojournScenes {

    /**
     * Marks an entry that draws over the entries beneath it, which stay composed and visible: a modal
     * `SojournSheet` such as the filter sheet over Results. The entry draws its own scrim and sheet.
     */
    fun overlay(): Map<String, Any> = metadata { put(OverlayKey, true) }

    /** The metadata key [overlay] sets. */
    data object OverlayKey : NavMetadataKey<Boolean>
}
