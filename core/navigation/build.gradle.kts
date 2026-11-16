plugins {
    id("sojourn.kmp.library")
    id("sojourn.compose")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // NavKey, NavBackStack, SavedStateConfiguration and SerializersModule appear in the public API.
            api(libs.navigation3.runtime)
            api(libs.androidx.savedstate)
            api(libs.kotlinx.serialization.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
