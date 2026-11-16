package com.abulubad.sojourn.buildlogic.localization

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Verifies the Compose Multiplatform string resources of a module (standards S-03 and S-05, ADR 0009):
 *
 * 1. every `<plurals>` in a locale lists every quantity that locale requires (Arabic: all six CLDR forms);
 * 2. every locale directory (`values`, `values-ar`, …) declares exactly the same string and plural keys, so a
 *    missing translation fails the build instead of falling back to English at runtime;
 * 3. no key is declared twice within a locale.
 *
 * Reads every `.xml` under `values*` directories of [resourcesDirectory]. Writes a short report so the task is
 * cacheable and up-to-date checks work.
 */
@CacheableTask
abstract class VerifyPluralsTask : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val resourcesDirectory: DirectoryProperty

    /** Locale qualifier (`""` for the default `values`) → quantities every plural must declare. */
    @get:Input
    abstract val requiredQuantities: MapProperty<String, List<String>>

    @get:OutputFile
    abstract val report: RegularFileProperty

    @TaskAction
    fun verify() {
        val root = resourcesDirectory.get().asFile
        val locales = root.listFiles { file -> file.isDirectory && (file.name == "values" || file.name.startsWith("values-")) }
            .orEmpty()
            .sortedBy { it.name }
            .associate { dir -> dir.name.removePrefix("values").removePrefix("-") to LocaleResources.read(dir) }
        require(locales.isNotEmpty()) { "No values*/ directories under $root" }

        val problems = mutableListOf<String>()
        locales.forEach { (locale, resources) -> problems += resources.duplicates.map { "values-$locale: '$it' is declared more than once" } }

        val allStrings = locales.values.flatMap { it.strings }.toSet()
        val allPlurals = locales.values.flatMap { it.plurals.keys }.toSet()
        locales.forEach { (locale, resources) ->
            val label = if (locale.isEmpty()) "values" else "values-$locale"
            (allStrings - resources.strings).sorted().forEach { problems += "$label: missing string '$it'" }
            (allPlurals - resources.plurals.keys).sorted().forEach { problems += "$label: missing plurals '$it'" }
            val required = requiredQuantities.get()[locale].orEmpty()
            resources.plurals.forEach { (name, quantities) ->
                val missing = required - quantities
                if (missing.isNotEmpty()) problems += "$label: plurals '$name' lacks quantities ${missing.joinToString()}"
            }
        }

        val summary = locales.entries.joinToString("\n") { (locale, resources) ->
            "values${if (locale.isEmpty()) "" else "-$locale"}: ${resources.strings.size} strings, ${resources.plurals.size} plurals"
        }
        report.get().asFile.apply { parentFile.mkdirs() }.writeText(
            if (problems.isEmpty()) "OK\n$summary\n" else "FAILED\n${problems.joinToString("\n")}\n",
        )
        if (problems.isNotEmpty()) {
            throw GradleException(
                "verifyPlurals found ${problems.size} problem(s) in $root:\n" + problems.joinToString("\n") { "  - $it" },
            )
        }
        logger.lifecycle("verifyPlurals: all locales complete.\n$summary")
    }

    private class LocaleResources(val strings: Set<String>, val plurals: Map<String, Set<String>>, val duplicates: Set<String>) {
        companion object {
            fun read(dir: File): LocaleResources {
                val builder = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = false }.newDocumentBuilder()
                val strings = mutableListOf<String>()
                val plurals = mutableMapOf<String, Set<String>>()
                val duplicates = mutableSetOf<String>()
                dir.listFiles { file -> file.extension == "xml" }.orEmpty().sortedBy { it.name }.forEach { file ->
                    val document = builder.parse(file)
                    val items = document.documentElement.childNodes
                    for (i in 0 until items.length) {
                        val node = items.item(i)
                        val name = node.attributes?.getNamedItem("name")?.nodeValue ?: continue
                        when (node.nodeName) {
                            "string" -> if (!strings.add(name) || name in plurals) duplicates += name else Unit
                            "plurals" -> {
                                val quantities = mutableSetOf<String>()
                                val children = node.childNodes
                                for (j in 0 until children.length) {
                                    val child = children.item(j)
                                    if (child.nodeName == "item") {
                                        child.attributes?.getNamedItem("quantity")?.nodeValue?.let(quantities::add)
                                    }
                                }
                                if (plurals.put(name, quantities) != null || name in strings) duplicates += name
                            }
                        }
                    }
                }
                return LocaleResources(strings.toSet(), plurals, duplicates)
            }
        }
    }
}
