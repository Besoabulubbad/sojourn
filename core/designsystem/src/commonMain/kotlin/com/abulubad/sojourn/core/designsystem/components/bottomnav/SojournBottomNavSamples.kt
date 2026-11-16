package com.abulubad.sojourn.core.designsystem.components.bottomnav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.tab_bookings
import com.abulubad.sojourn.core.localization.resources.tab_explore
import com.abulubad.sojourn.core.localization.resources.tab_profile
import com.abulubad.sojourn.core.localization.resources.tab_saved
import org.jetbrains.compose.resources.stringResource

/** The BottomNav card's example: the four destinations with Explore active. */
object SojournBottomNavSamples {

    /** Stands in for the app's navigation keys, which live in `core:navigation`. */
    private enum class Tab { Explore, Saved, Bookings, Profile }

    @Composable
    fun Tabs(modifier: Modifier = Modifier) {
        var selected by remember { mutableStateOf(Tab.Explore) }
        val tabs = listOf(
            SojournNavTab(Tab.Explore, SojournIcon.Explore, stringResource(Res.string.tab_explore)),
            SojournNavTab(Tab.Saved, SojournIcon.Saved, stringResource(Res.string.tab_saved)),
            SojournNavTab(Tab.Bookings, SojournIcon.Bookings, stringResource(Res.string.tab_bookings)),
            SojournNavTab(Tab.Profile, SojournIcon.Profile, stringResource(Res.string.tab_profile)),
        )
        SojournBottomNav(tabs, selected = selected, onSelect = { selected = it }, modifier = modifier)
    }
}
