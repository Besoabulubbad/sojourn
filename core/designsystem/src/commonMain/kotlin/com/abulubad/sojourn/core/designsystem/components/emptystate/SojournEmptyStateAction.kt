package com.abulubad.sojourn.core.designsystem.components.emptystate

/** The one action an empty state may offer; it must change the outcome ("Clear filters", not "OK"). */
class SojournEmptyStateAction(val label: String, val onClick: () -> Unit)
