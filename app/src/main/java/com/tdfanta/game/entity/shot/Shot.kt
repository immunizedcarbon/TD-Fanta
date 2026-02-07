package com.tdfanta.game.entity.shot

import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.entity.EntityTypes
import com.tdfanta.game.util.math.Vector2

abstract class Shot(private val mOrigin: Entity) : Entity(mOrigin.getGameEngine()) {
    private var mSpeed = 0f
    private lateinit var mDirection: Vector2
    private var mEnabled = true

    final override fun getEntityType(): Int = EntityTypes.SHOT

    override fun tick() {
        super.tick()

        if (mEnabled) {
            move(Vector2.mul(mDirection, mSpeed / GameEngine.TARGET_FRAME_RATE))
        }
    }

    fun isEnabled(): Boolean = mEnabled

    open fun setEnabled(enabled: Boolean) {
        mEnabled = enabled
    }

    fun getOrigin(): Entity = mOrigin

    fun getSpeed(): Float = mSpeed

    protected fun setSpeed(speed: Float) {
        mSpeed = speed
    }

    protected fun setDirection(direction: Vector2) {
        mDirection = direction
    }
}
