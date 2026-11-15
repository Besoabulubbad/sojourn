rootProject.name = "Sojourn"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// Application shells
include(":app:android")
include(":app:shared")

// Core layers (see docs/architecture/modules.md for the dependency contract)
include(":core:model")
include(":core:common")
include(":core:localization")
include(":core:designsystem")
include(":core:ui")
include(":core:navigation")
include(":core:platform")
include(":core:database")
include(":core:network")
include(":core:data")
include(":core:domain")
include(":core:testing")

// Features — one per screen
include(":feature:explore")
include(":feature:search")
include(":feature:results")
include(":feature:filters")
include(":feature:details")
include(":feature:checkout")
include(":feature:map")
include(":feature:saved")
include(":feature:bookings")
include(":feature:profile")

// Tooling
include(":tooling:konsist")
