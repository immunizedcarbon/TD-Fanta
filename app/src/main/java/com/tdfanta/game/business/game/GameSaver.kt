package com.tdfanta.game.business.game

import android.util.Log
import com.tdfanta.game.business.wave.WaveManager
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.persistence.GamePersister
import com.tdfanta.game.engine.render.Renderer
import com.tdfanta.game.util.container.KeyValueStore
import java.io.File
import java.io.FileOutputStream

class GameSaver(
    private val mGameEngine: GameEngine,
    private val mGameLoader: GameLoader,
    private val mGamePersister: GamePersister,
    private val mRenderer: Renderer,
    private val mWaveManager: WaveManager,
    private val mScoreBoard: ScoreBoard,
    private val mSaveGameRepository: SaveGameRepository,
) {
    fun autoSaveGame() {
        if (mGameEngine.isThreadRunning() && mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { autoSaveGame() }
            return
        }

        saveGameState(mSaveGameRepository.getAutoSaveStateFile())
    }

    fun saveGame(): SaveGameInfo {
        if (mGameEngine.isThreadRunning() && mGameEngine.isThreadChangeNeeded()) {
            throw RuntimeException("This method cannot be used when the game thread is running!")
        }

        val saveGameInfo = mSaveGameRepository.createSaveGame(
            mRenderer.getScreenshot(),
            mScoreBoard.getScore(),
            mWaveManager.getWaveNumber(),
            mScoreBoard.getLives(),
        )

        saveGameState(mSaveGameRepository.getGameStateFile(saveGameInfo))
        return saveGameInfo
    }

    fun saveGameState(stateFile: File) {
        Log.i(TAG, "Saving game...")
        val gameState = KeyValueStore()
        mGamePersister.writeState(gameState)
        gameState.putInt("version", SaveGameMigrator.SAVE_GAME_VERSION)
        gameState.putString("mapId", mGameLoader.getCurrentMapId())

        try {
            val outputStream = FileOutputStream(stateFile, false)
            gameState.toStream(outputStream)
            outputStream.close()
            Log.i(TAG, "Game saved.")
        } catch (e: Exception) {
            throw RuntimeException("Could not save game!", e)
        }
    }

    companion object {
        private val TAG = GameSaver::class.java.simpleName
    }
}
