package com.abulubad.sojourn.core.designsystem.icon

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.abulubad.sojourn.core.designsystem.resources.Res
import com.abulubad.sojourn.core.designsystem.resources.ic_arrow_left
import com.abulubad.sojourn.core.designsystem.resources.ic_arrow_right
import com.abulubad.sojourn.core.designsystem.resources.ic_arrow_up_down
import com.abulubad.sojourn.core.designsystem.resources.ic_bath
import com.abulubad.sojourn.core.designsystem.resources.ic_bed_double
import com.abulubad.sojourn.core.designsystem.resources.ic_bookmark
import com.abulubad.sojourn.core.designsystem.resources.ic_briefcase
import com.abulubad.sojourn.core.designsystem.resources.ic_building_2
import com.abulubad.sojourn.core.designsystem.resources.ic_calendar_days
import com.abulubad.sojourn.core.designsystem.resources.ic_check
import com.abulubad.sojourn.core.designsystem.resources.ic_chevron_down
import com.abulubad.sojourn.core.designsystem.resources.ic_chevron_left
import com.abulubad.sojourn.core.designsystem.resources.ic_chevron_right
import com.abulubad.sojourn.core.designsystem.resources.ic_chevron_up
import com.abulubad.sojourn.core.designsystem.resources.ic_circle_alert
import com.abulubad.sojourn.core.designsystem.resources.ic_circle_check
import com.abulubad.sojourn.core.designsystem.resources.ic_clock
import com.abulubad.sojourn.core.designsystem.resources.ic_compass
import com.abulubad.sojourn.core.designsystem.resources.ic_copy
import com.abulubad.sojourn.core.designsystem.resources.ic_credit_card
import com.abulubad.sojourn.core.designsystem.resources.ic_door_open
import com.abulubad.sojourn.core.designsystem.resources.ic_external_link
import com.abulubad.sojourn.core.designsystem.resources.ic_footprints
import com.abulubad.sojourn.core.designsystem.resources.ic_gift
import com.abulubad.sojourn.core.designsystem.resources.ic_heart
import com.abulubad.sojourn.core.designsystem.resources.ic_heart_filled
import com.abulubad.sojourn.core.designsystem.resources.ic_house
import com.abulubad.sojourn.core.designsystem.resources.ic_image_off
import com.abulubad.sojourn.core.designsystem.resources.ic_info
import com.abulubad.sojourn.core.designsystem.resources.ic_key_round
import com.abulubad.sojourn.core.designsystem.resources.ic_languages
import com.abulubad.sojourn.core.designsystem.resources.ic_laptop
import com.abulubad.sojourn.core.designsystem.resources.ic_list
import com.abulubad.sojourn.core.designsystem.resources.ic_map
import com.abulubad.sojourn.core.designsystem.resources.ic_map_pin
import com.abulubad.sojourn.core.designsystem.resources.ic_message_circle
import com.abulubad.sojourn.core.designsystem.resources.ic_minus
import com.abulubad.sojourn.core.designsystem.resources.ic_monitor
import com.abulubad.sojourn.core.designsystem.resources.ic_moon
import com.abulubad.sojourn.core.designsystem.resources.ic_phone
import com.abulubad.sojourn.core.designsystem.resources.ic_plus
import com.abulubad.sojourn.core.designsystem.resources.ic_rotate_cw
import com.abulubad.sojourn.core.designsystem.resources.ic_ruler
import com.abulubad.sojourn.core.designsystem.resources.ic_search
import com.abulubad.sojourn.core.designsystem.resources.ic_search_x
import com.abulubad.sojourn.core.designsystem.resources.ic_settings
import com.abulubad.sojourn.core.designsystem.resources.ic_share
import com.abulubad.sojourn.core.designsystem.resources.ic_shield_check
import com.abulubad.sojourn.core.designsystem.resources.ic_sliders_horizontal
import com.abulubad.sojourn.core.designsystem.resources.ic_sparkles
import com.abulubad.sojourn.core.designsystem.resources.ic_star
import com.abulubad.sojourn.core.designsystem.resources.ic_star_filled
import com.abulubad.sojourn.core.designsystem.resources.ic_sun
import com.abulubad.sojourn.core.designsystem.resources.ic_train_front
import com.abulubad.sojourn.core.designsystem.resources.ic_trash_2
import com.abulubad.sojourn.core.designsystem.resources.ic_user
import com.abulubad.sojourn.core.designsystem.resources.ic_users
import com.abulubad.sojourn.core.designsystem.resources.ic_washing_machine
import com.abulubad.sojourn.core.designsystem.resources.ic_wifi
import com.abulubad.sojourn.core.designsystem.resources.ic_wifi_off
import com.abulubad.sojourn.core.designsystem.resources.ic_x
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * The icon set (Icons card): Lucide at 24dp with a 1.75 stroke, one icon per meaning (N-02). Components accept
 * only a [SojournIcon], never a raw painter, so call sites cannot bring their own glyphs or flip them (L-05):
 * [autoMirror] is true for Back and the chevrons only, and the mirroring happens inside [SojournIcon].
 */
sealed class SojournIcon(internal val resource: DrawableResource, val autoMirror: Boolean = false) {
    // Navigation
    data object Back : SojournIcon(Res.drawable.ic_arrow_left, autoMirror = true)
    data object Forward : SojournIcon(Res.drawable.ic_arrow_right, autoMirror = true)
    data object ChevronStart : SojournIcon(Res.drawable.ic_chevron_left, autoMirror = true)
    data object ChevronEnd : SojournIcon(Res.drawable.ic_chevron_right, autoMirror = true)
    data object ChevronDown : SojournIcon(Res.drawable.ic_chevron_down)
    data object ChevronUp : SojournIcon(Res.drawable.ic_chevron_up)
    data object Close : SojournIcon(Res.drawable.ic_x)
    data object ExternalLink : SojournIcon(Res.drawable.ic_external_link)

    // Tabs and destinations
    data object Explore : SojournIcon(Res.drawable.ic_compass)
    data object Saved : SojournIcon(Res.drawable.ic_heart)

    /** The saved heart once a stay is saved: filled, tinted `save` (IconButton card). */
    data object SavedFilled : SojournIcon(Res.drawable.ic_heart_filled)
    data object Bookings : SojournIcon(Res.drawable.ic_briefcase)
    data object Profile : SojournIcon(Res.drawable.ic_user)
    data object Home : SojournIcon(Res.drawable.ic_house)

    // Search and results
    data object Search : SojournIcon(Res.drawable.ic_search)
    data object Filters : SojournIcon(Res.drawable.ic_sliders_horizontal)
    data object Map : SojournIcon(Res.drawable.ic_map)
    data object MapPin : SojournIcon(Res.drawable.ic_map_pin)
    data object List : SojournIcon(Res.drawable.ic_list)
    data object Calendar : SojournIcon(Res.drawable.ic_calendar_days)
    data object Guests : SojournIcon(Res.drawable.ic_users)
    data object City : SojournIcon(Res.drawable.ic_building_2)

    // Facts and amenities
    data object Bed : SojournIcon(Res.drawable.ic_bed_double)
    data object Bath : SojournIcon(Res.drawable.ic_bath)
    data object Size : SojournIcon(Res.drawable.ic_ruler)
    data object Wifi : SojournIcon(Res.drawable.ic_wifi)
    data object Washer : SojournIcon(Res.drawable.ic_washing_machine)
    data object Workspace : SojournIcon(Res.drawable.ic_laptop)
    data object Lift : SojournIcon(Res.drawable.ic_arrow_up_down)
    data object Walk : SojournIcon(Res.drawable.ic_footprints)
    data object Transit : SojournIcon(Res.drawable.ic_train_front)
    data object Star : SojournIcon(Res.drawable.ic_star)

    /** The rating star (Rating card): filled, tinted `accentInk`. */
    data object StarFilled : SojournIcon(Res.drawable.ic_star_filled)
    data object Sparkles : SojournIcon(Res.drawable.ic_sparkles)

    // Policies and booking
    data object Key : SojournIcon(Res.drawable.ic_key_round)
    data object Clock : SojournIcon(Res.drawable.ic_clock)
    data object Deposit : SojournIcon(Res.drawable.ic_shield_check)
    data object DoorOpen : SojournIcon(Res.drawable.ic_door_open)
    data object Payment : SojournIcon(Res.drawable.ic_credit_card)
    data object Rewards : SojournIcon(Res.drawable.ic_gift)
    data object Bookmark : SojournIcon(Res.drawable.ic_bookmark)

    // Feedback and actions
    data object Check : SojournIcon(Res.drawable.ic_check)
    data object CheckCircle : SojournIcon(Res.drawable.ic_circle_check)
    data object Info : SojournIcon(Res.drawable.ic_info)
    data object Alert : SojournIcon(Res.drawable.ic_circle_alert)
    data object Offline : SojournIcon(Res.drawable.ic_wifi_off)
    data object Retry : SojournIcon(Res.drawable.ic_rotate_cw)
    data object ImageFailed : SojournIcon(Res.drawable.ic_image_off)

    /** Nothing matches a search (EmptyState card). */
    data object NoResults : SojournIcon(Res.drawable.ic_search_x)
    data object Plus : SojournIcon(Res.drawable.ic_plus)
    data object Minus : SojournIcon(Res.drawable.ic_minus)
    data object Share : SojournIcon(Res.drawable.ic_share)
    data object Copy : SojournIcon(Res.drawable.ic_copy)
    data object Delete : SojournIcon(Res.drawable.ic_trash_2)
    data object Phone : SojournIcon(Res.drawable.ic_phone)
    data object Message : SojournIcon(Res.drawable.ic_message_circle)

    // Profile
    data object Language : SojournIcon(Res.drawable.ic_languages)
    data object ThemeLight : SojournIcon(Res.drawable.ic_sun)
    data object ThemeDark : SojournIcon(Res.drawable.ic_moon)
    data object ThemeSystem : SojournIcon(Res.drawable.ic_monitor)
    data object Settings : SojournIcon(Res.drawable.ic_settings)

    companion object {
        /**
         * Every icon, for the showcase sheet and tests. Lazy on purpose: the nested objects extend this class, so an
         * eager list built during class initialisation would hold `null` for whichever icon was touched first.
         */
        val all: kotlin.collections.List<SojournIcon> by lazy {
            listOf(
                Back, Forward, ChevronStart, ChevronEnd, ChevronDown, ChevronUp, Close, ExternalLink,
                Explore, Saved, SavedFilled, Bookings, Profile, Home,
                Search, Filters, Map, MapPin, List, Calendar, Guests, City,
                Bed, Bath, Size, Wifi, Washer, Workspace, Lift, Walk, Transit, Star, StarFilled, Sparkles,
                Key, Clock, Deposit, DoorOpen, Payment, Rewards, Bookmark,
                Check, CheckCircle, Info, Alert, Offline, Retry, ImageFailed, NoResults,
                Plus, Minus, Share, Copy, Delete, Phone, Message,
                Language, ThemeLight, ThemeDark, ThemeSystem, Settings,
            )
        }
    }
}

/**
 * Draws a [SojournIcon] at [size] in [tint]. Directional icons mirror in right-to-left layouts here and nowhere
 * else (L-05). [contentDescription] is null only when a label next to the icon already names it; icon-only
 * controls pass a localized description (C-05).
 */
@Composable
fun SojournIcon(
    icon: SojournIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = SojournSizes.icon,
) {
    val mirrored = icon.autoMirror && LocalLayoutDirection.current == LayoutDirection.Rtl
    Icon(
        painter = painterResource(icon.resource),
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .then(if (mirrored) Modifier.graphicsLayer { scaleX = -1f } else Modifier),
        tint = tint,
    )
}
