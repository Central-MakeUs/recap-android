package com.chalkak.recap.core.design.component.text

private val MarkOpenRegex = Regex("<mark\\b[^>]*>", RegexOption.IGNORE_CASE)
private val MarkCloseRegex = Regex("</mark>", RegexOption.IGNORE_CASE)
private val MarkTagRegex = Regex("</?mark\\b[^>]*>", RegexOption.IGNORE_CASE)

/**
 * Finds the first `<mark>...</mark>` span in [highlightedText] and returns its inclusive
 * [IntRange] in the corresponding plain text (tags stripped). Returns null when absent.
 */
fun findFirstHighlightRange(highlightedText: String): IntRange? {
    val open = MarkOpenRegex.find(highlightedText) ?: return null
    val afterOpen = open.range.last + 1
    val close = MarkCloseRegex.find(highlightedText, afterOpen) ?: return null
    val marked = highlightedText.substring(afterOpen, close.range.first)
    if (marked.isEmpty()) {
        return null
    }
    val plainBefore = highlightedText.substring(0, open.range.first).toPlainSearchText()
    val plainMarked = marked.toPlainSearchText()
    if (plainMarked.isEmpty()) {
        return null
    }
    val start = plainBefore.length
    return start..<start + plainMarked.length
}

fun String.toPlainSearchText(): String = replace(MarkTagRegex, "")
