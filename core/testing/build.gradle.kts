plugins {
    id("sojourn.kmp.library")
    id("sojourn.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Fixtures expose model, common and datetime types; TestDispatcherProvider exposes coroutines-test.
            api(project(":core:model"))
            api(project(":core:common"))
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            // SojournScreenshotTest: its subclasses inherit the runner, config and compose rule.
            api(project(":core:designsystem"))
            api(libs.robolectric)
            api(libs.robolectric.annotations)
            api(libs.junit)
            api(libs.compose.uiTestJunit4)
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.core)
            implementation(libs.compose.uiTestManifest)
        }
    }
}
