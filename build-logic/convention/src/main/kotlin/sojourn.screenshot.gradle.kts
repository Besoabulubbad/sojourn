// Screenshot tests on Android and iOS with Roborazzi (ADR 0010). Cases are written once in `commonTest` as
// SnapshotCase lists; `androidHostTest` runs them on Robolectric (SojournScreenshotSuite) and `iosTest` on the
// simulator (verifySnapshots). Tasks: record/verify/compareRoborazziAndroidHostTest and …IosSimulatorArm64. Plain
// test tasks verify because gradle.properties sets `roborazzi.test.verify=true`. Baselines are committed per module
// under screenshots/<androidHostTest|iosSimulatorArm64>/ (separate directories also stop the two finalizers racing).
import com.abulubad.sojourn.buildlogic.libs
import com.abulubad.sojourn.buildlogic.library
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithSimulatorTests
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.github.takahirom.roborazzi")
}

roborazzi {
    outputDir.set(layout.projectDirectory.dir("screenshots"))
    separateOutputDirs.set(true)
}

kotlin {
    // iOS screenshots run from an optimised (release) test binary. In a debug binary, rendering and comparing a
    // 1236 × 2745 px image in Kotlin/Native takes ~13 s; the CI job outgrew its hour as screens were added.
    targets.withType<KotlinNativeTargetWithSimulatorTests>().configureEach {
        binaries.test(listOf(NativeBuildType.RELEASE))
        testRuns.named("test") { setExecutionSourceFrom(binaries.getTest(NativeBuildType.RELEASE)) }
    }
    sourceSets {
        commonTest.dependencies {
            // SnapshotCase; the platform runners come with it.
            implementation(project(":core:testing"))
        }
        // Created by the Android target's withHostTest {}; match lazily so plugin order does not matter.
        matching { it.name == "androidHostTest" }.configureEach {
            dependencies {
                implementation(libs.library("robolectric"))
            }
        }
    }
}

// Roborazzi's Compose test stack resolves Espresso 3.5.1, whose event injection calls
// InputManager.getInstance(), removed in API 37. A constraint lifts it to the catalog's 3.7.0 without
// declaring a dependency our code does not use.
dependencies {
    constraints {
        add("androidHostTestImplementation", libs.library("androidx-espresso-core")) {
            because("Espresso < 3.7 cannot initialise on the API 37 Robolectric runtime")
        }
        add("androidHostTestImplementation", "androidx.test:core:1.7.0") {
            because("matches Espresso 3.7.0")
        }
    }
}

// A release link runs the Kotlin/Native optimiser inside the Gradle daemon and needs several GB; a dozen at once
// exhaust the heap (and the CI runner's 7 GB). They take turns through one shared service; everything else stays
// parallel.
abstract class ReleaseTestLinks : BuildService<BuildServiceParameters.None>

val releaseTestLinks = gradle.sharedServices.registerIfAbsent("releaseTestLinks", ReleaseTestLinks::class) {
    maxParallelUsages.set(1)
}

tasks.withType<KotlinNativeLink>()
    .matching { it.name.startsWith("linkReleaseTest") }
    .configureEach { usesService(releaseTestLinks) }
