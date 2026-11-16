plugins {
    id("sojourn.kmp.library")
    id("sojourn.compose")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:common"))
            implementation(project(":core:designsystem"))
            api(libs.navigation3.ui)
            api(libs.navigation3.runtime)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
            implementation(libs.navigationevent.compose)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
