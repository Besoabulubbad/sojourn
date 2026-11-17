package com.abulubad.sojourn.buildlogic.catalog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogEmbeddingTest {

    @Test
    fun `escapes everything a Kotlin string literal treats specially`() {
        assertEquals("""a\\b\"c\${'$'}d\ne""", CatalogEmbedding.escape("a\\b\"c\$d\ne"))
    }

    @Test
    fun `long text is split into chunks that each stay well under the JVM constant limit`() {
        val text = "ش".repeat(CatalogEmbedding.CHUNK * 2 + 5)
        val source = CatalogEmbedding.source(text, "x.y")
        assertEquals(3, Regex("append\\(").findAll(source).count())
        assertTrue(source.lines().all { it.toByteArray(Charsets.UTF_8).size < 65_535 })
    }
}
