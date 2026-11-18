plugins {
    id("sojourn.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // SavedScreen takes the SojournSnackbarHostState its route keeps, to offer "Undo".
            api(project(":core:designsystem"))
            // The samples' dates.
            implementation(libs.kotlinx.datetime)
        }
    }
}
