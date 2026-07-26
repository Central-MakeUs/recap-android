package com.chalkak.recap.core.design.component.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SearchHighlightTextTest {
    @Test
    fun `findFirstHighlightRange returns first mark span`() {
        assertEquals(3..4, findFirstHighlightRange("제주 <mark>숙소</mark> 예약"))
        assertEquals(0..2, findFirstHighlightRange("<mark>파스타</mark> 레시피"))
    }

    @Test
    fun `findFirstHighlightRange ignores later mark spans`() {
        assertEquals(0..1, findFirstHighlightRange("<mark>제주</mark> <mark>숙소</mark>"))
    }

    @Test
    fun `findFirstHighlightRange returns null without mark`() {
        assertNull(findFirstHighlightRange("제주 숙소 예약"))
        assertNull(findFirstHighlightRange(""))
    }

    @Test
    fun `findFirstHighlightRange returns null for empty mark`() {
        assertNull(findFirstHighlightRange("제주 <mark></mark> 예약"))
    }

    @Test
    fun `toPlainSearchText strips mark tags`() {
        assertEquals("제주 숙소 예약", "제주 <mark>숙소</mark> 예약".toPlainSearchText())
    }
}
