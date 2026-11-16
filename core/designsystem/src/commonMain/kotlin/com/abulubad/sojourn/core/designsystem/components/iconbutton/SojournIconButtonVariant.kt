package com.abulubad.sojourn.core.designsystem.components.iconbutton

/** The IconButton card's variants. */
enum class SojournIconButtonVariant {
    /** No container: top bars, rows. */
    Plain,

    /** `surface` disc with a `borderControl` outline: the filters button on results. */
    Outline,

    /** Its own `surface` disc with `shadow-2`, for use over photos and the map; never depends on what is behind it. */
    Floating,
}
