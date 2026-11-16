package com.abulubad.sojourn.tooling.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

/**
 * The module dependency contract and forbidden-import rules from docs/architecture/modules.md and CLAUDE.md,
 * asserted over every Kotlin file in the repository (ADR 0003, 0014).
 *
 * Rules are written so that a skeleton repository passes vacuously and the first violating file fails.
 */
class ArchitectureTest {

    /** Product code only: the build logic and this tooling are not shipped and may use the JDK freely. */
    private val scope = Konsist.scopeFromProject()
        .slice { file -> PRODUCT_ROOTS.any { file.path.contains(it) } }

    private val featureFiles: List<KoFileDeclaration> = scope.files.filter { it.path.contains("/feature/") }

    @Test
    fun `feature modules never import data infrastructure`() {
        featureFiles.assertFalse(
            additionalMessage = "Features reach data only through core:domain use cases.",
        ) { file ->
            file.imports.any { import -> DATA_INFRASTRUCTURE.any { import.name.startsWith(it) } }
        }
    }

    @Test
    fun `feature modules never import another feature`() {
        featureFiles.assertFalse(
            additionalMessage = "Features talk through navigation keys and shared use cases, never directly.",
        ) { file ->
            val own = "$FEATURE_PACKAGE.${ownFeature(file.path)}."
            file.imports.any { it.name.startsWith("$FEATURE_PACKAGE.") && !it.name.startsWith(own) }
        }
    }

    @Test
    fun `material is imported only inside core designsystem`() {
        scope.files
            .filter { !it.path.contains("/core/designsystem/") }
            .assertFalse(
                additionalMessage = "Material 3 lives in core:designsystem; compose Sojourn* components instead.",
            ) { file ->
                file.imports.any { MATERIAL_PACKAGES.any { prefix -> it.name.startsWith(prefix) } }
            }
    }

    @Test
    fun `navigation keys are declared only in core navigation`() {
        scope.files
            .filter { !it.path.contains("/core/navigation/") }
            .assertFalse(
                additionalMessage = "NavKeys belong in core:navigation only (ADR 0005).",
            ) { file ->
                val declarations = file.classes() + file.interfaces() + file.objects()
                declarations.any { it.hasParentWithName("NavKey", indirectParents = true) }
            }
    }

    @Test
    fun `koin component service locator is used only by the app shell`() {
        scope.files
            .filter { !it.path.contains("/app/shared/") }
            .assertFalse(
                additionalMessage = "Constructor injection only; KoinComponent is reserved for app:shared.",
            ) { file ->
                file.imports.any { it.name == "org.koin.core.component.KoinComponent" }
            }
    }

    @Test
    fun `platform locale apis are touched only by core localization`() {
        scope.files
            .filter { !it.path.contains("/core/localization/") }
            // Tests may set the device locale to simulate it (the device/app language pairs, L-03).
            .filterNot { TEST_SOURCE_SETS.any { set -> it.path.contains("/src/$set/") } }
            .assertFalse(
                additionalMessage = "AppLocale is the single source for language and formatting (ADR 0009).",
            ) { file ->
                file.imports.any { import -> PLATFORM_LOCALE_APIS.any { import.name.startsWith(it) } }
            }
    }

    @Test
    fun `feature view models extend SojournViewModel`() {
        val viewModels = featureFiles.flatMap { it.classes() }.filter { it.name.endsWith("ViewModel") }
        if (viewModels.isEmpty()) return // skeleton repository
        viewModels.assertTrue(
            additionalMessage = "Every screen follows MVI through SojournViewModel (ADR 0006).",
        ) {
            it.hasParentWithName("SojournViewModel", indirectParents = true)
        }
    }

    @Test
    fun `a feature with a view model also exposes Route and Screen composables`() {
        featureFiles
            .groupBy { ownFeature(it.path) }
            .filterValues { files -> files.any { f -> f.classes().any { it.name.endsWith("ViewModel") } } }
            .forEach { (feature, files) ->
                val functionNames = files.flatMap { it.functions() }.map { it.name }
                check(functionNames.any { it.endsWith("Route") }) { "feature:$feature has no *Route composable" }
                check(functionNames.any { it.endsWith("Screen") }) { "feature:$feature has no *Screen composable" }
            }
    }

    @Test
    fun `every public design-system component has a screenshot test`() {
        val screenshotTests = scope.files
            .filter { it.path.contains("/core/designsystem/src/androidHostTest/") }
            .joinToString("\n") { it.text }
        val components = scope.files
            .filter { it.path.contains("/core/designsystem/src/commonMain/") && it.path.contains("/components/") }
            .flatMap { it.functions() }
            .filter { it.name.startsWith("Sojourn") && it.hasPublicOrDefaultModifier }
            .filter { it.hasAnnotationWithName("Composable") }
            .filterNot { it.name.endsWith("Preview") }
        components.assertTrue(additionalMessage = "Every Sojourn* component needs a screenshot test (ADR 0010).") {
            "${it.name}(" in screenshotTests
        }
    }

    private fun ownFeature(path: String): String = path.substringAfter("/feature/").substringBefore("/")

    private companion object {
        const val FEATURE_PACKAGE = "com.abulubad.sojourn.feature"
        val PRODUCT_ROOTS = listOf("/app/", "/core/", "/feature/")
        val TEST_SOURCE_SETS = listOf("commonTest", "androidHostTest", "androidDeviceTest", "iosTest")
        val DATA_INFRASTRUCTURE = listOf(
            "com.abulubad.sojourn.core.data.",
            "com.abulubad.sojourn.core.database.",
            "com.abulubad.sojourn.core.network.",
        )
        val MATERIAL_PACKAGES = listOf("androidx.compose.material3", "androidx.compose.material.")
        val PLATFORM_LOCALE_APIS = listOf(
            "java.util.Locale",
            "java.text.",
            "platform.Foundation.NSLocale",
            "android.icu.",
        )
    }
}
