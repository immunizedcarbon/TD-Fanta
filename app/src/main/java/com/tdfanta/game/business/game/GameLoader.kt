package com.tdfanta.game.business.game

import android.content.Context
import android.util.Log
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.EntityRegistry
import com.tdfanta.game.engine.logic.loop.ErrorListener
import com.tdfanta.game.engine.logic.map.GameMap
import com.tdfanta.game.engine.logic.map.PlateauInfo
import com.tdfanta.game.engine.logic.map.WaveInfo
import com.tdfanta.game.engine.logic.persistence.GamePersister
import com.tdfanta.game.engine.render.Viewport
import com.tdfanta.game.entity.plateau.Plateau
import com.tdfanta.game.util.container.KeyValueStore
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.CopyOnWriteArrayList

class GameLoader(
    private val mContext: Context,
    private val mGameEngine: GameEngine,
    private val mGamePersister: GamePersister,
    private val mViewport: Viewport,
    private val mEntityRegistry: EntityRegistry,
    private val mMapRepository: MapRepository,
    private val mSaveGameRepository: SaveGameRepository,
) : ErrorListener {
    interface Listener {
        fun gameLoaded()
    }

    private var mCurrentMapId: String? = null

    private val mSaveGameMigrator = SaveGameMigrator()
    private val mListeners = CopyOnWriteArrayList<Listener>()

    init {
        mGameEngine.registerErrorListener(this)
    }

    fun addListener(listener: Listener) {
        mListeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        mListeners.remove(listener)
    }

    fun getCurrentMapId(): String = checkNotNull(mCurrentMapId) { "Current map is not initialized." }

    fun restart() {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { restart() }
            return
        }

        if (mCurrentMapId == null) {
            return
        }

        val mapId = checkNotNull(mCurrentMapId) { "Current map is not initialized." }
        loadMap(mapId)
    }

    fun autoLoadGame() {
        val autoSaveStateFile = mSaveGameRepository.getAutoSaveStateFile()

        if (autoSaveStateFile.exists()) {
            loadGame(autoSaveStateFile)
        } else {
            Log.i(TAG, "No auto save game file not found.")
            loadMap(mMapRepository.getDefaultMapId())
        }
    }

    fun loadGame(stateFile: File) {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { loadGame(stateFile) }
            return
        }

        Log.i(TAG, "Loading game...")
        val gameState = try {
            FileInputStream(stateFile).use { inputStream ->
                KeyValueStore.fromStream(inputStream)
            }
        } catch (e: Exception) {
            throw RuntimeException("Could not load game!", e)
        }

        if (!mSaveGameMigrator.migrate(gameState)) {
            Log.w(TAG, "Failed to migrate save game!")
            loadMap(mMapRepository.getDefaultMapId())
            return
        }

        val mapId = gameState.getString("mapId")
        mCurrentMapId = mapId
        initializeGame(mapId, gameState)
    }

    fun loadMap(mapId: String) {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { loadMap(mapId) }
            return
        }

        mCurrentMapId = mapId
        initializeGame(mapId, null)
    }

    private fun initializeGame(mapId: String, gameState: KeyValueStore?) {
        Log.d(TAG, "Initializing game...")
        mGameEngine.clear()

        val mapInfo = mMapRepository.getMapById(mapId)
        val map = GameMap(KeyValueStore.fromResources(mContext.resources, mapInfo.getMapDataResId()))
        mGameEngine.setGameMap(map)

        val waveData = KeyValueStore.fromResources(mContext.resources, R.raw.waves)
        val waveInfos = ArrayList<WaveInfo>()
        for (data in waveData.getStoreList("waves")) {
            waveInfos.add(WaveInfo(data))
        }
        mGameEngine.setWaveInfos(waveInfos)

        mViewport.setGameSize(map.getWidth(), map.getHeight())

        if (gameState != null) {
            mGamePersister.readState(gameState)
        } else {
            mGamePersister.resetState()
            initializeMap(map)
        }

        for (listener in mListeners) {
            listener.gameLoaded()
        }

        Log.d(TAG, "Game loaded.")
    }

    private fun initializeMap(map: GameMap) {
        for (info: PlateauInfo in map.getPlateaus()) {
            val plateau = mEntityRegistry.createEntity(info.getName()) as Plateau
            plateau.setPosition(info.getPosition())
            mGameEngine.add(plateau)
        }
    }

    override fun error(e: Exception, loopCount: Int) {
        // avoid game not starting anymore because of a somehow corrupt saved game file
        if (loopCount < 10) {
            Log.w(TAG, "Game crashed just after loading, deleting saved game file.")
            mSaveGameRepository.getAutoSaveStateFile().delete()
        }
    }

    companion object {
        private val TAG = GameLoader::class.java.simpleName
    }
}
