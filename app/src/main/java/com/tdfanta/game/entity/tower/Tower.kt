package com.tdfanta.game.entity.tower

import android.graphics.Canvas
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.loop.TickTimer
import com.tdfanta.game.entity.EntityTypes
import com.tdfanta.game.entity.enemy.Enemy
import com.tdfanta.game.entity.enemy.WeaponType
import com.tdfanta.game.entity.plateau.Plateau
import com.tdfanta.game.util.iterator.StreamIterator
import java.util.concurrent.CopyOnWriteArrayList

abstract class Tower protected constructor(
    gameEngine: GameEngine,
    private val mTowerProperties: TowerProperties,
) : Entity(gameEngine) {
    interface Listener {
        fun damageInflicted(totalDamage: Float)

        fun propertiesChanged()
    }

    private var mBuilt: Boolean
    private var mValue: Int
    private var mLevel: Int
    private var mDamage: Float
    private var mRange: Float
    private var mReloadTime: Float
    private var mDamageInflicted = 0f
    private var mReloaded = false

    private var mPlateau: Plateau? = null

    private val mReloadTimer: TickTimer
    private var mRangeIndicator: RangeIndicator? = null
    private var mLevelIndicator: LevelIndicator? = null

    private val mListeners = CopyOnWriteArrayList<Listener>()

    init {
        mValue = mTowerProperties.getValue()
        mDamage = mTowerProperties.getDamage().toFloat()
        mRange = mTowerProperties.getRange()
        mReloadTime = mTowerProperties.getReload()
        mLevel = 1

        mReloadTimer = TickTimer.createInterval(mReloadTime)

        mBuilt = false
    }

    final override fun getEntityType(): Int = EntityTypes.TOWER

    override fun clean() {
        super.clean()
        hideRange()

        if (mPlateau != null) {
            mPlateau!!.setOccupied(false)
            mPlateau = null
        }
    }

    override fun tick() {
        super.tick()

        if (mBuilt && !mReloaded && mReloadTimer.tick()) {
            mReloaded = true
        }
    }

    open fun getAimer(): Aimer? = null

    abstract fun preview(canvas: Canvas)

    abstract fun getTowerInfoValues(): List<TowerInfoValue>

    fun getPlateau(): Plateau = mPlateau!!

    fun setPlateau(plateau: Plateau?) {
        if (plateau!!.isOccupied()) {
            throw RuntimeException("Plateau already occupied!")
        }

        mPlateau = plateau
        mPlateau!!.setOccupied(true)
        setPosition(mPlateau!!.getPosition())
    }

    fun isBuilt(): Boolean = mBuilt

    open fun setBuilt() {
        mBuilt = true
        mReloaded = true
    }

    fun getWeaponType(): WeaponType = mTowerProperties.getWeaponType()

    fun isReloaded(): Boolean = mReloaded

    fun setReloaded(reloaded: Boolean) {
        mReloaded = reloaded
    }

    fun getValue(): Int = mValue

    fun setValue(value: Int) {
        mValue = value

        for (listener in mListeners) {
            listener.propertiesChanged()
        }
    }

    fun getDamage(): Float = mDamage

    fun getRange(): Float = mRange

    fun getReloadTime(): Float = mReloadTime

    fun getDamageInflicted(): Float = mDamageInflicted

    fun reportDamageInflicted(amount: Float) {
        mDamageInflicted += amount

        for (listener in mListeners) {
            listener.damageInflicted(mDamageInflicted)
        }
    }

    fun setDamageInflicted(damageInflicted: Float) {
        mDamageInflicted = damageInflicted
    }

    fun isUpgradeable(): Boolean = mTowerProperties.getUpgradeTowerName() != null

    fun getUpgradeName(): String = mTowerProperties.getUpgradeTowerName()!!

    fun getUpgradeCost(): Int = mTowerProperties.getUpgradeCost()

    fun getUpgradeLevel(): Int = mTowerProperties.getUpgradeLevel()

    open fun enhance() {
        mValue += getEnhanceCost()
        mDamage += mTowerProperties.getEnhanceDamage() *
            Math.pow(mTowerProperties.getEnhanceBase().toDouble(), (mLevel - 1).toDouble()).toFloat()
        mRange += mTowerProperties.getEnhanceRange()
        mReloadTime -= mTowerProperties.getEnhanceReload()

        mLevel++

        mReloadTimer.setInterval(mReloadTime)

        for (listener in mListeners) {
            listener.propertiesChanged()
        }
    }

    fun isEnhanceable(): Boolean = mLevel < mTowerProperties.getMaxLevel()

    fun getEnhanceCost(): Int {
        if (!isEnhanceable()) {
            return -1
        }

        return kotlin.math.round(
            mTowerProperties.getEnhanceCost() *
                Math.pow(mTowerProperties.getEnhanceBase().toDouble(), (mLevel - 1).toDouble()).toFloat(),
        ).toInt()
    }

    fun getLevel(): Int = mLevel

    fun getMaxLevel(): Int = mTowerProperties.getMaxLevel()

    fun showRange() {
        if (mRangeIndicator == null) {
            mRangeIndicator = RangeIndicator(getTheme(), this)
            getGameEngine().add(mRangeIndicator!!)
        }
    }

    fun hideRange() {
        if (mRangeIndicator != null) {
            getGameEngine().remove(mRangeIndicator!!)
            mRangeIndicator = null
        }
    }

    fun showLevel() {
        if (mLevelIndicator == null) {
            mLevelIndicator = LevelIndicator(getTheme(), this)
            getGameEngine().add(mLevelIndicator!!)
        }
    }

    fun hideLevel() {
        if (mLevelIndicator != null) {
            getGameEngine().remove(mLevelIndicator!!)
            mLevelIndicator = null
        }
    }

    open fun getPossibleTargets(): StreamIterator<Enemy> = getGameEngine().getEntitiesByType(EntityTypes.ENEMY)
        .filter(inRange(getPosition(), mRange))
        .cast(Enemy::class.java)

    fun addListener(listener: Listener) {
        mListeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        mListeners.remove(listener)
    }
}
