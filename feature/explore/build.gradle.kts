plugins {
    id("sojourn.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // ExploreSamples dates the last search.
            implementation(libs.kotlinx.datetime)
        }
    }
}
