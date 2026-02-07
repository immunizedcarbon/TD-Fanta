package com.tdfanta.game.util.math

class Vector2 {
    private var x: Float
    private var y: Float

    constructor() {
        x = 0f
        y = 0f
    }

    constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun x(): Float = x

    fun y(): Float = y

    // Like add() but overwrites the source object instead of allocating
    fun add(v: Vector2): Vector2 {
        x += v.x
        y += v.y
        return this
    }

    // Like mul() but overwrites the source object instead of allocating
    fun mul(s: Float): Vector2 {
        x *= s
        y *= s
        return this
    }

    fun div(s: Float): Vector2 = Vector2(x / s, y / s)

    fun dot(v: Vector2): Float = x * v.x + y * v.y

    fun len(): Float = len(x, y)

    fun len2(): Float = x * x + y * y

    fun norm(): Vector2 = div(len())

    fun proj(v: Vector2): Vector2 {
        val factor = dot(v) / v.len2()
        return mul(v, factor)
    }

    fun angle(): Float = angle(x, y)

    // equivalent to v.to(x).len()
    fun distanceTo(v: Vector2): Float = len(v.x - x, v.y - y)

    // equivalent to v.to(x).angle()
    fun angleTo(v: Vector2): Float = angle(v.x - x, v.y - y)

    fun directionTo(v: Vector2): Vector2 {
        val to = to(this, v)
        val length = to.len()
        to.x /= length
        to.y /= length
        return to
    }

    override fun toString(): String = "$x,$y"

    companion object {
        @JvmStatic
        fun polar(length: Float, angle: Float): Vector2 = Vector2(
            kotlin.math.cos(MathUtils.toRadians(angle).toDouble()).toFloat() * length,
            kotlin.math.sin(MathUtils.toRadians(angle).toDouble()).toFloat() * length,
        )

        @JvmStatic
        fun add(a: Vector2, b: Vector2): Vector2 = Vector2(a.x + b.x, a.y + b.y)

        @JvmStatic
        fun sub(a: Vector2, b: Vector2): Vector2 = Vector2(a.x - b.x, a.y - b.y)

        @JvmStatic
        fun to(a: Vector2, b: Vector2): Vector2 = Vector2(b.x - a.x, b.y - a.y)

        @JvmStatic
        fun mul(v: Vector2, s: Float): Vector2 = Vector2(v.x * s, v.y * s)

        private fun len(x: Float, y: Float): Float = kotlin.math.sqrt(x * x + y * y)

        private fun angle(x: Float, y: Float): Float =
            MathUtils.toDegrees(kotlin.math.atan2(y.toDouble(), x.toDouble()).toFloat())
    }
}
