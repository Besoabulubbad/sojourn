plugins {
    id("sojourn.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
            // SojournLog hands out Kermit Loggers (kermit-core) configured through the kermit facade.
            api(libs.kermit.core)
            api(libs.kermit)
        }
    }
}
