package com.abulubad.sojourn

import androidx.compose.ui.window.ComposeUIViewController

/** Entry point for the iOS app: `ContentView.swift` wraps this controller. The name is fixed by the Xcode project. */
@Suppress("FunctionNaming", "ktlint:standard:function-naming")
fun MainViewController() = ComposeUIViewController { App() }
