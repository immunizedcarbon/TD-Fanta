package com.tdfanta.game.engine.logic.map

import com.tdfanta.game.util.container.KeyValueStore

class GameMap(data: KeyValueStore) {
    private val mWidth = data.getInt("width")
    private val mHeight = data.getInt("height")
    private val mPlateaus = ArrayList<PlateauInfo>()
    private val mPaths = ArrayList<MapPath>()

    init {
        for (plateauData in data.getStoreList("plateaus")) {
            mPlateaus.add(PlateauInfo(plateauData))
        }

        for (pathData in data.getStoreList("paths")) {
            mPaths.add(MapPath(pathData.getVectorList("wayPoints")))
        }
    }

    fun getHeight(): Int = mHeight

    fun getWidth(): Int = mWidth

    fun getPlateaus(): Collection<PlateauInfo> = mPlateaus

    fun getPaths(): List<MapPath> = mPaths
}
