// Embeds the seed catalogue (catalog.json) into commonMain as BundledCatalog (ADR 0007).
// Applied by core:network only. The generated file lives under build/ and is never committed.
import com.abulubad.sojourn.buildlogic.catalog.EmbedCatalogTask

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

val embedCatalog by tasks.registering(EmbedCatalogTask::class) {
    group = "build"
    description = "Embeds src/commonMain/catalog/catalog.json as BundledCatalog."
    catalogFile.set(layout.projectDirectory.file("src/commonMain/catalog/catalog.json"))
    packageName.set("com.abulubad.sojourn.core.network.local")
    outputDirectory.set(layout.buildDirectory.dir("generated/catalog/commonMain/kotlin"))
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(embedCatalog.map { it.outputDirectory })
        }
    }
}
