package com.tdfanta.game.util

object RandomUtils {
    private val random = java.util.Random()

    @JvmStatic
    fun next(max: Int): Int = random.nextInt(max)

    @JvmStatic
    fun next(min: Int, max: Int): Int = random.nextInt(max - min) + min

    @JvmStatic
    fun next(max: Float): Float = random.nextFloat() * max

    @JvmStatic
    fun next(min: Float, max: Float): Float = random.nextFloat() * (max - min) + min
}
