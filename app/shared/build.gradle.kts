plugins {
    id("sojourn.kmp.library")
    id("sojourn.compose")
    id("sojourn.koin")
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // The graph (SojournModules): every core module with a Koin module. Feature modules join as they land.
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(project(":core:platform"))
            implementation(project(":core:database"))
            implementation(project(":core:network"))
            implementation(project(":core:data"))
            implementation(project(":core:domain"))
            // Features, as they land.
            implementation(project(":feature:explore"))
            implementation(project(":feature:search"))
            implementation(project(":feature:results"))
            implementation(project(":feature:filters"))
            implementation(project(":feature:details"))
            implementation(project(":feature:checkout"))
            implementation(project(":feature:map"))
            implementation(project(":feature:saved"))
            implementation(project(":feature:bookings"))
            // The shell: theme, tab bar, navigation host, image loading.
            implementation(project(":core:designsystem"))
            implementation(project(":core:ui"))
            implementation(project(":core:navigation"))
            // initKoin returns Koin's KoinApplication and takes its KoinAppDeclaration.
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.savedstate)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
            implementation(libs.navigation3.runtime)
            implementation(libs.navigation3.ui)
            implementation(libs.coil.compose)
            implementation(libs.coil.networkKtor3)
        }
        named("androidHostTest").dependencies {
            // Koin's verify() (JVM): SojournModulesTest checks the whole graph.
            implementation(libs.koin.test)
            // SojournNavHostTest composes the navigation host under Robolectric, with fakes behind the screens.
            implementation(project(":core:testing"))
            implementation(project(":core:model"))
            implementation(libs.koin.coreViewmodel)
            // SojournGraphResolvesTest starts the real graph with the Robolectric application Context.
            implementation(libs.koin.android)
            implementation(libs.kotlinx.datetime)
            implementation(libs.compose.uiTestJunit4)
            implementation(libs.compose.uiTestManifest)
            implementation(libs.robolectric)
            implementation(libs.robolectric.annotations)
            implementation(libs.robolectric.shadowsFramework) // RuntimeEnvironment.getApplication()
        }
    }
}

compose.resources {
    packageOfResClass = "com.abulubad.sojourn.app.shared.resources"
}
