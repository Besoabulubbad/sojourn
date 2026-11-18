package com.abulubad.sojourn

import android.content.Context
import com.abulubad.sojourn.core.navigation.FiltersKey
import com.abulubad.sojourn.core.navigation.GalleryKey
import com.abulubad.sojourn.core.navigation.ResultsKey
import com.abulubad.sojourn.core.navigation.SearchKey
import com.abulubad.sojourn.core.navigation.StayDetailsKey
import com.abulubad.sojourn.feature.details.DetailsViewModel
import com.abulubad.sojourn.feature.details.GalleryViewModel
import com.abulubad.sojourn.feature.filters.FiltersViewModel
import com.abulubad.sojourn.feature.results.ResultsViewModel
import com.abulubad.sojourn.feature.search.SearchViewModel
import org.koin.dsl.module
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
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
            // Passed with parametersOf when the screen opens: the key it was opened with.
            injections = injectedParameters(
                definition<SearchViewModel>(SearchKey::class),
                definition<ResultsViewModel>(ResultsKey::class),
                definition<FiltersViewModel>(FiltersKey::class),
                definition<DetailsViewModel>(StayDetailsKey::class),
                definition<GalleryViewModel>(GalleryKey::class),
            ),
        )
    }
}
