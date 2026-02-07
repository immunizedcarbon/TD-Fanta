package com.tdfanta.game.util.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MathUtilsTest {
    @Test
    fun normalizeAngle_wrapsToMinus180To180Range() {
        assertEquals(-170f, MathUtils.normalizeAngle(190f))
        assertEquals(170f, MathUtils.normalizeAngle(-190f))
        assertEquals(45f, MathUtils.normalizeAngle(45f))
    }

    @Test
    fun radiansAndDegrees_areInverseWithinTolerance() {
        val degrees = 123.45f
        val radians = MathUtils.toRadians(degrees)
        val convertedBack = MathUtils.toDegrees(radians)

        assertTrue(MathUtils.equals(degrees, convertedBack, 0.0001f))
    }
}
