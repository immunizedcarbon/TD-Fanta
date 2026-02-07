package com.tdfanta.game.util.iterator

import org.junit.Assert.assertEquals
import org.junit.Test

class StreamIteratorTest {
    @Test
    fun filterByObject_removesMatchingElements() {
        val values = StreamIterator
            .fromArray(arrayOf(1, 2, 3, 2))
            .filter(2)
            .toList()

        assertEquals(listOf(1, 3), values)
    }

    @Test
    fun firstAndLast_returnExpectedValues() {
        val first = StreamIterator.fromArray(arrayOf(4, 5, 6)).first()
        val last = StreamIterator.fromArray(arrayOf(4, 5, 6)).last()

        assertEquals(4, first)
        assertEquals(6, last)
    }
}
