plugins {
    id("sojourn.kmp.library")
    id("sojourn.compose")
    id("sojourn.tokens")
    id("sojourn.screenshot")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // SojournTheme(locale: AppLocale) and SojournVariant expose AppLocale: part of this module's ABI.
            api(project(":core:localization"))
            // Motion helpers return animation specs; SojournIcon carries a DrawableResource.
            api(libs.compose.animation)
            api(libs.compose.components.resources)
            implementation(libs.compose.material3)
        }
    }
}

compose.resources {
    packageOfResClass = "com.abulubad.sojourn.core.designsystem.resources"
}
