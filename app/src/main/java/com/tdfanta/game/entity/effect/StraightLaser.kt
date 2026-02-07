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

class StraightLaser(
    origin: Entity,
    position: Vector2,
    private val mLaserTo: Vector2,
    private val mDamage: Float,
) : Effect(origin, EFFECT_DURATION) {
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
            canvas.drawLine(getPosition().x(), getPosition().y(), mLaserTo.x(), mLaserTo.y(), mPaint)
        }
    }

    private val mStunnedFliers = ArrayList<Flyer>()

    private val mDrawObject = LaserDrawable()

    init {
        setPosition(position)
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
    }

    override fun effectBegin() {
        val enemies = getGameEngine().getEntitiesByType(EntityTypes.ENEMY)
            .filter(onLine(getPosition(), mLaserTo, LASER_WIDTH))
            .cast(Enemy::class.java)

        while (enemies.hasNext()) {
            val enemy = enemies.next()
            enemy.damage(mDamage, getOrigin())

            if (enemy is Flyer) {
                enemy.modifySpeed(1.0f / FLYER_STUN_INTENSITY, getOrigin())
                mStunnedFliers.add(enemy)
            }
        }
    }

    override fun effectEnd() {
        for (flyer in mStunnedFliers) {
            flyer.modifySpeed(FLYER_STUN_INTENSITY, getOrigin())
        }
    }

    companion object {
        private const val LASER_WIDTH = 0.7f

        private const val FLYER_STUN_INTENSITY = 20.0f
        private const val EFFECT_DURATION = 1.0f
        private const val VISIBLE_EFFECT_DURATION = 0.5f
        private const val ALPHA_START = 180
        private val ALPHA_STEP =
            (ALPHA_START / (GameEngine.TARGET_FRAME_RATE * VISIBLE_EFFECT_DURATION)).toInt()
    }
}
