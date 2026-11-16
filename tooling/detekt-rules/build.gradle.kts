import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlinJvm)
    id("sojourn.detekt")
    alias(libs.plugins.dependencyAnalysis)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        // detekt 1.23.8 runs these rules on its own Kotlin 2.0 runtime; do not emit calls to newer stdlib APIs.
        apiVersion.set(KotlinVersion.KOTLIN_2_0)
        languageVersion.set(KotlinVersion.KOTLIN_2_0)
    }
}

dependencies {
    // Provided by detekt at analysis time (detekt's extension guide).
    compileOnly(libs.detekt.api)

    testImplementation(libs.detekt.api)
    testImplementation(libs.detekt.test)
    testImplementation(libs.detekt.testUtils)
    testImplementation(libs.detekt.kotlinCompiler)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.testJunit)
}
