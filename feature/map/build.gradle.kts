plugins {
    id("sojourn.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // MapScreen draws SojournMap, whose state and pin groups are design-system types.
            api(project(":core:designsystem"))
            // The samples' dates.
            implementation(libs.kotlinx.datetime)
        }
    }
}
