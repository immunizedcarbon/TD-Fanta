package com.tdfanta.game.entity.effect

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.render.Drawable
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.sound.Sound
import com.tdfanta.game.entity.EntityTypes
import com.tdfanta.game.entity.enemy.Enemy
import com.tdfanta.game.util.math.Vector2

class Explosion(
    origin: Entity,
    position: Vector2,
    private val mDamage: Float,
    private val mRadius: Float,
) : Effect(origin, EFFECT_DURATION) {
    private inner class ExplosionDrawable : Drawable {
        private val mPaint = Paint()
        private var mAlpha = ALPHA_START

        init {
            mPaint.color = Color.YELLOW
            mPaint.alpha = mAlpha
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
            canvas.drawCircle(getPosition().x(), getPosition().y(), mRadius, mPaint)
        }
    }

    private val mDrawObject = ExplosionDrawable()
    private val mSound: Sound = getSoundFactory().createSound(R.raw.explosive3_bghgh)

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
        mSound.play()

        val enemies = getGameEngine().getEntitiesByType(EntityTypes.ENEMY)
            .filter(inRange(getPosition(), mRadius))
            .cast(Enemy::class.java)

        while (enemies.hasNext()) {
            val enemy = enemies.next()
            enemy.damage(mDamage, getOrigin())
        }
    }

    companion object {
        private const val EFFECT_DURATION = 0.2f
        private const val ALPHA_START = 180
        private val ALPHA_STEP = (ALPHA_START / (GameEngine.TARGET_FRAME_RATE * EFFECT_DURATION)).toInt()
    }
}
