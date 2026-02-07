package com.tdfanta.game.util.math

class Line(point1: Vector2?, point2: Vector2?) {
    private val mPoint1: Vector2
    private val mPoint2: Vector2

    init {
        require(!(point1 == null || point2 == null))
        mPoint1 = point1
        mPoint2 = point2
    }

    fun getPoint1(): Vector2 = mPoint1

    fun getPoint2(): Vector2 = mPoint2

    fun length(): Float = mPoint1.distanceTo(mPoint2)

    fun angle(): Float = mPoint1.angleTo(mPoint2)

    fun direction(): Vector2 = mPoint1.directionTo(mPoint2)
}
