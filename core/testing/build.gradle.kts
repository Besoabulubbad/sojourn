plugins {
    id("sojourn.kmp.library")
    id("sojourn.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Fixtures expose model, common and datetime types; TestDispatcherProvider exposes coroutines-test;
            // SnapshotCase and the runners render through SojournPreview; the fakes implement core:data's interfaces.
            api(project(":core:model"))
            api(project(":core:common"))
            api(project(":core:designsystem"))
            api(project(":core:data"))
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            // SojournScreenshotSuite: its subclasses inherit the runner, config and compose rule.
            api(libs.robolectric)
            api(libs.robolectric.annotations)
            api(libs.junit)
            api(libs.compose.uiTestJunit4)
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.core)
            implementation(libs.compose.uiTestManifest)
        }
        iosMain.dependencies {
            // Brings Compose's ui-test (runSkikoComposeUiTest) transitively.
            implementation(libs.roborazzi.composeIos)
        }
    }
}
