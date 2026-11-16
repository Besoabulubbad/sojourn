// Resource completeness gate for core:localization (ADR 0009, standards S-03 and S-05): every locale declares the
// same keys, and Arabic plurals carry all six CLDR forms. Runs as part of `check`.
import com.abulubad.sojourn.buildlogic.localization.VerifyPluralsTask

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

val verifyPlurals by tasks.registering(VerifyPluralsTask::class) {
    group = "verification"
    description = "Fails if a locale is missing a string/plural key or an Arabic plural lacks one of its six forms."
    resourcesDirectory.set(layout.projectDirectory.dir("src/commonMain/composeResources"))
    requiredQuantities.set(
        mapOf(
            "" to listOf("one", "other"),
            "ar" to listOf("zero", "one", "two", "few", "many", "other"),
        ),
    )
    report.set(layout.buildDirectory.file("reports/verifyPlurals/report.txt"))
}

tasks.named("check") {
    dependsOn(verifyPlurals)
}
