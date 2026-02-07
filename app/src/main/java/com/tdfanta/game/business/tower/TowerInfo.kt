package com.tdfanta.game.business.tower

import com.tdfanta.game.entity.tower.Aimer
import com.tdfanta.game.entity.tower.Tower
import com.tdfanta.game.entity.tower.TowerInfoValue
import com.tdfanta.game.entity.tower.TowerStrategy

class TowerInfo(tower: Tower, credits: Int, controlsEnabled: Boolean) {
    private val mValue: Int = tower.getValue()
    private val mLevel: Int = tower.getLevel()
    private val mLevelMax: Int = tower.getMaxLevel()
    private val mEnhanceable: Boolean
    private val mEnhanceCost: Int = tower.getEnhanceCost()
    private val mUpgradeable: Boolean
    private val mUpgradeCost: Int = tower.getUpgradeCost()
    private val mSellable: Boolean = controlsEnabled
    private val mCanLockTarget: Boolean
    private val mDoesLockTarget: Boolean
    private val mHasStrategy: Boolean
    private val mStrategy: TowerStrategy?
    private val mProperties: List<TowerInfoValue>

    init {
        mEnhanceable = tower.isEnhanceable() && mEnhanceCost <= credits && controlsEnabled
        mUpgradeable = tower.isUpgradeable() && mUpgradeCost <= credits && controlsEnabled

        val aimer: Aimer? = tower.getAimer()

        if (aimer != null) {
            mCanLockTarget = true
            mDoesLockTarget = aimer.doesLockTarget()
            mHasStrategy = true
            mStrategy = aimer.getStrategy()
        } else {
            mCanLockTarget = false
            mDoesLockTarget = false
            mHasStrategy = false
            mStrategy = null
        }

        mProperties = tower.getTowerInfoValues()
    }

    fun getValue(): Int = mValue

    fun isSellable(): Boolean = mSellable

    fun getLevel(): Int = mLevel

    fun getLevelMax(): Int = mLevelMax

    fun isEnhanceable(): Boolean = mEnhanceable

    fun getEnhanceCost(): Int = mEnhanceCost

    fun isUpgradeable(): Boolean = mUpgradeable

    fun getUpgradeCost(): Int = mUpgradeCost

    fun canLockTarget(): Boolean = mCanLockTarget

    fun doesLockTarget(): Boolean = mDoesLockTarget

    fun hasStrategy(): Boolean = mHasStrategy

    fun getStrategy(): TowerStrategy? = mStrategy

    fun getProperties(): List<TowerInfoValue> = mProperties
}
