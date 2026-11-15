plugins {
    alias(libs.plugins.kotlinJvm)
    id("sojourn.detekt")
    alias(libs.plugins.dependencyAnalysis)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.junit)
}

tasks.test {
    // Konsist reads the whole repository, so its inputs are every Kotlin source file — never build outputs,
    // which would create undeclared task dependencies.
    listOf("app", "core", "feature").forEach { dir ->
        inputs.files(
            rootProject.fileTree(dir) {
                include("**/*.kt", "**/*.kts")
                exclude("**/build/**")
            },
        ).withPropertyName("${dir}Sources").withPathSensitivity(PathSensitivity.RELATIVE)
    }
}
