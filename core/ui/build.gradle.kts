plugins {
    id("sojourn.kmp.library")
    id("sojourn.compose")
    id("sojourn.screenshot")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Public API: ScreenState(AppError), HostText(core.model.HostText), SojournViewModel (ViewModel, Flow).
            api(project(":core:model"))
            api(project(":core:common"))
            api(libs.androidx.lifecycle.viewmodel)
            api(libs.kotlinx.coroutines.core)
            implementation(project(":core:designsystem"))
            // StepBackHandler takes system back for screens with their own steps (the search flow).
            implementation(libs.navigationevent.compose)
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
        named("androidHostTest").dependencies {
            // SojournScaffoldModalTest drives a compose rule under Robolectric.
            implementation(libs.compose.uiTest)
            implementation(libs.compose.uiTestJunit4)
            implementation(libs.robolectric.annotations)
        }
    }
}
