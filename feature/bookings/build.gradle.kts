plugins {
    id("sojourn.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // The ViewModels take the AppClock; their state holds today's date.
            api(project(":core:common"))
            api(libs.kotlinx.datetime)
            // The screens take the sheet and snackbar states their routes hoist.
            api(project(":core:designsystem"))
            // Directions open the platform maps app; the key code goes to the clipboard.
            implementation(project(":core:platform"))
        }
    }
}
