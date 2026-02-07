package com.tdfanta.game.entity.tower

import android.graphics.Canvas
import android.graphics.Paint
import com.tdfanta.game.R
import com.tdfanta.game.engine.render.Drawable
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.theme.Theme
import com.tdfanta.game.util.math.Vector2

class LevelIndicator(theme: Theme, private val mTower: Tower) : Drawable {
    init {
        if (mText == null) {
            mText = Paint()
            mText!!.style = Paint.Style.FILL
            mText!!.color = theme.getColor(R.attr.levelIndicatorColor)
            mText!!.textSize = 100f
        }
    }

    override fun draw(canvas: Canvas) {
        val pos: Vector2 = mTower.getPosition()

        canvas.save()
        canvas.translate(pos.x(), pos.y())
        canvas.scale(0.0075f, -0.0075f)
        val text = mTower.getLevel().toString()
        val height = mText!!.ascent() + mText!!.descent()
        val width = mText!!.measureText(text)
        canvas.drawText(text, -width / 2f, -height / 2f, mText!!)
        canvas.restore()
    }

    override fun getLayer(): Int = Layers.TOWER_LEVEL

    companion object {
        private var mText: Paint? = null
    }
}
