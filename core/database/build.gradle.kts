plugins {
    id("sojourn.kmp.library")
    id("sojourn.sqldelight")
    id("sojourn.koin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // SojournDatabase and DatabaseDriverFactory expose SQLDelight's SqlDriver and generated queries;
            // databaseModule is a Koin Module.
            api(libs.sqldelight.runtime)
            api(libs.koin.core)
        }
    }
}
