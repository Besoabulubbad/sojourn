package com.abulubad.sojourn.buildlogic.tokens

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * The design system's `tokens.json`, read into the shapes the generator needs (ADR 0008).
 *
 * Only the families the app consumes are modelled; unknown keys are ignored so the design tool can add
 * metadata without breaking the build. A missing family or malformed value fails loudly at generation time.
 */
class DesignTokens private constructor(root: JsonObject) {
    val name: String = root.string("name")
    val version: Int = root.getValue("version").jsonPrimitive.int

    val themes: List<String> = root.obj("color").arr("themes").map { it.jsonObject.string("id") }
    val colors: List<ThemedToken> = root.obj("color").arr("tokens").map { ThemedToken.from(it.jsonObject, themes) }

    val typeFamilies: List<String> = root.obj("type").obj("families").keys.toList()
    val typeStyles: List<TypeStyle> = root.obj("type").arr("groups").flatMap { group ->
        val family = group.jsonObject.string("family")
        group.jsonObject.arr("styles").map { TypeStyle.from(it.jsonObject, family) }
    }

    val spacing: List<SimpleToken> = root.obj("spacing").arr("tokens").map { SimpleToken.from(it.jsonObject) }
    val radius: List<SimpleToken> = root.obj("radius").arr("tokens").map { SimpleToken.from(it.jsonObject) }
    val shadows: List<ThemedToken> = root.obj("shadow").arr("tokens").map { ThemedToken.from(it.jsonObject, themes) }
    val sizes: List<SimpleToken> = root.obj("size").arr("tokens").map { SimpleToken.from(it.jsonObject) }
    val durations: List<SimpleToken> = root.obj("duration").arr("tokens").map { SimpleToken.from(it.jsonObject) }
    val easings: List<SimpleToken> = root.obj("easing").arr("tokens").map { SimpleToken.from(it.jsonObject) }

    companion object {
        fun parse(text: String): DesignTokens = DesignTokens(Json.parseToJsonElement(text).jsonObject)
    }
}

/** A token with one value, e.g. `space-4 = 16px`. */
class SimpleToken(val name: String, val value: String, val usage: String) {
    companion object {
        fun from(obj: JsonObject) = SimpleToken(obj.string("name"), obj.string("value"), obj.stringOrEmpty("usage"))
    }
}

/** A token with one value per theme, e.g. a colour or a shadow. */
class ThemedToken(val name: String, val values: Map<String, String>, val usage: String) {
    companion object {
        fun from(obj: JsonObject, themes: List<String>): ThemedToken {
            val value = obj.obj("value")
            val values = themes.associateWith { theme ->
                value[theme]?.jsonPrimitive?.content ?: error("Token '${obj.string("name")}' has no value for theme '$theme'")
            }
            return ThemedToken(obj.string("name"), values, obj.stringOrEmpty("usage"))
        }
    }
}

/** One text style: numbers only; the font family is resolved by [family] at theme time. */
class TypeStyle(
    val name: String,
    val family: String,
    val fontSizePx: Int,
    val lineHeightPx: Int,
    val fontWeight: Int,
    val letterSpacingEm: Double?,
    val usage: String,
) {
    companion object {
        fun from(obj: JsonObject, family: String) = TypeStyle(
            name = obj.string("name"),
            family = family,
            fontSizePx = obj.string("fontSize").px(),
            lineHeightPx = obj.string("lineHeight").px(),
            fontWeight = obj.getValue("fontWeight").jsonPrimitive.int,
            letterSpacingEm = obj["letterSpacing"]?.jsonPrimitive?.content?.removeSuffix("em")?.toDouble(),
            usage = obj.stringOrEmpty("usage"),
        )
    }
}

private fun JsonObject.obj(key: String): JsonObject = getValue(key).jsonObject
private fun JsonObject.arr(key: String) = getValue(key).jsonArray
private fun JsonObject.string(key: String): String = getValue(key).jsonPrimitive.content
private fun JsonObject.stringOrEmpty(key: String): String = this[key]?.jsonPrimitive?.content.orEmpty()

/** `"16px"` → 16. Tokens are authored in CSS pixels, which map 1:1 to dp/sp. */
internal fun String.px(): Int = removeSuffix("px").trim().toInt()
