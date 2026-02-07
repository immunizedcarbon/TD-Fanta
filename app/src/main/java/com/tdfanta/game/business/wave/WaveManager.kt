package com.tdfanta.game.business.wave

import android.util.Log
import com.tdfanta.game.GameSettings
import com.tdfanta.game.business.game.GameState
import com.tdfanta.game.business.game.ScoreBoard
import com.tdfanta.game.business.tower.TowerAging
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.EntityRegistry
import com.tdfanta.game.engine.logic.map.MapPath
import com.tdfanta.game.engine.logic.map.WaveInfo
import com.tdfanta.game.engine.logic.persistence.Persister
import com.tdfanta.game.util.container.KeyValueStore
import java.util.concurrent.CopyOnWriteArrayList

class WaveManager(
    private val mGameEngine: GameEngine,
    private val mScoreBoard: ScoreBoard,
    private val mGameState: GameState,
    private val mEntityRegistry: EntityRegistry,
    private val mTowerAging: TowerAging,
) : Persister, GameState.Listener {
    interface Listener {
        fun waveStarted()

        fun waveNumberChanged()

        fun nextWaveReadyChanged()

        fun remainingEnemiesCountChanged()
    }

    private val mEnemyDefaultHealth = EnemyDefaultHealth(mEntityRegistry)

    private var mWaveNumber = 0
    private var mRemainingEnemiesCount = 0
    private var mNextWaveReady = false

    private val mActiveWaves = ArrayList<WaveAttender>()
    private val mListeners = CopyOnWriteArrayList<Listener>()

    init {
        mGameState.addListener(this)
    }

    fun getWaveNumber(): Int = mWaveNumber

    fun isNextWaveReady(): Boolean = mNextWaveReady

    fun getRemainingEnemiesCount(): Int = mRemainingEnemiesCount

    fun startNextWave() {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { startNextWave() }
            return
        }

        if (!mNextWaveReady) {
            return
        }

        setNextWaveReady(false)
        nextWaveReadyDelayed(NEXT_WAVE_MIN_DELAY)

        mGameState.gameStarted()

        giveWaveRewardAndEarlyBonus()
        createAndStartWaveAttender()
        updateBonusOnScoreBoard()
        updateRemainingEnemiesCount()

        setWaveNumber(mWaveNumber + 1)

        for (listener in mListeners) {
            listener.waveStarted()
        }
    }

    fun addListener(listener: Listener) {
        mListeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        mListeners.remove(listener)
    }

    override fun resetState() {
        setWaveNumber(0)
        mActiveWaves.clear()
        setNextWaveReady(true)
    }

    override fun writeState(gameState: KeyValueStore) {
        gameState.putInt("waveNumber", mWaveNumber)

        for (waveAttender in mActiveWaves) {
            gameState.appendStore("activeWaves", waveAttender.writeActiveWaveData())
        }
    }

    override fun readState(gameState: KeyValueStore) {
        initializeActiveWaves(gameState)
        initializeNextWaveReady()
        setWaveNumber(gameState.getInt("waveNumber"))
        updateRemainingEnemiesCount()
    }

    override fun gameRestart() {
    }

    override fun gameOver() {
        setNextWaveReady(false)
    }

    internal fun enemyRemoved() {
        updateBonusOnScoreBoard()
        updateRemainingEnemiesCount()
    }

    internal fun waveFinished(waveAttender: WaveAttender) {
        mActiveWaves.remove(waveAttender)

        mTowerAging.ageTowers()
        updateBonusOnScoreBoard()
    }

    private fun initializeActiveWaves(gameState: KeyValueStore) {
        mActiveWaves.clear()

        for (activeWaveData in gameState.getStoreList("activeWaves")) {
            val waveInfos = mGameEngine.getWaveInfos()!!
            val waveInfo = waveInfos[activeWaveData.getInt("waveNumber") % waveInfos.size]
            val paths = mGameEngine.getGameMap()!!.getPaths()
            val waveAttender = WaveAttender(
                mGameEngine,
                mScoreBoard,
                mEntityRegistry,
                this,
                waveInfo,
                paths,
                activeWaveData.getInt("waveNumber"),
            )
            waveAttender.readActiveWaveData(activeWaveData)
            waveAttender.start()
            mActiveWaves.add(waveAttender)
        }
    }

    private fun initializeNextWaveReady() {
        if (mGameState.isGameOver()) {
            setNextWaveReady(false)
            return
        }

        val minWaveDelayTicks = kotlin.math.round(NEXT_WAVE_MIN_DELAY * GameEngine.TARGET_FRAME_RATE).toInt()
        var lastStartedWaveTickCount = -minWaveDelayTicks

        for (wave in mActiveWaves) {
            lastStartedWaveTickCount = maxOf(lastStartedWaveTickCount, wave.getWaveStartTickCount())
        }

        val nextWaveReadyTicks = minWaveDelayTicks - (mGameEngine.getTickCount() - lastStartedWaveTickCount)
        val nextWaveReadyDelay = nextWaveReadyTicks.toFloat() / GameEngine.TARGET_FRAME_RATE

        if (nextWaveReadyDelay > 0f) {
            setNextWaveReady(false)
            nextWaveReadyDelayed(nextWaveReadyDelay)
        } else {
            setNextWaveReady(true)
        }
    }

    private fun giveWaveRewardAndEarlyBonus() {
        val currentWave = getCurrentWave()

        if (currentWave != null) {
            currentWave.giveWaveReward()
            mScoreBoard.giveCredits(getEarlyBonus(), false)
        }
    }

    private fun nextWaveReadyDelayed(delay: Float) {
        mGameEngine.postDelayed(
            {
                if (!mGameState.isGameOver()) {
                    setNextWaveReady(true)
                }
            },
            delay,
        )
    }

    private fun updateBonusOnScoreBoard() {
        mScoreBoard.setEarlyBonus(getEarlyBonus())

        val currentWave = getCurrentWave()
        if (currentWave != null) {
            mScoreBoard.setWaveBonus(currentWave.getWaveReward())
        } else {
            mScoreBoard.setWaveBonus(0)
        }
    }

    private fun updateRemainingEnemiesCount() {
        var totalCount = 0

        for (waveAttender in mActiveWaves) {
            totalCount += waveAttender.getRemainingEnemiesCount()
        }

        if (mRemainingEnemiesCount != totalCount) {
            mRemainingEnemiesCount = totalCount

            for (listener in mListeners) {
                listener.remainingEnemiesCountChanged()
            }
        }
    }

    private fun createAndStartWaveAttender() {
        val waveInfos: List<WaveInfo> = mGameEngine.getWaveInfos()!!
        val nextWaveInfo = waveInfos[mWaveNumber % waveInfos.size]
        val paths: List<MapPath> = mGameEngine.getGameMap()!!.getPaths()
        val nextWave = WaveAttender(mGameEngine, mScoreBoard, mEntityRegistry, this, nextWaveInfo, paths, mWaveNumber)
        updateWaveExtend(nextWave, nextWaveInfo)
        updateWaveModifiers(nextWave)
        nextWave.start()
        mActiveWaves.add(nextWave)
    }

    private fun updateWaveExtend(wave: WaveAttender, waveInfo: WaveInfo) {
        val extend = minOf((getIterationNumber() - 1) * waveInfo.getExtend(), waveInfo.getMaxExtend())
        wave.setExtend(extend)
    }

    private fun updateWaveModifiers(wave: WaveAttender) {
        val waveHealth = wave.getWaveDefaultHealth(this.mEnemyDefaultHealth)
        val damagePossible = (
            GameSettings.DIFFICULTY_LINEAR * mScoreBoard.getCreditsEarned() +
                GameSettings.DIFFICULTY_MODIFIER *
                Math.pow(
                    mScoreBoard.getCreditsEarned().toDouble(),
                    GameSettings.DIFFICULTY_EXPONENT.toDouble(),
                )
            ).toFloat()
        var healthModifier = damagePossible / waveHealth
        healthModifier = maxOf(healthModifier, GameSettings.MIN_HEALTH_MODIFIER)

        var rewardModifier = (
            GameSettings.REWARD_MODIFIER *
                Math.pow(healthModifier.toDouble(), GameSettings.REWARD_EXPONENT.toDouble())
            ).toFloat()
        rewardModifier = maxOf(rewardModifier, GameSettings.MIN_REWARD_MODIFIER)

        wave.modifyEnemyHealth(healthModifier)
        wave.modifyEnemyReward(rewardModifier)
        wave.modifyWaveReward(getIterationNumber().toFloat())

        Log.i(TAG, String.format("waveNumber=%d", mWaveNumber))
        Log.i(TAG, String.format("waveHealth=%f", waveHealth))
        Log.i(TAG, String.format("creditsEarned=%d", mScoreBoard.getCreditsEarned()))
        Log.i(TAG, String.format("damagePossible=%f", damagePossible))
        Log.i(TAG, String.format("healthModifier=%f", healthModifier))
        Log.i(TAG, String.format("rewardModifier=%f", rewardModifier))
    }

    private fun getIterationNumber(): Int = (mWaveNumber / mGameEngine.getWaveInfos()!!.size) + 1

    private fun getEarlyBonus(): Int {
        var remainingReward = 0f

        for (wave in mActiveWaves) {
            remainingReward += wave.getRemainingEnemiesReward()
        }

        return kotlin.math.round(
            GameSettings.EARLY_BONUS_MODIFIER *
                Math.pow(remainingReward.toDouble(), GameSettings.EARLY_BONUS_EXPONENT.toDouble()).toFloat(),
        ).toInt()
    }

    private fun getCurrentWave(): WaveAttender? {
        if (mActiveWaves.isEmpty()) {
            return null
        }

        return mActiveWaves[mActiveWaves.size - 1]
    }

    private fun setWaveNumber(waveNumber: Int) {
        if (mWaveNumber != waveNumber) {
            mWaveNumber = waveNumber

            for (listener in mListeners) {
                listener.waveNumberChanged()
            }
        }
    }

    private fun setNextWaveReady(ready: Boolean) {
        if (mNextWaveReady != ready) {
            mNextWaveReady = ready

            for (listener in mListeners) {
                listener.nextWaveReadyChanged()
            }
        }
    }

    companion object {
        private val TAG = WaveManager::class.java.simpleName

        private const val NEXT_WAVE_MIN_DELAY = 5f
    }
}
