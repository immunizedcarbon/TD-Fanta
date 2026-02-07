package com.tdfanta.game.engine.logic.map

import com.tdfanta.game.util.container.KeyValueStore

class EnemyInfo(data: KeyValueStore) {
    private val mName: String = data.getString("name")
    private val mPathIndex: Int = if (data.hasKey("pathIndex")) data.getInt("pathIndex") else 0
    private val mDelay: Float = if (data.hasKey("delay")) data.getFloat("delay") else 0f
    private val mOffset: Float = if (data.hasKey("offset")) data.getFloat("offset") else 0f

    fun getName(): String = mName

    fun getPathIndex(): Int = mPathIndex

    fun getDelay(): Float = mDelay

    fun getOffset(): Float = mOffset
}
