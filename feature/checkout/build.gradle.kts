plugins {
    id("sojourn.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // CheckoutViewModel takes the AppClock; its state and the dates editor are kotlinx-datetime types.
            api(project(":core:common"))
            api(libs.kotlinx.datetime)
            // CheckoutScreen takes the SojournSheetStates its route hoists, to animate an editor out.
            api(project(":core:designsystem"))
            // Payment goes through the platform payment sheet.
            api(project(":core:platform"))
        }
    }
}
