// The Android application shell (app:android): edge-to-edge Activity hosting the shared UI.
import com.abulubad.sojourn.buildlogic.libs
import com.abulubad.sojourn.buildlogic.version
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("sojourn.detekt")
    id("com.autonomousapps.dependency-analysis")
}

android {
    compileSdk = libs.version("android-compileSdk").toInt()

    defaultConfig {
        minSdk = libs.version("android-minSdk").toInt()
        targetSdk = libs.version("android-targetSdk").toInt()
    }

    buildTypes {
        release {
            // R8 on; keep rules live in proguard-rules.pro and are completed in the release phase.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}
