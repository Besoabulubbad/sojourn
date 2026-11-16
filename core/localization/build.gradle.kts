plugins {
    id("sojourn.kmp.library")
    id("sojourn.compose")
    id("sojourn.koin")
    id("sojourn.localization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Public API: SojournFormatter(Money), AppLocale(DayOfWeek), AppLocaleController(Settings, StateFlow),
            // localizationModule (Koin Module) and the public Res class (compose resources).
            api(project(":core:model"))
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
            api(libs.multiplatformSettings)
            api(libs.koin.core)
            api(libs.compose.components.resources)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        commonTest.dependencies {
            implementation(libs.multiplatformSettings.test)
        }
    }
}

compose.resources {
    // Features read Res.string.* / Res.plurals.* from this module (ADR 0009).
    publicResClass = true
    packageOfResClass = "com.abulubad.sojourn.core.localization.resources"
}
