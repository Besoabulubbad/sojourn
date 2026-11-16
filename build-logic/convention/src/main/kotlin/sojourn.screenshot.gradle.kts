// Screenshot tests with Roborazzi on Robolectric, in the AGP KMP library plugin's `androidHostTest` source set
// (ADR 0010). Roborazzi registers `recordRoborazziAndroidHostTest` / `verifyRoborazziAndroidHostTest` /
// `compareRoborazziAndroidHostTest`; plain test tasks verify because gradle.properties sets
// `roborazzi.test.verify=true`. Baselines are committed per module under src/androidHostTest/screenshots.
import com.abulubad.sojourn.buildlogic.libs
import com.abulubad.sojourn.buildlogic.library

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.github.takahirom.roborazzi")
}

roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/androidHostTest/screenshots"))
}

kotlin {
    sourceSets {
        // Created by the Android target's withHostTest {}; match lazily so plugin order does not matter.
        matching { it.name == "androidHostTest" }.configureEach {
            dependencies {
                // SojournScreenshotTest brings Roborazzi, Robolectric and the compose test rule; tests only
                // write @Test methods against it.
                implementation(project(":core:testing"))
                implementation(libs.library("junit"))
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
