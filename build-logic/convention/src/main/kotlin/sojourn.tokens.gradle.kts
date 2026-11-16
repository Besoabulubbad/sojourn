// Generates the Sojourn* token objects from design/tokens.json into commonMain before compilation (ADR 0008).
// Applied by core:designsystem only. The generated files live under build/ and are never committed.
import com.abulubad.sojourn.buildlogic.tokens.GenerateDesignTokensTask

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

val generateDesignTokens by tasks.registering(GenerateDesignTokensTask::class) {
    group = "build"
    description = "Generates SojournColors, SojournType, SojournSpacing, … from design/tokens.json."
    tokensFile.set(rootProject.layout.projectDirectory.file("design/tokens.json"))
    packageName.set("com.abulubad.sojourn.core.designsystem.tokens")
    outputDirectory.set(layout.buildDirectory.dir("generated/tokens/commonMain/kotlin"))
}

kotlin {
    sourceSets {
        commonMain {
            // Wiring the task's output as a source directory also makes every compile task depend on it.
            kotlin.srcDir(generateDesignTokens.map { it.outputDirectory })
        }
    }
}
