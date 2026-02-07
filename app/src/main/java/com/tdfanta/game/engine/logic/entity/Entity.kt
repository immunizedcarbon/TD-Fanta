package com.tdfanta.game.engine.logic.entity

import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.render.sprite.SpriteFactory
import com.tdfanta.game.engine.sound.SoundFactory
import com.tdfanta.game.engine.theme.Theme
import com.tdfanta.game.util.iterator.Function
import com.tdfanta.game.util.iterator.Predicate
import com.tdfanta.game.util.math.MathUtils
import com.tdfanta.game.util.math.Vector2
import java.util.concurrent.CopyOnWriteArrayList

abstract class Entity(private val mGameEngine: GameEngine) {
    interface Listener {
        fun entityRemoved(entity: Entity)
    }

    private val mListeners = CopyOnWriteArrayList<Listener>()
    private var mEntityId = 0
    private var mPosition = Vector2()

    fun setEntityId(entityId: Int) {
        mEntityId = entityId
    }

    fun getEntityId(): Int = mEntityId

    abstract fun getEntityType(): Int

    open fun getEntityName(): String = javaClass.simpleName

    open fun initStatic(): Any? = null

    open fun init() {
    }

    open fun clean() {
        for (listener in mListeners) {
            listener.entityRemoved(this)
        }
    }

    fun remove() {
        mGameEngine.remove(this)
    }

    open fun tick() {
    }

    protected fun getStaticData(): Any? = mGameEngine.getStaticData(this)

    fun getGameEngine(): GameEngine = mGameEngine

    protected fun getSpriteFactory(): SpriteFactory = mGameEngine.getSpriteFactory()

    protected fun getTheme(): Theme = mGameEngine.getThemeManager().getTheme()

    protected fun getSoundFactory(): SoundFactory = mGameEngine.getSoundFactory()

    fun getPosition(): Vector2 = mPosition

    open fun setPosition(position: Vector2) {
        mPosition = position
    }

    // note: overwrites offset
    open fun move(offset: Vector2) {
        mPosition = offset.add(mPosition)
    }

    fun getDistanceTo(target: Entity): Float = getDistanceTo(target.mPosition)

    fun getDistanceTo(target: Vector2): Float = mPosition.distanceTo(target)

    fun getDirectionTo(target: Entity): Vector2 = getDirectionTo(target.mPosition)

    fun getDirectionTo(target: Vector2): Vector2 = mPosition.directionTo(target)

    fun getAngleTo(target: Entity): Float = getAngleTo(target.mPosition)

    fun getAngleTo(target: Vector2): Float = mPosition.angleTo(target)

    fun isPositionVisible(): Boolean = mGameEngine.isPositionVisible(mPosition)

    fun addListener(listener: Listener) {
        mListeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        mListeners.remove(listener)
    }

    companion object {
        @JvmStatic
        fun inRange(center: Vector2, range: Float): Predicate<Entity> =
            Predicate { entity -> entity.getDistanceTo(center) <= range }

        @JvmStatic
        fun onLine(p1: Vector2, p2: Vector2, lineWidth: Float): Predicate<Entity> =
            Predicate { entity ->
                val line = Vector2.to(p1, p2)
                val toObj = Vector2.to(p1, entity.mPosition)
                val proj = toObj.proj(line)

                // check whether object is after line end
                if (proj.len() > line.len()) {
                    return@Predicate false
                }

                // check whether object is before line end
                if (!MathUtils.equals(proj.angle(), line.angle(), 1f)) {
                    return@Predicate false
                }

                proj.distanceTo(toObj) <= lineWidth / 2f
            }

        @JvmStatic
        fun nameEquals(name: String): Predicate<Entity> =
            Predicate { value -> name == value.getEntityName() }

        @JvmStatic
        fun distanceTo(toPoint: Vector2): Function<Entity, Float> =
            Function { input -> input.getDistanceTo(toPoint) }
    }
}
