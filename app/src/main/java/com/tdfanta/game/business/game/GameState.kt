package com.tdfanta.game.business.game

import com.tdfanta.game.business.tower.TowerSelector
import com.tdfanta.game.engine.logic.persistence.Persister
import com.tdfanta.game.util.container.KeyValueStore
import java.util.concurrent.CopyOnWriteArrayList

class GameState(
    private val mScoreBoard: ScoreBoard,
    private val mHighScores: HighScores,
    private val mTowerSelector: TowerSelector,
) : ScoreBoard.Listener, Persister {
    interface Listener {
        fun gameRestart()

        fun gameOver()
    }

    private var mGameOver = false
    private var mGameStarted = false
    private var mFinalScore = 0

    private val mListeners = CopyOnWriteArrayList<Listener>()

    init {
        mScoreBoard.addListener(this)
    }

    fun isGameOver(): Boolean = mGameOver

    fun isGameStarted(): Boolean = !mGameOver && mGameStarted

    fun getFinalScore(): Int = mFinalScore

    fun addListener(listener: Listener) {
        mListeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        mListeners.remove(listener)
    }

    fun gameStarted() {
        mGameStarted = true
    }

    override fun livesChanged(lives: Int) {
        if (!mGameOver && mScoreBoard.getLives() < 0) {
            setGameOver(true)
        }
    }

    override fun creditsChanged(credits: Int) {
    }

    override fun bonusChanged(waveBonus: Int, earlyBonus: Int) {
    }

    override fun resetState() {
        setGameOver(false)
        mGameStarted = false
    }

    override fun writeState(gameState: KeyValueStore) {
        gameState.putInt("finalScore", mFinalScore)
    }

    override fun readState(gameState: KeyValueStore) {
        setGameOver(gameState.getInt("lives") < 0)
        mGameStarted = gameState.getInt("waveNumber") > 0
        mFinalScore = gameState.getInt("finalScore")
    }

    private fun setGameOver(gameOver: Boolean) {
        mGameOver = gameOver

        if (gameOver) {
            mHighScores.updateHighScore()
            mFinalScore = mScoreBoard.getScore()
            mTowerSelector.setControlsEnabled(false)

            for (listener in mListeners) {
                listener.gameOver()
            }
        }

        if (!gameOver) {
            mGameStarted = false
            mFinalScore = 0
            mTowerSelector.setControlsEnabled(true)

            for (listener in mListeners) {
                listener.gameRestart()
            }
        }
    }
}
