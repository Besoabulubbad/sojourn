import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("sojourn.kmp.library")
    id("sojourn.koin")
}

kotlin {
    // SQLDelight's native driver needs the system SQLite; its Gradle plugin links it only where it is applied
    // (core:database), so this module's iOS test binaries link it themselves.
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.all { linkerOpts("-lsqlite3") }
    }
    sourceSets {
        commonMain.dependencies {
            // Repository interfaces speak model and AppResult types and return Flows; dataModule is a Koin Module.
            api(project(":core:model"))
            api(project(":core:common"))
            api(libs.kotlinx.coroutines.core)
            api(libs.koin.core)
            implementation(project(":core:database"))
            implementation(project(":core:network"))
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.multiplatformSettings)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.sqldelight.runtime)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.multiplatformSettings.test)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        // Tests run the repositories over a real in-memory SojournDatabase, on each test target's driver.
        named("androidHostTest").dependencies {
            implementation(libs.sqldelight.sqliteDriver)
        }
        named("androidDeviceTest").dependencies {
            implementation(libs.sqldelight.androidDriver)
            implementation(libs.androidx.sqlite)
            implementation(libs.androidx.testMonitor)
        }
        iosTest.dependencies {
            implementation(libs.sqldelight.nativeDriver)
        }
    }
}
