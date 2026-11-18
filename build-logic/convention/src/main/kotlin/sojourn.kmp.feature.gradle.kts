// A feature module = one screen. Library + Compose + Koin (with Compose/ViewModel/Nav3 integration)
// + the fixed set of core modules a feature may see. Nothing else (Konsist enforces the rest).
import com.abulubad.sojourn.buildlogic.libs
import com.abulubad.sojourn.buildlogic.library

plugins {
    id("sojourn.kmp.library")
    id("sojourn.compose")
    id("sojourn.koin")
    id("sojourn.screenshot")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // A feature's public surface: its ViewModel takes use cases, its contract speaks model and ui types,
            // `<name>Entries` extends Navigation 3's EntryProviderScope with core:navigation keys, `<name>Module`
            // is a Koin Module.
            api(project(":core:model"))
            api(project(":core:ui"))
            api(project(":core:navigation"))
            api(project(":core:domain"))
            api(libs.library("navigation3-runtime"))
            api(libs.library("koin-core"))
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(project(":core:designsystem"))
            implementation(libs.library("koin-compose"))
            implementation(libs.library("koin-composeViewmodel"))
            implementation(libs.library("androidx-lifecycle-viewmodel"))
            implementation(libs.library("androidx-lifecycle-viewmodelCompose"))
            implementation(libs.library("koin-coreViewmodel"))
            implementation(libs.library("androidx-lifecycle-runtimeCompose"))
            implementation(libs.library("kotlinx-coroutines-core"))
            // core:platform, maps, payments and the like are declared by the features that use them.
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            // The fake repositories implement core:data's interfaces; features still never import it (Konsist).
            implementation(project(":core:data"))
            implementation(libs.library("kotlinx-coroutines-test"))
            implementation(libs.library("turbine"))
        }
    }
}
