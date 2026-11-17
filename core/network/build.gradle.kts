plugins {
    id("sojourn.kmp.library")
    id("sojourn.koin")
    id("sojourn.catalog")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // SojournApi returns AppResult<model> and KtorSojournApi takes an HttpClient: part of the public API.
            api(project(":core:model"))
            api(project(":core:common"))
            api(libs.ktor.client.core)
            // networkModule is a Koin Module; the catalogue DTOs are public @Serializable types.
            api(libs.koin.core)
            api(libs.kotlinx.serialization.core)
            implementation(libs.ktor.http)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
