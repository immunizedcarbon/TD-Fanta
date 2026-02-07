package com.tdfanta.game.entity.tower

import com.tdfanta.game.entity.enemy.WeaponType

class TowerProperties {
    private var mValue = 0
    private var mDamage = 0
    private var mRange = 0f
    private var mReload = 0f
    private var mMaxLevel = 0
    private var mWeaponType: WeaponType? = null
    private var mEnhanceCost = 0
    private var mEnhanceBase = 0f
    private var mEnhanceDamage = 0
    private var mEnhanceRange = 0f
    private var mEnhanceReload = 0f
    private var mUpgradeTowerName: String? = null
    private var mUpgradeCost = 0
    private var mUpgradeLevel = 0

    class Builder {
        private val mResult = TowerProperties()

        fun setValue(value: Int): Builder {
            mResult.mValue = value
            return this
        }

        fun setDamage(damage: Int): Builder {
            mResult.mDamage = damage
            return this
        }

        fun setRange(range: Float): Builder {
            mResult.mRange = range
            return this
        }

        fun setReload(reload: Float): Builder {
            mResult.mReload = reload
            return this
        }

        fun setMaxLevel(maxLevel: Int): Builder {
            mResult.mMaxLevel = maxLevel
            return this
        }

        fun setWeaponType(weaponType: WeaponType): Builder {
            mResult.mWeaponType = weaponType
            return this
        }

        fun setEnhanceCost(enhanceCost: Int): Builder {
            mResult.mEnhanceCost = enhanceCost
            return this
        }

        fun setEnhanceBase(enhanceBase: Float): Builder {
            mResult.mEnhanceBase = enhanceBase
            return this
        }

        fun setEnhanceDamage(enhanceDamage: Int): Builder {
            mResult.mEnhanceDamage = enhanceDamage
            return this
        }

        fun setEnhanceRange(enhanceRange: Float): Builder {
            mResult.mEnhanceRange = enhanceRange
            return this
        }

        fun setEnhanceReload(enhanceReload: Float): Builder {
            mResult.mEnhanceReload = enhanceReload
            return this
        }

        fun setUpgradeTowerName(upgradeTowerName: String): Builder {
            mResult.mUpgradeTowerName = upgradeTowerName
            return this
        }

        fun setUpgradeCost(upgradeCost: Int): Builder {
            mResult.mUpgradeCost = upgradeCost
            return this
        }

        fun setUpgradeLevel(upgradeLevel: Int): Builder {
            mResult.mUpgradeLevel = upgradeLevel
            return this
        }

        fun build(): TowerProperties = mResult
    }

    fun getValue(): Int = mValue

    fun getDamage(): Int = mDamage

    fun getRange(): Float = mRange

    fun getReload(): Float = mReload

    fun getMaxLevel(): Int = mMaxLevel

    fun getWeaponType(): WeaponType = mWeaponType!!

    fun getEnhanceCost(): Int = mEnhanceCost

    fun getEnhanceBase(): Float = mEnhanceBase

    fun getEnhanceDamage(): Int = mEnhanceDamage

    fun getEnhanceRange(): Float = mEnhanceRange

    fun getEnhanceReload(): Float = mEnhanceReload

    fun getUpgradeTowerName(): String? = mUpgradeTowerName

    fun getUpgradeCost(): Int = mUpgradeCost

    fun getUpgradeLevel(): Int = mUpgradeLevel
}
