plugins {
    id("sojourn.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // FiltersScreen takes the SojournSheetState its route hoists, to animate the sheet out on apply.
            api(project(":core:designsystem"))
            // FiltersSamples dates the search.
            implementation(libs.kotlinx.datetime)
        }
    }
}
