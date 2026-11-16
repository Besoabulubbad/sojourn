plugins {
    id("sojourn.kmp.library")
    id("sojourn.sqldelight")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:common"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
