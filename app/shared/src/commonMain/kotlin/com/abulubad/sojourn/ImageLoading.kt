package com.abulubad.sojourn

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade

/**
 * Coil's app-wide loader: catalogue photos come from the docs site over Ktor (ADR 0016), with Coil's memory and disk
 * caches, so a photo seen once still shows offline. Every slot renders its own placeholder and failed state.
 */
@Composable
internal fun ProvideImageLoader() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(enable = true)
            .build()
    }
}
