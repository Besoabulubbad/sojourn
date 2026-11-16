plugins {
    id("sojourn.kmp.library")
    id("sojourn.koin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // GeoPoint, Money and QuoteId appear in the adapter signatures; platformModule is a Koin Module.
            api(project(":core:model"))
            api(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
