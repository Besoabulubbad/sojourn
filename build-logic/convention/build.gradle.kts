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
    implementation(libs.roborazzi.gradlePlugin)

    // design/tokens.json → Kotlin (sojourn.tokens, ADR 0008). Parsed as a JsonElement tree, so no
    // serialization compiler plugin is needed inside the build logic.
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.kotlin.test)
}

tasks.test {
    // The generator test runs against the real token file so the build logic is proven on every `check`.
    systemProperty("sojourn.tokensFile", rootDir.parentFile.resolve("design/tokens.json").absolutePath)
}
