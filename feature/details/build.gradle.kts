plugins {
    id("sojourn.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // DetailsViewModel takes the AppClock; its state and the dates sheet are kotlinx-datetime types.
            api(project(":core:common"))
            api(libs.kotlinx.datetime)
            // DetailsScreen takes the SojournSheetStates its route hoists, to animate a sheet out.
            api(project(":core:designsystem"))
            // Share goes through the platform share sheet.
            implementation(project(":core:platform"))
        }
    }
}
