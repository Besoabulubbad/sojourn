plugins {
    id("sojourn.android.application")
}

android {
    namespace = "com.abulubad.sojourn"

    defaultConfig {
        applicationId = "com.abulubad.sojourn"
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(project(":app:shared"))
    // MainActivity binds the notification-permission launcher to AndroidReminders.
    implementation(project(":core:platform"))

    implementation(libs.androidx.activity) // enableEdgeToEdge
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android) // androidContext() for the database and settings
    implementation(libs.koin.core) // initKoin's KoinAppDeclaration
    implementation(libs.compose.runtime) // setContent { App() }
    implementation(libs.compose.ui)
    // The map's native renderer: an Android app packages one MapLibre runtime; Vulkan, as MapLibre Compose
    // recommends (ADR 0017).
    runtimeOnly(libs.maplibre.compose.runtimeVulkan)
}
