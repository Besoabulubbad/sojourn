plugins {
    id("sojourn.kmp.library")
    id("sojourn.koin")
}

kotlin {
    android {
        // The arrival reminder's notification icon and channel name (src/androidMain/res).
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // GeoPoint, Money and QuoteId appear in the adapter signatures; platformModule is a Koin Module.
            api(project(":core:model"))
            api(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            // AndroidCalendarSaver turns check-in and check-out dates into all-day event times.
            implementation(libs.kotlinx.datetime)
            // Arrival reminders: NotificationCompat and the permission check; ask() waits for the answer.
            implementation(libs.androidx.core)
            implementation(libs.kotlinx.coroutines.core)
        }
        iosMain.dependencies {
            // IosReminders waits for the system's answers.
            implementation(libs.kotlinx.coroutines.core)
        }
        named("androidHostTest").dependencies {
            // AndroidRemindersTest fires the receivers under Robolectric (the real day is weeks away).
            implementation(libs.robolectric)
            implementation(libs.robolectric.annotations)
            implementation(libs.robolectric.shadowsFramework)
            implementation(libs.junit)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
