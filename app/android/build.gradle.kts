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

    implementation(libs.androidx.activity) // enableEdgeToEdge
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.koin.android)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}
