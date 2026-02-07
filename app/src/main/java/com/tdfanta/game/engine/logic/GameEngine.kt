package com.tdfanta.game.engine.logic

import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.entity.EntityStore
import com.tdfanta.game.engine.logic.loop.ErrorListener
import com.tdfanta.game.engine.logic.loop.GameLoop
import com.tdfanta.game.engine.logic.loop.Message
import com.tdfanta.game.engine.logic.loop.MessageQueue
import com.tdfanta.game.engine.logic.loop.TickListener
import com.tdfanta.game.engine.logic.map.GameMap
import com.tdfanta.game.engine.logic.map.WaveInfo
import com.tdfanta.game.engine.render.Drawable
import com.tdfanta.game.engine.render.Renderer
import com.tdfanta.game.engine.render.sprite.SpriteFactory
import com.tdfanta.game.engine.sound.SoundFactory
import com.tdfanta.game.engine.theme.ThemeManager
import com.tdfanta.game.util.iterator.StreamIterator
import com.tdfanta.game.util.math.Vector2

class GameEngine(
    private val mSpriteFactory: SpriteFactory,
    private val mThemeManager: ThemeManager,
    private val mSoundFactory: SoundFactory,
    private val mEntityStore: EntityStore,
    private val mMessageQueue: MessageQueue,
    private val mRenderer: Renderer,
    private val mGameLoop: GameLoop,
) {
    private var mGameMap: GameMap? = null
    private var mWaveInfos: List<WaveInfo>? = null

    fun getGameMap(): GameMap? = mGameMap

    fun setGameMap(gameMap: GameMap?) {
        mGameMap = gameMap
    }

    fun getWaveInfos(): List<WaveInfo>? = mWaveInfos

    fun setWaveInfos(waveInfos: List<WaveInfo>?) {
        mWaveInfos = waveInfos
    }

    fun getThemeManager(): ThemeManager = mThemeManager

    fun getSpriteFactory(): SpriteFactory = mSpriteFactory

    fun getSoundFactory(): SoundFactory = mSoundFactory

    fun getStaticData(entity: Entity): Any? = mEntityStore.getStaticData(entity)

    fun getAllEntities(): StreamIterator<Entity> = mEntityStore.getAll()

    fun getEntitiesByType(typeId: Int): StreamIterator<Entity> = mEntityStore.getByType(typeId)

    fun getEntityById(entityId: Int): Entity? = mEntityStore.getById(entityId)

    fun add(entity: Entity) {
        mEntityStore.add(entity)
    }

    fun add(drawable: Drawable) {
        mRenderer.add(drawable)
    }

    fun add(listener: TickListener) {
        mGameLoop.add(listener)
    }

    fun remove(entity: Entity) {
        mEntityStore.remove(entity)
    }

    fun remove(drawable: Drawable?) {
        drawable?.let(mRenderer::remove)
    }

    fun remove(listener: TickListener) {
        mGameLoop.remove(listener)
    }

    fun clear() {
        mMessageQueue.clear()
        mEntityStore.clear()
        mRenderer.clear()
        mGameLoop.clear()
    }

    fun start() {
        mGameLoop.start()
    }

    fun stop() {
        mGameLoop.stop()
    }

    fun getTickCount(): Int = mMessageQueue.getTickCount()

    fun post(message: Message) {
        mMessageQueue.post(message)
    }

    fun postDelayed(message: Message, delay: Float) {
        mMessageQueue.postAfterTicks(message, kotlin.math.round(delay * TARGET_FRAME_RATE).toInt())
    }

    fun postAfterTicks(message: Message, ticks: Int) {
        mMessageQueue.postAfterTicks(message, ticks)
    }

    fun setTicksPerLoop(ticksPerLoop: Int) {
        mGameLoop.setTicksPerLoop(ticksPerLoop)
    }

    fun isThreadRunning(): Boolean = mGameLoop.isRunning()

    fun isThreadChangeNeeded(): Boolean = mGameLoop.isThreadChangeNeeded()

    fun isPositionVisible(position: Vector2): Boolean = mRenderer.isPositionVisible(position)

    fun registerErrorListener(listener: ErrorListener) {
        mGameLoop.registerErrorListener(listener)
    }

    companion object {
        @JvmField
        val TARGET_FRAME_RATE: Int = GameLoop.TARGET_FRAME_RATE
    }
}
