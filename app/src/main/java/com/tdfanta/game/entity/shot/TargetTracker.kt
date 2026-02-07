package com.tdfanta.game.entity.shot

import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.entity.enemy.Enemy
import com.tdfanta.game.util.math.Vector2

class TargetTracker(
    private val mShot: Shot,
    private val mListener: Listener,
) : Entity.Listener {
    interface Listener {
        fun targetReached(target: Enemy)

        fun targetLost(target: Enemy)
    }

    private var mTarget: Enemy? = null
    private var mTargetReached = false

    constructor(target: Enemy, shot: Shot, listener: Listener) : this(shot, listener) {
        setTarget(target)
    }

    fun setTarget(target: Enemy) {
        mTarget = target
        mTargetReached = false
    }

    fun getTargetDirection(): Vector2 = mShot.getDirectionTo(mTarget!!)

    fun tick() {
        val target = mTarget
        if (mTargetReached || target == null) {
            return
        }

        if (mShot.getDistanceTo(target) <= mShot.getSpeed() / GameEngine.TARGET_FRAME_RATE) {
            mTargetReached = true
            mListener.targetReached(target)
        }
    }

    override fun entityRemoved(entity: Entity) {
        if (!mTargetReached) {
            entity.removeListener(this)
            mListener.targetLost(mTarget!!)
        }
    }
}
