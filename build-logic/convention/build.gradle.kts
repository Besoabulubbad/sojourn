plugins {
    `kotlin-dsl`
}

group = "com.abulubad.sojourn.buildlogic"

kotlin {
    jvmToolchain(21)
}

dependencies {
    // Plugin artifacts that the precompiled script plugins apply by id.
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.kotlin.composeCompilerGradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.sqldelight.gradlePlugin)
    implementation(libs.dependencyAnalysis.gradlePlugin)
}
