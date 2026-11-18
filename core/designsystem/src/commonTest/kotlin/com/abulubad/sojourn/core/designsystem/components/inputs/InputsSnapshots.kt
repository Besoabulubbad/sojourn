package com.abulubad.sojourn.core.designsystem.components.inputs

import com.abulubad.sojourn.core.designsystem.components.daterange.SojournDateRangePickerSamples
import com.abulubad.sojourn.core.designsystem.components.divider.SojournDividerSamples
import com.abulubad.sojourn.core.designsystem.components.pricerange.SojournPriceRangeSamples
import com.abulubad.sojourn.core.designsystem.components.searchbar.SojournSearchBarSamples
import com.abulubad.sojourn.core.designsystem.components.segmented.SojournSegmentedControlSamples
import com.abulubad.sojourn.core.designsystem.components.stepper.SojournStepperSamples
import com.abulubad.sojourn.core.testing.SnapshotCase

/** The Inputs group: SearchBar, SegmentedControl, Stepper, DateRangePicker, price RangeSlider, and the divider between
 * filter sections; each is its default. */
val inputsSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("search_bar_forms", fullMatrix = true) { SojournSearchBarSamples.Forms() },
    SnapshotCase("segmented_control_rows", fullMatrix = true) { SojournSegmentedControlSamples.Rows() },
    SnapshotCase("stepper_guests", fullMatrix = true) { SojournStepperSamples.Guests() },
    SnapshotCase("date_range_march", fullMatrix = true) { SojournDateRangePickerSamples.March() },
    SnapshotCase("price_range_total", fullMatrix = true) { SojournPriceRangeSamples.Total() },
    SnapshotCase("divider_sections") { SojournDividerSamples.Sections() },
)
