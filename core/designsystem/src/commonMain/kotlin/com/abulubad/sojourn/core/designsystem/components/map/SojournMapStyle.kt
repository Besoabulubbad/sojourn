package com.abulubad.sojourn.core.designsystem.components.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.abulubad.sojourn.core.designsystem.tokens.SojournColors
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * The map's style, built from the map tokens (ScreenMap card: the map follows the app theme and never contrasts with
 * it). Land, water, parks and roads are `mapLand`, `mapWater`, `mapPark` and `mapRoad`; place and water names are
 * `inkMuted` on a `mapLand` halo, in the app's [language] where OpenStreetMap has the name, else as written locally.
 * Everything else a general-purpose map draws (buildings, rail, borders, shields, POIs) is left out, so the price
 * pins are what stands out. Tiles: OpenMapTiles from OpenFreeMap (ADR 0017).
 */
internal fun sojournMapStyle(colors: SojournColors, language: String): String = buildJsonObject {
    put("version", STYLE_VERSION)
    put("name", "Sojourn")
    putJsonObject("sources") {
        putJsonObject(SOURCE) {
            put("type", "vector")
            put("url", SojournMapDefaults.TILES_URL)
        }
    }
    put("glyphs", SojournMapDefaults.GLYPHS_URL)
    putJsonArray("layers") {
        addJsonObject {
            put("id", "land")
            put("type", "background")
            putJsonObject("paint") { put("background-color", colors.mapLand.hex) }
        }
        addJsonObject { fill("park", "park", colors.mapPark) }
        addJsonObject {
            fill("green", "landcover", colors.mapPark)
            put("filter", json("match", json("get", "class"), json("grass", "wood"), true, false))
        }
        addJsonObject { fill("water", "water", colors.mapWater) }
        addJsonObject {
            line("waterway", "waterway", colors.mapWater, width = ramp(MIN_ROAD_ZOOM, THIN, WATERWAY_WIDTH))
        }
        addJsonObject {
            line("road-minor", "transportation", colors.mapRoad, width = ramp(MIN_ROAD_ZOOM, THIN, MINOR_ROAD_WIDTH))
            put("filter", classIn("minor", "service"))
            put("minzoom", MIN_ROAD_ZOOM)
        }
        addJsonObject {
            line("road-major", "transportation", colors.mapRoad, width = ramp(MAJOR_ROAD_ZOOM, 1.0, MAJOR_ROAD_WIDTH))
            put("filter", classIn("primary", "secondary", "tertiary", "trunk", "motorway"))
        }
        addJsonObject {
            label("water-name", "water_name", colors, language, size = 12.0)
        }
        addJsonObject {
            label("place-name", "place", colors, language, size = 13.0)
            put("filter", classIn("city", "town", "suburb", "quarter", "neighbourhood"))
        }
    }
}.toString()

private fun JsonObjectBuilder.fill(id: String, layer: String, color: Color) {
    put("id", id)
    put("type", "fill")
    put("source", SOURCE)
    put("source-layer", layer)
    putJsonObject("paint") { put("fill-color", color.hex) }
}

private fun JsonObjectBuilder.line(id: String, layer: String, color: Color, width: JsonElement) {
    put("id", id)
    put("type", "line")
    put("source", SOURCE)
    put("source-layer", layer)
    putJsonObject("layout") {
        put("line-cap", "round")
        put("line-join", "round")
    }
    putJsonObject("paint") {
        put("line-color", color.hex)
        put("line-width", width)
    }
}

private fun JsonObjectBuilder.label(id: String, layer: String, colors: SojournColors, language: String, size: Double) {
    put("id", id)
    put("type", "symbol")
    put("source", SOURCE)
    put("source-layer", layer)
    putJsonObject("layout") {
        put("text-field", nameIn(language))
        put("text-font", json(FONT))
        put("text-size", size)
        put("text-max-width", LABEL_MAX_WIDTH)
    }
    putJsonObject("paint") {
        put("text-color", colors.inkMuted.hex)
        put("text-halo-color", colors.mapLand.hex)
        put("text-halo-width", 1.0)
    }
}

/** The name in [language] where the map data has it, else the local name. */
private fun nameIn(language: String): JsonElement =
    if (language == "ar") {
        json("coalesce", json("get", "name:ar"), json("get", "name"))
    } else {
        json(
            "coalesce",
            json("get", "name:$language"),
            json("get", "name_en"),
            json("get", "name:latin"),
            json("get", "name"),
        )
    }

private fun classIn(vararg classes: String): JsonElement =
    json("match", json("get", "class"), json(*classes), true, false)

/** A line [fromWidth] wide at [fromZoom], widening to [toWidth] at street level. */
private fun ramp(fromZoom: Double, fromWidth: Double, toWidth: Double): JsonElement =
    json("interpolate", json("exponential", ROAD_EXPONENT), json("zoom"), fromZoom, fromWidth, MAX_ROAD_ZOOM, toWidth)

/** A JSON array of strings, numbers, booleans and nested arrays: MapLibre's expression syntax. */
private fun json(vararg items: Any): JsonArray = JsonArray(
    items.map { item ->
        when (item) {
            is JsonElement -> item
            is String -> JsonPrimitive(item)
            is Number -> JsonPrimitive(item)
            is Boolean -> JsonPrimitive(item)
            else -> error("Not a style value: $item")
        }
    },
)

/** `#RRGGBB`: map tokens are opaque. */
internal val Color.hex: String
    get() = "#" + (toArgb() and RGB_MASK).toString(HEX_RADIX).padStart(HEX_DIGITS, '0')

private const val STYLE_VERSION = 8
private const val SOURCE = "openmaptiles"
private const val FONT = "Noto Sans Regular"
private const val LABEL_MAX_WIDTH = 8.0
private const val ROAD_EXPONENT = 1.4
private const val MIN_ROAD_ZOOM = 12.0
private const val MAJOR_ROAD_ZOOM = 8.0
private const val MAX_ROAD_ZOOM = 20.0
private const val THIN = 0.5
private const val WATERWAY_WIDTH = 6.0
private const val MINOR_ROAD_WIDTH = 14.0
private const val MAJOR_ROAD_WIDTH = 22.0
private const val RGB_MASK = 0xFFFFFF
private const val HEX_RADIX = 16
private const val HEX_DIGITS = 6
