plugins {
    id("sojourn.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // ResultsViewModel takes the AppClock; ResultsSamples dates the search.
            api(project(":core:common"))
            implementation(libs.kotlinx.datetime)
        }
    }
}
