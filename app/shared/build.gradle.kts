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
            implementation(project(":core:model"))
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:ui"))
            implementation(project(":core:navigation"))
            implementation(project(":core:platform"))
            implementation(project(":core:database"))
            implementation(project(":core:network"))
            implementation(project(":core:data"))
            implementation(project(":core:domain"))

            implementation(project(":feature:explore"))
            implementation(project(":feature:search"))
            implementation(project(":feature:results"))
            implementation(project(":feature:filters"))
            implementation(project(":feature:details"))
            implementation(project(":feature:checkout"))
            implementation(project(":feature:map"))
            implementation(project(":feature:saved"))
            implementation(project(":feature:bookings"))
            implementation(project(":feature:profile"))

            implementation(libs.compose.material3)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewmodel)
            implementation(libs.koin.composeNavigation3)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation3.ui)
            implementation(libs.navigation3.runtime)
        }
        named("androidHostTest").dependencies {
            // Koin's verify() (JVM): SojournModulesTest checks the whole graph.
            implementation(libs.koin.test)
        }
        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.testRunner)
        }
    }
}

compose.resources {
    packageOfResClass = "com.abulubad.sojourn.app.shared.resources"
}
