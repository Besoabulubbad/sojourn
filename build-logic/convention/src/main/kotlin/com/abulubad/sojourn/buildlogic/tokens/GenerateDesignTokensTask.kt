package com.abulubad.sojourn.buildlogic.tokens

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
 * Generates the `Sojourn*` token objects from `design/tokens.json` into a Kotlin source directory that the
 * `sojourn.tokens` plugin adds to `commonMain` (ADR 0008). Cacheable: same JSON in, same Kotlin out.
 */
@CacheableTask
abstract class GenerateDesignTokensTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val tokensFile: RegularFileProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val tokens = DesignTokens.parse(tokensFile.get().asFile.readText())
        val packageDir = outputDirectory.get().asFile.resolve(packageName.get().replace('.', '/'))
        outputDirectory.get().asFile.deleteRecursively()
        packageDir.mkdirs()
        DesignTokenCodegen(tokens, packageName.get()).files().forEach { (fileName, content) ->
            packageDir.resolve(fileName).writeText(content)
        }
        logger.lifecycle(
            "Generated design tokens (${tokens.name} v${tokens.version}): " +
                "${tokens.colors.size} colours, ${tokens.typeStyles.size} type styles, " +
                "${tokens.spacing.size} spacing, ${tokens.radius.size} radii, ${tokens.shadows.size} shadows, " +
                "${tokens.sizes.size} sizes, ${tokens.durations.size} durations, ${tokens.easings.size} easings.",
        )
    }
}
