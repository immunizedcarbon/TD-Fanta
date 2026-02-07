package com.tdfanta.game.entity.tower

class TowerInfoValue(
    private val mTextId: Int,
    private val mValue: Float,
) {
    fun getTextId(): Int = mTextId

    fun getValue(): Float = mValue
}
