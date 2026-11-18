package com.abulubad.sojourn.feature.details

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SentenceExcerptTest {

    private val two = "A calm studio by the canal. There is a desk by the window and a full kitchen."

    @Test
    fun `a text that fits is not cut`() {
        assertNull(sentenceExcerpt(two, two.length))
    }

    @Test
    fun `a long text is cut after its last whole sentence within the limit`() {
        assertEquals("A calm studio by the canal.", sentenceExcerpt(two, 40))
    }

    @Test
    fun `no sentence end within the limit shows the whole text rather than half a sentence`() {
        assertNull(sentenceExcerpt(two, 10))
    }

    @Test
    fun `an Arabic question mark ends a sentence and a decimal point does not`() {
        assertEquals("هل تبحث عن الهدوء؟", sentenceExcerpt("هل تبحث عن الهدوء؟ الشقة على بعد 2.5 كم من البحر.", 30))
        assertEquals("Only 2.5 km.", sentenceExcerpt("Only 2.5 km. Then the sea and the long corniche.", 20))
    }
}
