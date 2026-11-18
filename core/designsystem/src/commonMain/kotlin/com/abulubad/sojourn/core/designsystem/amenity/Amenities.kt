package com.abulubad.sojourn.core.designsystem.amenity

import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.amenity_air_conditioning
import com.abulubad.sojourn.core.localization.resources.amenity_balcony
import com.abulubad.sojourn.core.localization.resources.amenity_dishwasher
import com.abulubad.sojourn.core.localization.resources.amenity_heating
import com.abulubad.sojourn.core.localization.resources.amenity_kitchen
import com.abulubad.sojourn.core.localization.resources.amenity_lift
import com.abulubad.sojourn.core.localization.resources.amenity_parking
import com.abulubad.sojourn.core.localization.resources.amenity_pet_friendly
import com.abulubad.sojourn.core.localization.resources.amenity_tv
import com.abulubad.sojourn.core.localization.resources.amenity_washer
import com.abulubad.sojourn.core.localization.resources.amenity_wifi
import com.abulubad.sojourn.core.localization.resources.amenity_workspace
import com.abulubad.sojourn.core.model.Amenity
import org.jetbrains.compose.resources.StringResource

/** An amenity's glossary name (S-05: localized by id), the same on every surface that names it. */
val Amenity.label: StringResource
    get() = when (this) {
        Amenity.Wifi -> Res.string.amenity_wifi
        Amenity.Washer -> Res.string.amenity_washer
        Amenity.Workspace -> Res.string.amenity_workspace
        Amenity.Lift -> Res.string.amenity_lift
        Amenity.Kitchen -> Res.string.amenity_kitchen
        Amenity.AirConditioning -> Res.string.amenity_air_conditioning
        Amenity.Heating -> Res.string.amenity_heating
        Amenity.Parking -> Res.string.amenity_parking
        Amenity.Balcony -> Res.string.amenity_balcony
        Amenity.Dishwasher -> Res.string.amenity_dishwasher
        Amenity.Tv -> Res.string.amenity_tv
        Amenity.PetFriendly -> Res.string.amenity_pet_friendly
    }

/** An amenity's icon: one icon per meaning (N-02), so a chip and a fact row never disagree. */
val Amenity.icon: SojournIcon
    get() = when (this) {
        Amenity.Wifi -> SojournIcon.Wifi
        Amenity.Washer -> SojournIcon.Washer
        Amenity.Workspace -> SojournIcon.Workspace
        Amenity.Lift -> SojournIcon.Lift
        Amenity.Kitchen -> SojournIcon.Kitchen
        Amenity.AirConditioning -> SojournIcon.AirConditioning
        Amenity.Heating -> SojournIcon.Heating
        Amenity.Parking -> SojournIcon.Parking
        Amenity.Balcony -> SojournIcon.Balcony
        Amenity.Dishwasher -> SojournIcon.Dishwasher
        Amenity.Tv -> SojournIcon.Tv
        Amenity.PetFriendly -> SojournIcon.PetFriendly
    }
