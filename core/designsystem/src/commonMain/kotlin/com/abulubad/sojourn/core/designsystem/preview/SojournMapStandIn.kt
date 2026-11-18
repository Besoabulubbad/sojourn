package com.abulubad.sojourn.core.designsystem.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.abulubad.sojourn.core.designsystem.components.map.LocalSojournMapStandIn

/**
 * Draws every `SojournMap` inside [content] as its stand-in: the `mapLand` colour, the overlays where the map would
 * put them. A live map cannot render in screenshot tests (ADR 0017); previews and samples use this.
 */
@Composable
fun WithSojournMapStandIn(content: @Composable () -> Unit) =
    CompositionLocalProvider(LocalSojournMapStandIn provides true, content = content)
