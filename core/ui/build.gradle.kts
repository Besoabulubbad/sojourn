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
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}
