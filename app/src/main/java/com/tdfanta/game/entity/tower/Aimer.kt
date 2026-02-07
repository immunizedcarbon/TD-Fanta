package com.tdfanta.game.entity.tower

import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.loop.TickTimer
import com.tdfanta.game.entity.enemy.Enemy

class Aimer(private val mTower: Tower) : Entity.Listener {
    private var mTarget: Enemy? = null
    private var mStrategy = sDefaultStrategy
    private var mLockTarget = sDefaultLockTarget

    private val mUpdateTimer = TickTimer.createInterval(0.1f)

    fun tick() {
        if (mUpdateTimer.tick()) {
            val target = mTarget
            if (target != null && mTower.getDistanceTo(target) > mTower.getRange()) {
                setTarget(null)
            }

            if (mTarget == null || !mLockTarget) {
                nextTarget()
            }
        }
    }

    fun getStrategy(): TowerStrategy = mStrategy

    fun setStrategy(strategy: TowerStrategy) {
        mStrategy = strategy
        sDefaultStrategy = strategy
    }

    fun doesLockTarget(): Boolean = mLockTarget

    fun setLockTarget(lock: Boolean) {
        mLockTarget = lock
        sDefaultLockTarget = lock
    }

    fun getTarget(): Enemy? = mTarget

    fun setTarget(target: Enemy?) {
        if (mTarget != null) {
            mTarget!!.removeListener(this)
        }

        mTarget = target

        if (mTarget != null) {
            mTarget!!.addListener(this)
        }
    }

    private fun nextTarget() {
        when (mStrategy) {
            TowerStrategy.Closest -> {
                setTarget(mTower.getPossibleTargets().min(Entity.distanceTo(mTower.getPosition())))
            }

            TowerStrategy.Strongest -> {
                setTarget(mTower.getPossibleTargets().max(Enemy.health()))
            }

            TowerStrategy.Weakest -> {
                setTarget(mTower.getPossibleTargets().min(Enemy.health()))
            }

            TowerStrategy.First -> {
                setTarget(mTower.getPossibleTargets().min(Enemy.distanceRemaining()))
            }

            TowerStrategy.Last -> {
                setTarget(mTower.getPossibleTargets().max(Enemy.distanceRemaining()))
            }
        }
    }

    override fun entityRemoved(entity: Entity) {
        setTarget(null)
    }

    companion object {
        private var sDefaultStrategy = TowerStrategy.Closest
        private var sDefaultLockTarget = true
    }
}
