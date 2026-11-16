// A feature module = one screen. Library + Compose + Koin (with Compose/ViewModel/Nav3 integration)
// + the fixed set of core modules a feature may see. Nothing else (Konsist enforces the rest).
import com.abulubad.sojourn.buildlogic.libs
import com.abulubad.sojourn.buildlogic.library

plugins {
    id("sojourn.kmp.library")
    id("sojourn.compose")
    id("sojourn.koin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:ui"))
            implementation(project(":core:navigation"))
            implementation(project(":core:domain"))
            implementation(project(":core:platform"))

            implementation(libs.library("koin-compose"))
            implementation(libs.library("koin-composeViewmodel"))
            implementation(libs.library("koin-composeNavigation3"))
            implementation(libs.library("androidx-lifecycle-viewmodelCompose"))
            implementation(libs.library("androidx-lifecycle-runtimeCompose"))
            implementation(libs.library("navigation3-ui"))
            implementation(libs.library("navigation3-runtime"))
            implementation(libs.library("kotlinx-coroutines-core"))
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.library("kotlinx-coroutines-test"))
            implementation(libs.library("turbine"))
        }
    }
}
