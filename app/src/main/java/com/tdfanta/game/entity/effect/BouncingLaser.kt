package com.tdfanta.game.entity.effect

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.render.Drawable
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.entity.EntityTypes
import com.tdfanta.game.entity.enemy.Enemy
import com.tdfanta.game.entity.enemy.Flyer
import com.tdfanta.game.util.math.Vector2

class BouncingLaser : Effect {
    private inner class LaserDrawable : Drawable {
        private val mPaint = Paint()
        private var mAlpha = ALPHA_START

        init {
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeWidth = 0.1f
            mPaint.color = Color.RED
        }

        fun decreaseVisibility() {
            mAlpha -= ALPHA_STEP

            if (mAlpha < 0) {
                mAlpha = 0
            }

            mPaint.alpha = mAlpha
        }

        override fun getLayer(): Int = Layers.SHOT

        override fun draw(canvas: Canvas) {
            canvas.drawLine(getPosition().x(), getPosition().y(), mTargetPos.x(), mTargetPos.y(), mPaint)
        }
    }

    private val mDamage: Float
    private val mBounceCount: Int
    val mMaxBounceDist: Float
    private var mOrigin: Enemy? = null
    private val mTarget: Enemy
    private var mTargetPos: Vector2
    private var mPrevTargets: MutableCollection<Enemy>? = null

    private val mDrawObject = LaserDrawable()

    constructor(origin: Entity, position: Vector2, target: Enemy, damage: Float) :
        this(origin, position, target, damage, 0, 0f)

    constructor(
        origin: Entity,
        position: Vector2,
        target: Enemy,
        damage: Float,
        bounceCount: Int,
        maxBounceDist: Float,
    ) : super(origin, EFFECT_DURATION) {
        setPosition(position)

        mTarget = target
        mTargetPos = target.getPosition()

        mDamage = damage
        mBounceCount = bounceCount
        mMaxBounceDist = maxBounceDist
    }

    private constructor(origin: BouncingLaser, target: Enemy) : this(
        origin.getOrigin(),
        origin.mTarget.getPosition(),
        target,
        origin.mDamage,
        origin.mBounceCount - 1,
        origin.mMaxBounceDist,
    ) {
        mOrigin = origin.mTarget

        mPrevTargets = origin.mPrevTargets
        mPrevTargets!!.add(target)
    }

    override fun init() {
        super.init()

        getGameEngine().add(mDrawObject)
    }

    override fun clean() {
        super.clean()

        getGameEngine().remove(mDrawObject)
    }

    override fun tick() {
        super.tick()

        mDrawObject.decreaseVisibility()

        if (mOrigin != null) {
            setPosition(mOrigin!!.getPosition())
        }

        mTargetPos = mTarget.getPosition()
    }

    override fun effectBegin() {
        if (mBounceCount > 0) {
            if (mPrevTargets == null) {
                mPrevTargets = ArrayList()
                mPrevTargets!!.add(mTarget)
            }

            val enemy = getGameEngine().getEntitiesByType(EntityTypes.ENEMY)
                .filter(mPrevTargets!!)
                .min(distanceTo(mTarget.getPosition())) as Enemy?

            if (enemy != null && mTarget.getDistanceTo(enemy) <= mMaxBounceDist) {
                getGameEngine().add(BouncingLaser(this, enemy))
            }
        }

        mTarget.damage(mDamage, getOrigin())

        if (mTarget is Flyer) {
            mTarget.modifySpeed(1.0f / FLYER_STUN_INTENSITY, getOrigin())
        }
    }

    override fun effectEnd() {
        if (mTarget is Flyer) {
            mTarget.modifySpeed(FLYER_STUN_INTENSITY, getOrigin())
        }
    }

    companion object {
        private const val FLYER_STUN_INTENSITY = 20.0f
        private const val EFFECT_DURATION = 0.5f
        private const val VISIBLE_EFFECT_DURATION = 0.5f
        private const val ALPHA_START = 180
        private val ALPHA_STEP =
            (ALPHA_START / (GameEngine.TARGET_FRAME_RATE * VISIBLE_EFFECT_DURATION)).toInt()
    }
}
