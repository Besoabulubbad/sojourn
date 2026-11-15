// Kotlin Multiplatform library: Android (AGP KMP library plugin) + iOS device and simulator.
// Every core:* and feature:* module starts here (docs/architecture/modules.md).
import com.abulubad.sojourn.buildlogic.libs
import com.abulubad.sojourn.buildlogic.library
import com.abulubad.sojourn.buildlogic.sojournNamespace
import com.abulubad.sojourn.buildlogic.version
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("sojourn.detekt")
    // dependency-analysis 3.x is applied per module (the root only aggregates `buildHealth`).
    id("com.autonomousapps.dependency-analysis")
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = sojournNamespace
        compileSdk = libs.version("android-compileSdk").toInt()
        minSdk = libs.version("android-minSdk").toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.library("kotlin-test"))
        }
    }
}
