package com.tdfanta.game.entity.effect

import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.loop.TickTimer
import com.tdfanta.game.entity.EntityTypes

abstract class Effect : Entity {
    private enum class State {
        NotStarted,
        Active,
        Ended,
    }

    private val mOrigin: Entity
    private var mTimer: TickTimer? = null
    private var mState = State.NotStarted

    internal constructor(origin: Entity) : super(origin.getGameEngine()) {
        mOrigin = origin
    }

    internal constructor(origin: Entity, duration: Float) : this(origin) {
        mTimer = TickTimer.createInterval(duration)
    }

    fun getOrigin(): Entity = mOrigin

    final override fun getEntityType(): Int = EntityTypes.EFFECT

    override fun tick() {
        super.tick()

        if (mState == State.NotStarted) {
            mState = State.Active
            effectBegin()
        }

        if (mTimer != null && mTimer!!.tick()) {
            mState = State.Ended
            effectEnd()
            remove()
        }
    }

    protected open fun effectBegin() {
    }

    protected open fun effectEnd() {
    }
}
