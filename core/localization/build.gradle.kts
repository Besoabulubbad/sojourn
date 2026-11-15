plugins {
    id("sojourn.kmp.library")
    id("sojourn.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:common"))
            implementation(libs.multiplatformSettings)
        }
    }
}
