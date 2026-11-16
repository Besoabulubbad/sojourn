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
            // SojournPrice(price: PriceDisplay) and the other components take domain types.
            api(project(":core:model"))
            // Motion helpers return animation specs; SojournIcon carries a DrawableResource.
            api(libs.compose.animation)
            api(libs.compose.components.resources)
            implementation(libs.compose.material3)
        }
        named("androidHostTest").dependencies {
            // AppLanguagePairsTest drives Robolectric and a compose rule directly (device locale per test).
            implementation(libs.compose.uiTestJunit4)
            implementation(libs.robolectric)
            implementation(libs.robolectric.annotations)
            implementation(libs.robolectric.shadowsFramework)
        }
    }
}

compose.resources {
    packageOfResClass = "com.abulubad.sojourn.core.designsystem.resources"
}
