// detekt: the single linter (ADR 0014). Project config in config/detekt/detekt.yml on top of the defaults,
// formatting rules and the Compose rule set. Custom design-system rules join via tooling/detekt-rules.
import com.abulubad.sojourn.buildlogic.libs
import com.abulubad.sojourn.buildlogic.library

plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    // KMP layout: every source set under src/, not only src/main.
    source.setFrom(files("src"))
    parallel = true
    basePath = rootProject.projectDir.absolutePath
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    exclude("**/build/**", "**/generated/**")
    reports {
        html.required.set(true)
        sarif.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
    }
}

dependencies {
    "detektPlugins"(libs.library("detekt-formatting"))
    "detektPlugins"(libs.library("detekt-composeRules"))
}
