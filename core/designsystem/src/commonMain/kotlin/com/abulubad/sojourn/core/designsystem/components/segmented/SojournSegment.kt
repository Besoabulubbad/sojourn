package com.abulubad.sojourn.core.designsystem.components.segmented

/**
 * One option of a [SojournSegmentedControl]. [label] comes from resources; a value like "3+" comes from
 * `SojournFormatter.plus`, which isolates it so it never reads "+3" in Arabic.
 */
class SojournSegment<T>(val value: T, val label: String)
