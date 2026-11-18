plugins {
    id("sojourn.kmp.library")
    id("sojourn.koin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Use cases speak model, AppResult and Loadable types, take repository interfaces in their constructors
            // and return Flows; domainModule is a Koin Module.
            api(project(":core:model"))
            api(project(":core:common"))
            api(project(":core:data"))
            api(libs.kotlinx.coroutines.core)
            api(libs.koin.core)
            // StayRules.hasFreeRun takes a YearMonth and a LocalDate.
            api(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
