package com.abulubad.sojourn.core.network

import com.abulubad.sojourn.core.network.dto.CatalogDto
import kotlinx.serialization.json.Json

/** One JSON configuration for the catalogue: unknown keys are tolerated so older apps read newer catalogues. */
internal object CatalogJson {
    val format = Json { ignoreUnknownKeys = true }

    fun parse(text: String): CatalogDto = format.decodeFromString(CatalogDto.serializer(), text)
}
