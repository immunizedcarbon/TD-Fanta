package com.tdfanta.game.business.wave

import com.tdfanta.game.business.game.ScoreBoard
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.EntityRegistry
import com.tdfanta.game.engine.logic.map.EnemyInfo
import com.tdfanta.game.engine.logic.map.MapPath
import com.tdfanta.game.engine.logic.map.WaveInfo
import com.tdfanta.game.entity.EntityTypes
import com.tdfanta.game.entity.enemy.Enemy
import com.tdfanta.game.util.container.KeyValueStore
import com.tdfanta.game.util.math.MathUtils
import com.tdfanta.game.util.math.Vector2

internal class WaveAttender(
    private val mGameEngine: GameEngine,
    private val mScoreBoard: ScoreBoard,
    private val mEntityRegistry: EntityRegistry,
    private val mWaveManager: WaveManager,
    private val mWaveInfo: WaveInfo,
    private val mPaths: List<MapPath>,
    private val mWaveNumber: Int,
) : Enemy.Listener {
    private val mRemainingEnemies = ArrayList<Enemy>()

    private var mWaveStartTickCount = 0

    private var mExtend = 1
    private var mWaveReward: Int = mWaveInfo.getWaveReward()
    private var mEnemyHealthModifier = 1f
    private var mEnemyRewardModifier = 1f

    fun getWaveDefaultHealth(enemyDefaultHealth: EnemyDefaultHealth): Float {
        var waveHealth = 0f
        for (d: EnemyInfo in mWaveInfo.getEnemies()) {
            waveHealth += enemyDefaultHealth.getDefaultHealth(d.getName())
        }
        waveHealth *= (mExtend + 1).toFloat()
        return waveHealth
    }

    fun getWaveReward(): Int = mWaveReward

    fun setExtend(extend: Int) {
        mExtend = extend
    }

    fun modifyEnemyHealth(modifier: Float) {
        mEnemyHealthModifier *= modifier
    }

    fun modifyEnemyReward(modifier: Float) {
        mEnemyRewardModifier *= modifier
    }

    fun modifyWaveReward(modifier: Float) {
        mWaveReward = (mWaveReward.toFloat() * modifier).toInt()
    }

    fun start() {
        if (mWaveStartTickCount == 0) {
            mWaveStartTickCount = mGameEngine.getTickCount()
        }

        scheduleEnemies()
    }

    fun giveWaveReward() {
        mScoreBoard.giveCredits(mWaveReward, true)
        mWaveReward = 0
    }

    fun getRemainingEnemiesCount(): Int = mRemainingEnemies.size

    fun getWaveStartTickCount(): Int = mWaveStartTickCount

    fun getRemainingEnemiesReward(): Float {
        var totalReward = 0f

        for (enemy in mRemainingEnemies) {
            totalReward += enemy.getReward()
        }

        return totalReward
    }

    fun writeActiveWaveData(): KeyValueStore {
        val data = KeyValueStore()
        data.putInt("waveNumber", mWaveNumber)
        data.putInt("waveStartTickCount", mWaveStartTickCount)
        data.putInt("extend", mExtend)
        data.putInt("waveReward", mWaveReward)
        data.putFloat("enemyHealthModifier", mEnemyHealthModifier)
        data.putFloat("enemyRewardModifier", mEnemyRewardModifier)
        return data
    }

    fun readActiveWaveData(data: KeyValueStore) {
        mExtend = data.getInt("extend")
        mWaveReward = data.getInt("waveReward")
        mEnemyHealthModifier = data.getFloat("enemyHealthModifier")
        mEnemyRewardModifier = data.getFloat("enemyRewardModifier")
        mWaveStartTickCount = data.getInt("waveStartTickCount")

        val enemyIterator = mGameEngine.getEntitiesByType(EntityTypes.ENEMY).cast(Enemy::class.java)
        while (enemyIterator.hasNext()) {
            val enemy = enemyIterator.next()

            if (enemy.getWaveNumber() == mWaveNumber) {
                mRemainingEnemies.add(enemy)
                enemy.addListener(this)
            }
        }
    }

    private fun scheduleEnemies() {
        var delayTicks = mWaveStartTickCount - mGameEngine.getTickCount()
        var offset = 0f

        val enemyInfos = mWaveInfo.getEnemies()

        for (extendIndex in 0..mExtend) {
            for (enemyIndex in enemyInfos.indices) {
                val info = enemyInfos[enemyIndex]

                if (MathUtils.equals(info.getDelay(), 0f, 0.1f)) {
                    offset += info.getOffset()
                } else {
                    offset = info.getOffset()
                }

                if (enemyIndex > 0 || extendIndex > 0) {
                    delayTicks += kotlin.math.round(info.getDelay() * GameEngine.TARGET_FRAME_RATE).toInt()
                }

                if (delayTicks >= 0) {
                    val enemy = createAndConfigureEnemy(info, offset)
                    addEnemy(enemy, delayTicks)
                }
            }
        }
    }

    private fun createAndConfigureEnemy(info: EnemyInfo, offset: Float): Enemy {
        val path = mPaths[info.getPathIndex()]
        val enemy = mEntityRegistry.createEntity(info.getName()) as Enemy
        enemy.setWaveNumber(mWaveNumber)
        enemy.modifyHealth(mEnemyHealthModifier)
        enemy.modifyReward(mEnemyRewardModifier)
        enemy.setupPath(path.getWayPoints())

        val startPosition = path.getWayPoints()[0]
        val startDirection = startPosition.directionTo(path.getWayPoints()[1])
        enemy.setPosition(Vector2.mul(startDirection, -offset).add(startPosition))

        return enemy
    }

    private fun addEnemy(enemy: Enemy, delayTicks: Int) {
        mRemainingEnemies.add(enemy)
        enemy.addListener(this)

        mGameEngine.postAfterTicks({ mGameEngine.add(enemy) }, delayTicks)
    }

    override fun enemyKilled(enemy: Enemy) {
        mScoreBoard.giveCredits(enemy.getReward(), true)
    }

    override fun enemyFinished(enemy: Enemy) {
        mScoreBoard.takeLives(1)
    }

    override fun enemyRemoved(enemy: Enemy) {
        mRemainingEnemies.remove(enemy)
        mWaveManager.enemyRemoved()

        if (getRemainingEnemiesCount() == 0) {
            giveWaveReward()
            mWaveManager.waveFinished(this)
        }
    }
}
