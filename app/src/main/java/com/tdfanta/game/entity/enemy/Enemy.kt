package com.tdfanta.game.entity.enemy

import android.graphics.Canvas
import com.tdfanta.game.GameSettings
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.entity.EntityTypes
import com.tdfanta.game.entity.effect.TeleportedMarker
import com.tdfanta.game.entity.tower.Tower
import com.tdfanta.game.util.iterator.Function
import com.tdfanta.game.util.math.Vector2
import java.util.concurrent.CopyOnWriteArrayList

abstract class Enemy protected constructor(
    gameEngine: GameEngine,
    private val mEnemyProperties: EnemyProperties,
) : Entity(gameEngine) {
    interface Listener {
        fun enemyKilled(enemy: Enemy)

        fun enemyFinished(enemy: Enemy)

        fun enemyRemoved(enemy: Enemy)
    }

    private var mHealth = mEnemyProperties.getHealth().toFloat()
    private var mMaxHealth = mEnemyProperties.getHealth().toFloat()
    private var mSpeedModifier = 1f
    private var mReward = mEnemyProperties.getReward()
    private var mWaveNumber = 0
    private var mWayPoints: List<Vector2>? = null
    private var mWayPointIndex = 0
    private var mBeingTeleported = false
    private var mWasTeleported = false

    private val mHealthBar = HealthBar(getTheme(), this)

    private val mListeners = CopyOnWriteArrayList<Listener>()

    abstract fun getTextId(): Int

    abstract fun drawPreview(canvas: Canvas)

    final override fun getEntityType(): Int = EntityTypes.ENEMY

    override fun init() {
        super.init()
        getGameEngine().add(mHealthBar)
    }

    override fun clean() {
        super.clean()
        getGameEngine().remove(mHealthBar)

        for (listener in mListeners) {
            listener.enemyRemoved(this)
        }

        mListeners.clear()
    }

    override fun tick() {
        super.tick()

        if (mBeingTeleported) {
            return
        }

        if (!hasWayPoint()) {
            for (listener in mListeners) {
                listener.enemyFinished(this)
            }
            remove()
            return
        }

        val stepSize = getSpeed() / GameEngine.TARGET_FRAME_RATE
        if (getDistanceTo(getCurrentWayPoint()) >= stepSize) {
            move(getDirection()!!.mul(stepSize))
        } else {
            setPosition(getCurrentWayPoint())
            mWayPointIndex++
        }
    }

    fun getEnemyProperties(): EnemyProperties = mEnemyProperties

    fun startTeleport() {
        mBeingTeleported = true
    }

    fun finishTeleport() {
        mBeingTeleported = false
        mWasTeleported = true
        getGameEngine().add(TeleportedMarker(this))
    }

    fun isBeingTeleported(): Boolean = mBeingTeleported

    fun wasTeleported(): Boolean = mWasTeleported

    fun getWaveNumber(): Int = mWaveNumber

    fun setWaveNumber(waveNumber: Int) {
        mWaveNumber = waveNumber
    }

    fun setupPath(wayPoints: List<Vector2>) {
        setupPath(wayPoints, 0)
    }

    fun setupPath(wayPoints: List<Vector2>, wayPointIndex: Int) {
        mWayPoints = wayPoints
        mWayPointIndex = wayPointIndex
    }

    private fun getCurrentWayPoint(): Vector2 = mWayPoints!![mWayPointIndex]

    fun getWayPoints(): List<Vector2> = mWayPoints!!

    fun getWayPointIndex(): Int = mWayPointIndex

    fun hasWayPoint(): Boolean = mWayPoints != null && mWayPointIndex < mWayPoints!!.size

    fun getDirection(): Vector2? {
        if (!hasWayPoint()) {
            return null
        }

        return getDirectionTo(getCurrentWayPoint())
    }

    open fun getSpeed(): Float = mEnemyProperties.getSpeed() * kotlin.math.max(mSpeedModifier, GameSettings.MIN_SPEED_MODIFIER)

    fun modifySpeed(f: Float, origin: Entity) {
        if (origin is Tower) {
            if (mEnemyProperties.getStrongAgainst().contains(origin.getWeaponType())) {
                return
            }
        }

        mSpeedModifier *= f
    }

    private fun getDistanceRemainingValue(): Float {
        if (!hasWayPoint()) {
            return 0f
        }

        var dist = getDistanceTo(getCurrentWayPoint())

        for (i in mWayPointIndex + 1 until mWayPoints!!.size) {
            val wThis = mWayPoints!![i]
            val wLast = mWayPoints!![i - 1]

            dist += wLast.distanceTo(wThis)
        }

        return dist
    }

    fun getPositionAfter(sec: Float): Vector2 {
        if (mWayPoints == null) {
            return getPosition()
        }

        var distance = sec * getSpeed()
        var index = mWayPointIndex
        var position = getPosition()

        while (index < mWayPoints!!.size) {
            val wayPoint = mWayPoints!![index]
            val toWaypointDist = position.distanceTo(wayPoint)

            if (distance < toWaypointDist) {
                return Vector2.to(position, wayPoint)
                    .mul(distance / toWaypointDist)
                    .add(position)
            }
            distance -= toWaypointDist
            index++
        }

        return position
    }

    fun sendBack(mutDist: Float) {
        var dist = mutDist
        var index = mWayPointIndex - 1
        var pos = getPosition()

        while (index > 0) {
            val wp = mWayPoints!![index]
            val toWpDist = pos.distanceTo(wp)

            if (dist > toWpDist) {
                dist -= toWpDist
                pos = wp
                index--
            } else {
                pos = pos.directionTo(wp)
                    .mul(dist)
                    .add(pos)
                setPosition(pos)
                mWayPointIndex = index + 1
                return
            }
        }

        setPosition(mWayPoints!![0])
        mWayPointIndex = 1
    }

    fun getHealth(): Float = mHealth

    fun getMaxHealth(): Float = mMaxHealth

    fun damage(mutAmount: Float, origin: Entity) {
        var amount = mutAmount

        if (origin is Tower) {
            if (mEnemyProperties.getWeakAgainst().contains(origin.getWeaponType())) {
                amount *= GameSettings.WEAK_AGAINST_DAMAGE_MODIFIER
            }

            if (mEnemyProperties.getStrongAgainst().contains(origin.getWeaponType())) {
                amount *= GameSettings.STRONG_AGAINST_DAMAGE_MODIFIER
            }

            origin.reportDamageInflicted(amount)
        }

        mHealth -= amount

        if (mHealth <= 0) {
            for (listener in mListeners) {
                listener.enemyKilled(this)
            }

            remove()
        }
    }

    fun modifyHealth(f: Float) {
        mHealth *= f
        mMaxHealth *= f
    }

    fun setHealth(health: Float, maxHealth: Float) {
        mHealth = health
        mMaxHealth = maxHealth
    }

    fun heal(amount: Float) {
        mHealth += amount

        if (mHealth > mMaxHealth) {
            mHealth = mMaxHealth
        }
    }

    fun getReward(): Int = mReward

    fun modifyReward(f: Float) {
        mReward = kotlin.math.round(mReward * f).toInt()
    }

    fun setReward(reward: Int) {
        mReward = reward
    }

    fun addListener(listener: Listener) {
        mListeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        mListeners.remove(listener)
    }

    companion object {
        @JvmStatic
        fun health(): Function<Enemy, Float> = Function { input -> input.mHealth }

        @JvmStatic
        fun distanceRemaining(): Function<Enemy, Float> =
            Function { input -> input.getDistanceRemainingValue() }
    }
}
