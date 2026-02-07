package com.tdfanta.game.util.math

object MathUtils {
    @JvmStatic
    fun square(x: Float): Float = x * x

    @JvmStatic
    fun sign(x: Float): Float = if (x < 0f) -1f else 1f

    @JvmStatic
    fun equals(x: Float, y: Float, d: Float): Boolean = kotlin.math.abs(x - y) <= d

    @JvmStatic
    fun toRadians(degrees: Float): Float = degrees / 180f * Math.PI.toFloat()

    @JvmStatic
    fun toDegrees(radians: Float): Float = radians / Math.PI.toFloat() * 180f

    @JvmStatic
    fun normalizeAngle(angle: Float): Float {
        var ret = angle % 360f

        if (ret > 180f) {
            ret -= 360f
        } else if (ret < -180f) {
            ret += 360f
        }

        return ret
    }
}
