// SQLDelight for core:database (ADR 0007): the SojournDatabase schema, drivers per target,
// a JVM SQLite driver for host tests.
import com.abulubad.sojourn.buildlogic.libs
import com.abulubad.sojourn.buildlogic.library

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("app.cash.sqldelight")
}

sqldelight {
    databases {
        create("SojournDatabase") {
            packageName.set("com.abulubad.sojourn.core.database")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/schema"))
            verifyMigrations.set(true)
        }
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.library("sqldelight-runtime"))
        }
        androidMain.dependencies {
            implementation(libs.library("sqldelight-androidDriver"))
            // The driver's open callback takes SupportSQLiteDatabase (foreign keys on).
            implementation(libs.library("androidx-sqlite"))
        }
        iosMain.dependencies {
            implementation(libs.library("sqldelight-nativeDriver"))
        }
        // Created by the Android target's withHostTest {}; match lazily so plugin order does not matter.
        matching { it.name == "androidHostTest" }.configureEach {
            dependencies {
                implementation(libs.library("sqldelight-sqliteDriver"))
            }
        }
        // Device tests share commonTest; their in-memory driver is the Android one on the instrumentation context.
        matching { it.name == "androidDeviceTest" }.configureEach {
            dependencies {
                implementation(libs.library("sqldelight-androidDriver"))
                implementation(libs.library("androidx-testMonitor"))
            }
        }
    }
}
