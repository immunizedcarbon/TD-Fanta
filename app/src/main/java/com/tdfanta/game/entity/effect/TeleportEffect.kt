package com.tdfanta.game.entity.effect

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.render.Drawable
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.entity.enemy.Enemy
import com.tdfanta.game.util.math.Vector2

class TeleportEffect(
    origin: Entity,
    position: Vector2,
    target: Enemy,
    private val mDistance: Float,
) : Effect(origin, EFFECT_DURATION), Entity.Listener {
    private class StaticData {
        lateinit var mPaint: Paint
    }

    private inner class TeleportDrawable : Drawable {
        override fun getLayer(): Int = Layers.SHOT

        override fun draw(canvas: Canvas) {
            val target = mTarget!!.getPosition()
            canvas.drawLine(getPosition().x(), getPosition().y(), target.x(), target.y(), mStaticData.mPaint)
        }
    }

    private var mTarget: Enemy? = target

    private val mMoveDirection: Vector2
    private val mMoveStep: Float
    private val mDrawObject = TeleportDrawable()
    private lateinit var mStaticData: StaticData

    init {
        setPosition(position)

        target.startTeleport()

        mTarget!!.addListener(this)

        mMoveDirection = target.getDirectionTo(this)
        mMoveStep = target.getDistanceTo(this) / EFFECT_DURATION / GameEngine.TARGET_FRAME_RATE
    }

    override fun initStatic(): Any {
        val s = StaticData()

        s.mPaint = Paint()
        s.mPaint.style = Paint.Style.STROKE
        s.mPaint.strokeWidth = 0.1f
        s.mPaint.color = Color.MAGENTA
        s.mPaint.alpha = 70

        return s
    }

    override fun init() {
        super.init()
        mStaticData = getStaticData() as StaticData
        getGameEngine().add(mDrawObject)
    }

    override fun clean() {
        super.clean()
        getGameEngine().remove(mDrawObject)
    }

    override fun tick() {
        super.tick()
        mTarget?.move(Vector2.mul(mMoveDirection, mMoveStep))
    }

    override fun entityRemoved(entity: Entity) {
        mTarget = null
        remove()
    }

    override fun effectEnd() {
        if (mTarget != null) {
            mTarget!!.sendBack(mDistance)
            mTarget!!.finishTeleport()
        }
    }

    companion object {
        private const val EFFECT_DURATION = 1f
    }
}
