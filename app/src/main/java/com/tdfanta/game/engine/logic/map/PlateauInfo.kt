package com.tdfanta.game.engine.logic.map

import com.tdfanta.game.util.container.KeyValueStore
import com.tdfanta.game.util.math.Vector2

class PlateauInfo(data: KeyValueStore) {
    private val mName: String = data.getString("name")
    private val mPosition = Vector2(
        data.getFloat("x"),
        data.getFloat("y"),
    )

    fun getName(): String = mName

    fun getPosition(): Vector2 = mPosition
}
