package com.abulubad.sojourn.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

/** The shared version catalog (`gradle/libs.versions.toml`), for use inside precompiled script plugins. */
val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.version(alias: String): String = findVersion(alias).get().requiredVersion

fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> = findLibrary(alias).get()

/** `:core:designsystem` → `com.abulubad.sojourn.core.designsystem`; the Android namespace of every module. */
val Project.sojournNamespace: String
    get() = "com.abulubad.sojourn" + path.split(':').filter { it.isNotEmpty() }
        .joinToString("") { "." + it.replace("-", "") }
