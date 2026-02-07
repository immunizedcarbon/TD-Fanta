package com.tdfanta.game.entity.effect

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.loop.TickListener
import com.tdfanta.game.engine.render.Drawable
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.util.math.Function
import com.tdfanta.game.util.math.SampledFunction

class TeleportedMarker(private val mMarked: Entity) : Effect(mMarked), Entity.Listener {
    private class StaticData : TickListener {
        lateinit var mScaleFunction: SampledFunction
        lateinit var mPaint: Paint

        override fun tick() {
            mScaleFunction.step()
        }
    }

    private inner class MarkerDrawable : Drawable {
        override fun getLayer(): Int = Layers.SHOT

        override fun draw(canvas: Canvas) {
            canvas.drawCircle(
                getPosition().x(),
                getPosition().y(),
                mStaticData.mScaleFunction.getValue(),
                mStaticData.mPaint,
            )
        }
    }

    private lateinit var mStaticData: StaticData
    private val mDrawable = MarkerDrawable()

    init {
        mMarked.addListener(this)
    }

    override fun initStatic(): Any {
        val s = StaticData()

        s.mScaleFunction = Function.sine()
            .multiply((MARKER_MAX_RADIUS - MARKER_MIN_RADIUS) / 2)
            .offset((MARKER_MAX_RADIUS + MARKER_MIN_RADIUS) / 2)
            .stretch(GameEngine.TARGET_FRAME_RATE / MARKER_SPEED / Math.PI.toFloat())
            .sample()

        s.mPaint = Paint()
        s.mPaint.style = Paint.Style.FILL
        s.mPaint.color = Color.MAGENTA
        s.mPaint.alpha = 30

        getGameEngine().add(s)
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

        setPosition(mMarked.getPosition())
    }

    override fun entityRemoved(entity: Entity) {
        remove()
    }

    companion object {
        private const val MARKER_MIN_RADIUS = 0.1f
        private const val MARKER_MAX_RADIUS = 0.2f
        private const val MARKER_SPEED = 1f
    }
}
