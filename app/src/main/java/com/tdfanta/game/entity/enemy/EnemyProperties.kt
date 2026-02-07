package com.tdfanta.game.entity.enemy

import java.util.Collections

class EnemyProperties {
    private var mHealth = 0
    private var mSpeed = 0f
    private var mReward = 0
    private var mWeakAgainst: Collection<WeaponType> = Collections.emptyList()
    private var mStrongAgainst: Collection<WeaponType> = Collections.emptyList()

    class Builder {
        private val mResult = EnemyProperties()

        fun setHealth(health: Int): Builder {
            mResult.mHealth = health
            return this
        }

        fun setSpeed(speed: Float): Builder {
            mResult.mSpeed = speed
            return this
        }

        fun setReward(reward: Int): Builder {
            mResult.mReward = reward
            return this
        }

        fun setWeakAgainst(vararg weakAgainst: WeaponType): Builder {
            mResult.mWeakAgainst = weakAgainst.asList()
            return this
        }

        fun setStrongAgainst(vararg strongAgainst: WeaponType): Builder {
            mResult.mStrongAgainst = strongAgainst.asList()
            return this
        }

        fun build(): EnemyProperties = mResult
    }

    fun getHealth(): Int = mHealth

    fun getSpeed(): Float = mSpeed

    fun getReward(): Int = mReward

    fun getWeakAgainst(): Collection<WeaponType> = mWeakAgainst

    fun getStrongAgainst(): Collection<WeaponType> = mStrongAgainst
}
