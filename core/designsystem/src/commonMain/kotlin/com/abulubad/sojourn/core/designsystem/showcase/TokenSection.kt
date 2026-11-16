package com.abulubad.sojourn.core.designsystem.showcase

/** The sections of the token sheet, one per token family. [title] is the family's name in `tokens.json`. */
enum class TokenSection(val title: String) {
    Color("color"),
    Type("type"),
    Spacing("spacing"),
    Shape("radius · elevation"),
    Icons("icons"),
}
