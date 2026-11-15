plugins {
    id("sojourn.kmp.library")
    id("sojourn.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(libs.compose.material3)
        }
    }
}
