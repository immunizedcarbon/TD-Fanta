package com.tdfanta.game.engine.logic.map

import com.tdfanta.game.util.container.KeyValueStore

class WaveInfo(data: KeyValueStore) {
    private val mWaveReward = data.getInt("waveReward")
    private val mExtend = data.getInt("extend")
    private val mMaxExtend = data.getInt("maxExtend")
    private val mEnemies = ArrayList<EnemyInfo>()

    init {
        for (enemyData in data.getStoreList("enemies")) {
            mEnemies.add(EnemyInfo(enemyData))
        }
    }

    fun getEnemies(): List<EnemyInfo> = mEnemies

    fun getExtend(): Int = mExtend

    fun getMaxExtend(): Int = mMaxExtend

    fun getWaveReward(): Int = mWaveReward
}
