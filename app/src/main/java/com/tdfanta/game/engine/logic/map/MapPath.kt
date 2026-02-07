package com.tdfanta.game.engine.logic.map

import com.tdfanta.game.util.math.Vector2

class MapPath(private val mWayPoints: List<Vector2>) {
    fun getWayPoints(): List<Vector2> = mWayPoints
}
