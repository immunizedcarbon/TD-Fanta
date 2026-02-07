package com.tdfanta.game.entity.tower

import android.graphics.Canvas
import android.graphics.Paint
import com.tdfanta.game.R
import com.tdfanta.game.engine.render.Drawable
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.theme.Theme

class RangeIndicator(theme: Theme, private val mTower: Tower) : Drawable {
    init {
        if (mPen == null) {
            mPen = Paint()
            mPen!!.style = Paint.Style.STROKE
            mPen!!.strokeWidth = 0.05f
            mPen!!.color = theme.getColor(R.attr.rangeIndicatorColor)
        }
    }

    override fun getLayer(): Int = Layers.TOWER_RANGE

    override fun draw(canvas: Canvas) {
        canvas.drawCircle(mTower.getPosition().x(), mTower.getPosition().y(), mTower.getRange(), mPen!!)
    }

    companion object {
        private var mPen: Paint? = null
    }
}
