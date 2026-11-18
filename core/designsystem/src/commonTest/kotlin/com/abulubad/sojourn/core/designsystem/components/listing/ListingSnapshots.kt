package com.abulubad.sojourn.core.designsystem.components.listing

import com.abulubad.sojourn.core.designsystem.components.citycard.SojournCityCardSamples
import com.abulubad.sojourn.core.designsystem.components.continuerow.SojournContinueRowSamples
import com.abulubad.sojourn.core.designsystem.components.gallery.SojournGallerySamples
import com.abulubad.sojourn.core.designsystem.components.keyfacts.SojournKeyFactsSamples
import com.abulubad.sojourn.core.designsystem.components.policy.SojournPolicyRowSamples
import com.abulubad.sojourn.core.designsystem.components.pricepin.SojournPricePinSamples
import com.abulubad.sojourn.core.designsystem.components.staycard.SojournStayCardSamples
import com.abulubad.sojourn.core.testing.SnapshotCase

/**
 * The Listing group: KeyFacts, PolicyRow, PricePin, CityCard, ContinueRow, StayCard, Gallery; each default in the full
 * matrix.
 */
val listingSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("key_facts", fullMatrix = true) { SojournKeyFactsSamples.StudioAndFlat() },
    SnapshotCase("policy_rows", fullMatrix = true) { SojournPolicyRowSamples.Policies() },
    SnapshotCase("price_pins", fullMatrix = true) { SojournPricePinSamples.States() },
    SnapshotCase("city_cards", fullMatrix = true) { SojournCityCardSamples.Cities() },
    SnapshotCase("city_rows", fullMatrix = true) { SojournCityCardSamples.Rows() },
    SnapshotCase("continue_rows", fullMatrix = true) { SojournContinueRowSamples.Searches() },
    SnapshotCase("stay_card_full", fullMatrix = true) { SojournStayCardSamples.Full() },
    SnapshotCase("stay_card_compact") { SojournStayCardSamples.Compact() },
    SnapshotCase("stay_card_stale") { SojournStayCardSamples.Stale() },
    SnapshotCase("gallery_states", fullMatrix = true) { SojournGallerySamples.States() },
)
