package com.tdfanta.game.business.game

import com.tdfanta.game.GameSettings
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.persistence.Persister
import com.tdfanta.game.util.container.KeyValueStore
import java.util.concurrent.CopyOnWriteArrayList

class ScoreBoard(private val mGameEngine: GameEngine) : Persister {
    interface Listener {
        fun creditsChanged(credits: Int)

        fun bonusChanged(waveBonus: Int, earlyBonus: Int)

        fun livesChanged(lives: Int)
    }

    private var mCredits = 0
    private var mCreditsEarned = 0
    private var mLives = 0
    private var mEarlyBonus = 0
    private var mWaveBonus = 0

    private val mListeners = CopyOnWriteArrayList<Listener>()

    fun takeLives(lives: Int) {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { takeLives(lives) }
            return
        }

        mLives -= lives
        livesChanged()
    }

    fun giveCredits(credits: Int, earned: Boolean) {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { giveCredits(credits, earned) }
            return
        }

        mCredits += credits

        if (earned) {
            mCreditsEarned += credits
        }

        creditsChanged()
    }

    fun takeCredits(credits: Int) {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { takeCredits(credits) }
            return
        }

        mCredits -= credits
        creditsChanged()
    }

    fun setEarlyBonus(earlyBonus: Int) {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { setEarlyBonus(earlyBonus) }
            return
        }

        mEarlyBonus = earlyBonus
        bonusChanged()
    }

    fun setWaveBonus(waveBonus: Int) {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { setWaveBonus(waveBonus) }
            return
        }

        mWaveBonus = waveBonus
        bonusChanged()
    }

    fun getCredits(): Int = mCredits

    fun getCreditsEarned(): Int = mCreditsEarned

    fun getScore(): Int = mCreditsEarned

    fun getLives(): Int = mLives

    fun getEarlyBonus(): Int = mEarlyBonus

    fun getWaveBonus(): Int = mWaveBonus

    fun addListener(listener: Listener) {
        mListeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        mListeners.remove(listener)
    }

    private fun bonusChanged() {
        for (listener in mListeners) {
            listener.bonusChanged(mWaveBonus, mEarlyBonus)
        }
    }

    private fun creditsChanged() {
        for (listener in mListeners) {
            listener.creditsChanged(mCredits)
        }
    }

    private fun livesChanged() {
        for (listener in mListeners) {
            listener.livesChanged(mLives)
        }
    }

    override fun resetState() {
        mLives = GameSettings.START_LIVES
        mCredits = GameSettings.START_CREDITS
        mCreditsEarned = 0

        creditsChanged()
        livesChanged()
    }

    override fun writeState(gameState: KeyValueStore) {
        gameState.putInt("lives", mLives)
        gameState.putInt("credits", mCredits)
        gameState.putInt("creditsEarned", mCreditsEarned)
    }

    override fun readState(gameState: KeyValueStore) {
        mLives = gameState.getInt("lives")
        mCredits = gameState.getInt("credits")
        mCreditsEarned = gameState.getInt("creditsEarned")

        creditsChanged()
        livesChanged()
    }
}
