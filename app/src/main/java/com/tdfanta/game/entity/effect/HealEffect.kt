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
import com.tdfanta.game.util.math.Vector2

class HealEffect(
    origin: Entity,
    position: Vector2,
    private val mHealAmount: Float,
    private val mRange: Float,
    private val mHealedEnemies: MutableCollection<Enemy>,
) : Effect(origin, EFFECT_DURATION) {
    private class StaticData {
        lateinit var mPaint: Paint
    }

    private inner class HealDrawable : Drawable {
        override fun getLayer(): Int = Layers.SHOT

        override fun draw(canvas: Canvas) {
            canvas.drawCircle(getPosition().x(), getPosition().y(), mDrawRadius, mStaticData.mPaint)
        }
    }

    private var mDrawRadius = 0f

    private val mDrawable: Drawable = HealDrawable()
    private lateinit var mStaticData: StaticData

    init {
        setPosition(position)
    }

    override fun initStatic(): Any {
        val s = StaticData()
        s.mPaint = Paint()

        s.mPaint.style = Paint.Style.STROKE
        s.mPaint.strokeWidth = 0.05f
        s.mPaint.color = Color.BLUE
        s.mPaint.alpha = 70
        return s
    }

    override fun init() {
        super.init()

        mStaticData = getStaticData() as StaticData
        getGameEngine().add(mDrawable)
    }

    override fun clean() {
        super.clean()

        getGameEngine().remove(mDrawable)
    }

    override fun tick() {
        super.tick()

        mDrawRadius += mRange / (GameEngine.TARGET_FRAME_RATE * EFFECT_DURATION)
    }

    override fun effectBegin() {
        val enemies = getGameEngine().getEntitiesByType(EntityTypes.ENEMY)
            .filter(inRange(getPosition(), mRange))
            .filter(mHealedEnemies)
            .cast(Enemy::class.java)

        while (enemies.hasNext()) {
            val enemy = enemies.next()
            enemy.heal(mHealAmount * enemy.getMaxHealth())
            mHealedEnemies.add(enemy)
        }
    }

    companion object {
        private const val EFFECT_DURATION = 0.7f
    }
}
