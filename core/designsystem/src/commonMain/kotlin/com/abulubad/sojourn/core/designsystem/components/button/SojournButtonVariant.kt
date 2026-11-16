package com.abulubad.sojourn.core.designsystem.components.button

/** The Button card's variants. One `Primary` per screen; review enforces it, the type cannot. */
enum class SojournButtonVariant {
    /** The screen's main action: `brand` fill, `onBrand` text. */
    Primary,

    /** A secondary action: `surface` fill, `ink` text, `borderControl` outline. */
    Secondary,

    /** A low-emphasis action inline with content: no fill, `ink` text. */
    Ghost,

    /** A destructive action: `dangerFill`, `onDanger` text. */
    Danger,
}
