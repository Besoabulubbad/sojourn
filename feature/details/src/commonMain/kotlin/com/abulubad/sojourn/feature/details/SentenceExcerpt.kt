package com.abulubad.sojourn.feature.details

/**
 * The start of [text] up to the last sentence end within [maxChars], for a description that truncates at a
 * sentence end and never mid-word (ScreenDetails card). Null when the text already fits, or when no sentence ends
 * within the limit: then the whole text shows rather than half a sentence.
 */
internal fun sentenceExcerpt(text: String, maxChars: Int): String? {
    if (text.length <= maxChars) return null
    val end = SENTENCE_END.findAll(text.take(maxChars + 1)).lastOrNull() ?: return null
    return text.substring(0, end.range.first + 1).trimEnd()
}

/** ". ", "! ", "? " and the Arabic question mark, each followed by a space, or the end. */
private val SENTENCE_END = Regex("[.!?؟](?=\\s|$)")
