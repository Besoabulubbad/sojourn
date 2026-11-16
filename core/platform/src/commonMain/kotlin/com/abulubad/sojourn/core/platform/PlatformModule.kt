package com.abulubad.sojourn.core.platform

import org.koin.core.module.Module

/**
 * Koin module: the platform implementations of [Haptics], [ShareSheet], [MapsLauncher], [AccessibilitySettings]
 * and [PaymentSheet].
 */
expect val platformModule: Module
