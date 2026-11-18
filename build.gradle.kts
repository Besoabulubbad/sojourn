// Root build: plugins are declared here (apply false) so every module resolves the same versions,
// and repository-wide tooling (dependency analysis, the build-logic test suite) is configured once.
plugins {
    base
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.dependencyAnalysis)
}

// The token generator and the resource checks live in build-logic; their unit tests run with `check` too.
tasks.check {
    dependsOn(gradle.includedBuild("build-logic").task(":convention:test"))
}

// `./gradlew buildHealth` (ADR 0014). The plugin is applied per module by the convention plugins; the root
// aggregates. Policy: every category fails. Modules that are still skeletons (their convention plugin declares
// dependencies their phase will use) warn instead; remove a module from this list in the PR that gives it code.
val skeletonModules = listOf(
    "saved", "bookings", "profile",
).map { ":feature:$it" } // Phase 5: each leaves this list in its own PR
dependencyAnalysis {
    abi {
        // The Compose compiler hoists capture-free composable lambdas into public `ComposableSingletons$<File>`
        // classes; a slot lambda's parameter type (e.g. Material's RangeSliderState) would otherwise count as
        // public API. They are compiler internals, so they are not part of any module's ABI.
        exclusions {
            excludeClasses(".*\\.ComposableSingletons\\$.*")
        }
    }
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
        // Same pattern for the JetBrains multiplatform forks of AndroidX: on Android they resolve to Google's.
        bundle("lifecycle") {
            includeGroup("org.jetbrains.androidx.lifecycle")
            includeGroup("androidx.lifecycle")
        }
        bundle("savedstate") {
            includeGroup("org.jetbrains.androidx.savedstate")
            includeGroup("androidx.savedstate")
        }
        bundle("navigationevent") {
            includeGroup("org.jetbrains.androidx.navigationevent")
            includeGroup("androidx.navigationevent")
        }
        // coil-compose is the one artifact we declare; its core modules come with it.
        // Navigation 3: the JetBrains artifacts resolve to androidx's on Android.
        bundle("navigation3") {
            includeGroup("org.jetbrains.androidx.navigation3")
            includeGroup("androidx.navigation3")
        }
        // koinViewModel() (compose-viewmodel) is inline, so its bytecode points at koin-core-viewmodel.
        bundle("koin-viewmodel") {
            includeDependency("io.insert-koin:koin-compose-viewmodel")
            includeDependency("io.insert-koin:koin-core-viewmodel")
        }
        bundle("coil") {
            includeGroup("io.coil-kt.coil3")
        }
    }
    issues {
        all {
            onAny { severity("warn") }
            onUnusedDependencies { severity("fail") }
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
        skeletonModules.forEach { path ->
            project(path) {
                onAny { severity("warn") }
                onUnusedDependencies { severity("warn") }
                onUsedTransitiveDependencies { severity("warn") }
                onIncorrectConfiguration { severity("warn") }
                onCompileOnly { severity("warn") }
                onRuntimeOnly { severity("warn") }
            }
        }
    }
}
