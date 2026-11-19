plugins {
    id("sojourn.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // The Language page's ViewModel takes the AppLocaleController, and its state the AppLocale.
            api(project(":core:localization"))
            // The samples' dates.
            implementation(libs.kotlinx.datetime)
        }
    }
}
