package com.tdfanta.game

import android.content.Context
import androidx.preference.PreferenceManager
import com.tdfanta.game.business.game.GameLoader
import com.tdfanta.game.business.game.GameSaver
import com.tdfanta.game.business.game.GameSpeed
import com.tdfanta.game.business.game.GameState
import com.tdfanta.game.business.game.HighScores
import com.tdfanta.game.business.game.MapRepository
import com.tdfanta.game.business.game.SaveGameRepository
import com.tdfanta.game.business.game.ScoreBoard
import com.tdfanta.game.business.game.TutorialControl
import com.tdfanta.game.business.tower.TowerAging
import com.tdfanta.game.business.tower.TowerControl
import com.tdfanta.game.business.tower.TowerInserter
import com.tdfanta.game.business.tower.TowerSelector
import com.tdfanta.game.business.wave.WaveManager
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.EntityRegistry
import com.tdfanta.game.engine.logic.entity.EntityStore
import com.tdfanta.game.engine.logic.loop.FrameRateLogger
import com.tdfanta.game.engine.logic.loop.GameLoop
import com.tdfanta.game.engine.logic.loop.MessageQueue
import com.tdfanta.game.engine.logic.persistence.GamePersister
import com.tdfanta.game.engine.render.Renderer
import com.tdfanta.game.engine.render.Viewport
import com.tdfanta.game.engine.render.sprite.SpriteFactory
import com.tdfanta.game.engine.sound.SoundFactory
import com.tdfanta.game.engine.sound.SoundManager
import com.tdfanta.game.engine.theme.ThemeManager
import com.tdfanta.game.entity.enemy.Blob
import com.tdfanta.game.entity.enemy.Flyer
import com.tdfanta.game.entity.enemy.Healer
import com.tdfanta.game.entity.enemy.Soldier
import com.tdfanta.game.entity.enemy.Sprinter
import com.tdfanta.game.entity.plateau.BasicPlateau
import com.tdfanta.game.entity.tower.BouncingLaser
import com.tdfanta.game.entity.tower.Canon
import com.tdfanta.game.entity.tower.DualCanon
import com.tdfanta.game.entity.tower.GlueGun
import com.tdfanta.game.entity.tower.GlueTower
import com.tdfanta.game.entity.tower.MachineGun
import com.tdfanta.game.entity.tower.MineLayer
import com.tdfanta.game.entity.tower.Mortar
import com.tdfanta.game.entity.tower.RocketLauncher
import com.tdfanta.game.entity.tower.SimpleLaser
import com.tdfanta.game.entity.tower.StraightLaser
import com.tdfanta.game.entity.tower.Teleporter

class GameFactory(context: Context) {
    // Engine
    private lateinit var mThemeManager: ThemeManager
    private lateinit var mSoundManager: SoundManager
    private lateinit var mSpriteFactory: SpriteFactory
    private lateinit var mSoundFactory: SoundFactory
    private lateinit var mViewport: Viewport
    private lateinit var mFrameRateLogger: FrameRateLogger
    private lateinit var mEntityStore: EntityStore
    private lateinit var mMessageQueue: MessageQueue
    private lateinit var mRenderer: Renderer
    private lateinit var mGameEngine: GameEngine
    private lateinit var mGameLoop: GameLoop
    private lateinit var mGamePersister: GamePersister
    private lateinit var mEntityRegistry: EntityRegistry

    // Business
    private lateinit var mScoreBoard: ScoreBoard
    private lateinit var mHighScores: HighScores
    private lateinit var mTowerSelector: TowerSelector
    private lateinit var mTowerControl: TowerControl
    private lateinit var mTowerAging: TowerAging
    private lateinit var mTowerInserter: TowerInserter
    private lateinit var mMapRepository: MapRepository
    private lateinit var mSaveGameRepository: SaveGameRepository
    private lateinit var mGameLoader: GameLoader
    private lateinit var mGameSaver: GameSaver
    private lateinit var mWaveManager: WaveManager
    private lateinit var mSpeedManager: GameSpeed
    private lateinit var mGameState: GameState
    private lateinit var mTutorialControl: TutorialControl

    init {
        PreferenceManager.setDefaultValues(context, R.xml.settings, false)

        initializeEngine(context)
        registerEntities()
        initializeBusiness(context)
        registerPersisters()
    }

    private fun initializeEngine(context: Context) {
        mViewport = Viewport()
        mEntityStore = EntityStore()
        mMessageQueue = MessageQueue()
        mGamePersister = GamePersister()
        mFrameRateLogger = FrameRateLogger()
        mRenderer = Renderer(mViewport, mFrameRateLogger)
        mGameLoop = GameLoop(mRenderer, mFrameRateLogger, mMessageQueue, mEntityStore)
        mThemeManager = ThemeManager(context, mRenderer)
        mSoundManager = SoundManager(context)
        mSpriteFactory = SpriteFactory(context, mThemeManager)
        mSoundFactory = SoundFactory(context, mSoundManager)
        mGameEngine = GameEngine(
            mSpriteFactory,
            mThemeManager,
            mSoundFactory,
            mEntityStore,
            mMessageQueue,
            mRenderer,
            mGameLoop,
        )
        mEntityRegistry = EntityRegistry(mGameEngine)
    }

    private fun registerEntities() {
        mEntityRegistry.registerEntity(BasicPlateau.Factory(), BasicPlateau.Persister())

        mEntityRegistry.registerEntity(Blob.Factory(), Blob.Persister())
        mEntityRegistry.registerEntity(Flyer.Factory(), Flyer.Persister())
        mEntityRegistry.registerEntity(Healer.Factory(), Healer.Persister())
        mEntityRegistry.registerEntity(Soldier.Factory(), Soldier.Persister())
        mEntityRegistry.registerEntity(Sprinter.Factory(), Sprinter.Persister())

        mEntityRegistry.registerEntity(Canon.Factory(), Canon.Persister())
        mEntityRegistry.registerEntity(DualCanon.Factory(), DualCanon.Persister())
        mEntityRegistry.registerEntity(MachineGun.Factory(), MachineGun.Persister())
        mEntityRegistry.registerEntity(SimpleLaser.Factory(), SimpleLaser.Persister())
        mEntityRegistry.registerEntity(BouncingLaser.Factory(), BouncingLaser.Persister())
        mEntityRegistry.registerEntity(StraightLaser.Factory(), StraightLaser.Persister())
        mEntityRegistry.registerEntity(Mortar.Factory(), Mortar.Persister())
        mEntityRegistry.registerEntity(MineLayer.Factory(), MineLayer.Persister())
        mEntityRegistry.registerEntity(RocketLauncher.Factory(), RocketLauncher.Persister())
        mEntityRegistry.registerEntity(GlueTower.Factory(), GlueTower.Persister())
        mEntityRegistry.registerEntity(GlueGun.Factory(), GlueGun.Persister())
        mEntityRegistry.registerEntity(Teleporter.Factory(), Teleporter.Persister())
    }

    private fun initializeBusiness(context: Context) {
        mMapRepository = MapRepository()
        mSaveGameRepository = SaveGameRepository(context)
        mScoreBoard = ScoreBoard(mGameEngine)
        mTowerAging = TowerAging(mGameEngine)
        mSpeedManager = GameSpeed(mGameEngine)
        mTowerSelector = TowerSelector(mGameEngine, mScoreBoard)
        mGameLoader = GameLoader(
            context,
            mGameEngine,
            mGamePersister,
            mViewport,
            mEntityRegistry,
            mMapRepository,
            mSaveGameRepository,
        )
        mHighScores = HighScores(context, mGameEngine, mScoreBoard, mGameLoader)
        mGameState = GameState(mScoreBoard, mHighScores, mTowerSelector)
        mWaveManager = WaveManager(mGameEngine, mScoreBoard, mGameState, mEntityRegistry, mTowerAging)
        mGameSaver = GameSaver(
            mGameEngine,
            mGameLoader,
            mGamePersister,
            mRenderer,
            mWaveManager,
            mScoreBoard,
            mSaveGameRepository,
        )
        mTowerControl = TowerControl(mGameEngine, mScoreBoard, mTowerSelector, mEntityRegistry)
        mTowerInserter = TowerInserter(
            mGameEngine,
            mGameState,
            mEntityRegistry,
            mTowerSelector,
            mTowerAging,
            mScoreBoard,
        )
        mTutorialControl = TutorialControl(context, mTowerInserter, mTowerSelector, mWaveManager)
    }

    private fun registerPersisters() {
        mGamePersister.registerPersister(mMessageQueue)
        mGamePersister.registerPersister(mScoreBoard)
        mGamePersister.registerPersister(mGameState)
        mGamePersister.registerPersister(mEntityRegistry)
        mGamePersister.registerPersister(mWaveManager)
    }

    fun getThemeManager(): ThemeManager = mThemeManager

    fun getViewport(): Viewport = mViewport

    fun getRenderer(): Renderer = mRenderer

    fun getGameEngine(): GameEngine = mGameEngine

    fun getEntityRegistry(): EntityRegistry = mEntityRegistry

    fun getScoreBoard(): ScoreBoard = mScoreBoard

    fun getTowerSelector(): TowerSelector = mTowerSelector

    fun getTowerControl(): TowerControl = mTowerControl

    fun getTowerInserter(): TowerInserter = mTowerInserter

    fun getGameLoader(): GameLoader = mGameLoader

    fun getWaveManager(): WaveManager = mWaveManager

    fun getSpeedManager(): GameSpeed = mSpeedManager

    fun getGameState(): GameState = mGameState

    fun getMapRepository(): MapRepository = mMapRepository

    fun getSaveGameRepository(): SaveGameRepository = mSaveGameRepository

    fun getHighScores(): HighScores = mHighScores

    fun getTutorialControl(): TutorialControl = mTutorialControl

    fun getGameSaver(): GameSaver = mGameSaver
}
