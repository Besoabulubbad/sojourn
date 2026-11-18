plugins {
    id("sojourn.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // SearchUiState carries today's date and the months on offer; SearchViewModel takes the AppClock.
            api(libs.kotlinx.datetime)
            api(project(":core:common"))
        }
    }
}
