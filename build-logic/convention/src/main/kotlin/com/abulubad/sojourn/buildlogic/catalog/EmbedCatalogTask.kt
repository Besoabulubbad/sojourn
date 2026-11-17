package com.abulubad.sojourn.buildlogic.catalog

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Embeds the committed seed catalogue (`catalog.json`) as a Kotlin string constant in commonMain, so
 * `LocalSojournApi` reads it with no file I/O on any platform, tests included (ADR 0007). Cacheable.
 */
@CacheableTask
abstract class EmbedCatalogTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val catalogFile: RegularFileProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun embed() {
        val json = catalogFile.get().asFile.readText()
        val packageDir = outputDirectory.get().asFile.resolve(packageName.get().replace('.', '/'))
        outputDirectory.get().asFile.deleteRecursively()
        packageDir.mkdirs()
        packageDir.resolve("BundledCatalog.kt").writeText(CatalogEmbedding.source(json, packageName.get()))
        logger.lifecycle("Embedded seed catalogue (${json.length} characters).")
    }
}

/** Turns the catalogue text into Kotlin source. Separate from the task so it is unit-testable. */
object CatalogEmbedding {

    /**
     * A JVM class constant holds at most 65,535 bytes of modified UTF-8; Arabic is 2–3 bytes a character, so the
     * text is split into chunks well under that and joined at first use.
     */
    const val CHUNK = 8_000

    fun source(json: String, packageName: String): String = buildString {
        appendLine("// Generated from catalog.json by the sojourn.catalog plugin. Do not edit.")
        appendLine("package $packageName")
        appendLine()
        appendLine("/** The seed catalogue, embedded at build time from `catalog.json`. */")
        appendLine("internal object BundledCatalog {")
        appendLine("    val json: String by lazy {")
        appendLine("        buildString {")
        json.chunked(CHUNK).forEach { appendLine("            append(\"${escape(it)}\")") }
        appendLine("        }")
        appendLine("    }")
        appendLine("}")
    }

    /** Kotlin string-literal escaping: backslash, quote, dollar and line breaks. */
    fun escape(text: String): String = buildString {
        text.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '$' -> append("\\$")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
    }
}
