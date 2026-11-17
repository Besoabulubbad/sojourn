package com.abulubad.sojourn

import android.content.Context
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.Test

/**
 * The Koin graph has no holes (ADR 0004): every definition's constructor dependencies are declared somewhere in
 * [sojournModules]. Uses Koin's `verify()` (JVM), which replaced the deprecated `checkModules()` in Koin 4.0.
 */
class SojournModulesTest {

    @Test
    fun `every dependency in the graph is declared`() {
        module { includes(sojournModules) }.verify(
            // Provided by the platform at start-up, not by a module: app:android registers the application Context.
            extraTypes = listOf(Context::class),
        )
    }
}
