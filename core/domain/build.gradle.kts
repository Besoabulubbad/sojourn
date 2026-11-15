plugins {
    id("sojourn.kmp.library")
    id("sojourn.koin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:common"))
            implementation(project(":core:data"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
