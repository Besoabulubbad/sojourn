package com.abulubad.sojourn.core.designsystem.components.pricepin

/** A price pin's look on the map (PricePin card). */
enum class SojournPinState {
    /** Not opened yet: `surface` with a hairline. */
    Default,

    /** Opened before: drops to `surfaceSunken` so new stays stand out. */
    Seen,

    /** The stay shown in the card below: inverts to `ink` and grows to 1.12. */
    Selected,
}
