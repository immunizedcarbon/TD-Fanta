package com.tdfanta.game.business.game

class MapInfo(
    private val mMapId: String,
    private val mMapNameResId: Int,
    private val mMapDataResId: Int,
) {
    fun getMapId(): String = mMapId

    fun getMapNameResId(): Int = mMapNameResId

    fun getMapDataResId(): Int = mMapDataResId
}
