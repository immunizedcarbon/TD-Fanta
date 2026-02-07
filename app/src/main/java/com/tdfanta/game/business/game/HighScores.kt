package com.tdfanta.game.business.game

import android.content.Context
import android.content.SharedPreferences
import com.tdfanta.game.engine.logic.GameEngine

class HighScores(
    context: Context,
    private val mGameEngine: GameEngine,
    private val mScoreBoard: ScoreBoard,
    private val mGameLoader: GameLoader,
) {
    private val mHighScores: SharedPreferences =
        context.getSharedPreferences("high_scores", Context.MODE_PRIVATE)

    fun getHighScore(mapId: String): Int = mHighScores.getInt(mapId, 0)

    fun updateHighScore() {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { updateHighScore() }
            return
        }

        val mapId = mGameLoader.getCurrentMapId()
        val highScore = getHighScore(mapId)
        val score = mScoreBoard.getScore()

        if (score > highScore) {
            mHighScores.edit().putInt(mapId, score).apply()
        }
    }

    fun clearHighScores() {
        mHighScores.edit()
            .clear()
            .apply()
    }
}
