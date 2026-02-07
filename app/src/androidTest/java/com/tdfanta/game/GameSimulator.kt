package com.tdfanta.game

import android.util.Log
import com.tdfanta.game.business.game.GameState
import com.tdfanta.game.business.game.SaveGameInfo
import java.util.concurrent.CountDownLatch

abstract class GameSimulator(private val gameFactory: GameFactory) {
    private lateinit var finishedLatch: CountDownLatch
    private var saveGameInfo: SaveGameInfo? = null

    internal fun startSimulation() {
        finishedLatch = CountDownLatch(1)
        gameFactory.getGameEngine().setTicksPerLoop(20)
        loadDefaultMap()
    }

    internal fun waitForFinished() {
        try {
            finishedLatch.await()
            deleteSaveGame()
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    protected abstract fun tick()

    protected fun getGameFactory(): GameFactory = gameFactory

    protected fun saveGame() {
        Log.i(TAG, "Saving game...")
        deleteSaveGame()
        saveGameInfo = gameFactory.getGameSaver().saveGame()
    }

    protected fun loadGame() {
        val info = saveGameInfo ?: return

        Log.i(TAG, "Loading game...")
        gameFactory.getGameLoader().loadGame(gameFactory.getSaveGameRepository().getGameStateFile(info))
        installTickHandler()
    }

    protected fun deleteSaveGame() {
        val info = saveGameInfo ?: return

        Log.i(TAG, "Deleting save game...")
        gameFactory.getSaveGameRepository().deleteSaveGame(info)
        saveGameInfo = null
    }

    protected fun autoSaveAndLoad() {
        Log.i(TAG, "Testing auto save and load...")
        gameFactory.getGameSaver().autoSaveGame()
        gameFactory.getGameLoader().autoLoadGame()
        installTickHandler()
    }

    private fun loadDefaultMap() {
        loadMap(gameFactory.getMapRepository().getDefaultMapId())
    }

    private fun loadMap(mapId: String) {
        gameFactory.getGameLoader().loadMap(mapId)
        waitForGameRestarted()
        adjustSettings()
        installTickHandler()
    }

    private fun waitForGameRestarted() {
        val gameState = gameFactory.getGameState()
        val loadMapLatch = CountDownLatch(1)

        val listener = object : GameState.Listener {
            override fun gameRestart() {
                loadMapLatch.countDown()
                gameState.removeListener(this)
            }

            override fun gameOver() {
            }
        }
        gameState.addListener(listener)

        try {
            loadMapLatch.await()
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    private fun adjustSettings() {
        // Make it easier for the simulator to reach higher tiers.
        gameFactory.getScoreBoard().giveCredits(500000, false)
    }

    private fun installTickHandler() {
        gameFactory.getGameEngine().add {
            if (gameFactory.getGameState().isGameOver()) {
                simulationFinished()
            } else {
                tick()
            }
        }
    }

    private fun simulationFinished() {
        if (finishedLatch.count > 0) {
            Log.i(TAG, "final wave=${gameFactory.getWaveManager().getWaveNumber()}")
            Log.i(TAG, "final score=${gameFactory.getScoreBoard().getScore()}")
            finishedLatch.countDown()
        }
    }

    companion object {
        private val TAG: String = GameSimulator::class.java.simpleName
    }
}
