// Root build: plugins are declared here (apply false) so every module resolves the same versions,
// and repository-wide tooling (dependency analysis) is configured once.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.dependencyAnalysis)
}

// `./gradlew buildHealth` (ADR 0014). The plugin is applied per module by the convention plugins; the root
// aggregates. Policy: everything fails except "unused", which warns while modules are skeletons and flips to
// fail at the end of Phase 2 (docs/plan.md).
dependencyAnalysis {
    structure {
        // Compose Multiplatform: we declare org.jetbrains.compose.* coordinates, which resolve to
        // androidx.compose.* on Android. One bundle so the analyser does not ask for the androidx twins.
        bundle("compose-multiplatform") {
            listOf("runtime", "foundation", "ui", "material3", "material", "animation", "components").forEach {
                includeGroup("org.jetbrains.compose.$it")
            }
            listOf("runtime", "foundation", "ui", "material3", "material", "animation").forEach {
                includeGroup("androidx.compose.$it")
            }
        }
    }
    issues {
        all {
            // Default is warn (covers "unused" during the skeleton phase); each category that must never
            // merge is set to fail explicitly.
            onAny { severity("warn") }
            onUsedTransitiveDependencies {
                severity("fail")
                // Reflection and the JUnit bridge are pulled in by the test runtime, not by our code.
                exclude("org.jetbrains.kotlin:kotlin-reflect", "org.jetbrains.kotlin:kotlin-test-junit")
            }
            onIncorrectConfiguration { severity("fail") }
            onCompileOnly { severity("fail") }
            onRuntimeOnly { severity("fail") }
            onUnusedAnnotationProcessors { severity("fail") }
            onRedundantPlugins { severity("fail") }
        }
    }
}
