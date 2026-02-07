package com.tdfanta.game.engine.render.sprite

import android.graphics.Canvas
import android.graphics.Paint
import com.tdfanta.game.engine.render.Drawable

abstract class SpriteInstance(
    private val mLayer: Int,
    private val mTemplate: SpriteTemplate,
) : Drawable {
    private var mPaint: Paint? = null
    private var mListener: SpriteTransformation? = null

    fun getTemplate(): SpriteTemplate = mTemplate

    abstract fun getIndex(): Int

    fun setListener(listener: SpriteTransformation?) {
        mListener = listener
    }

    fun setPaint(paint: Paint?) {
        mPaint = paint
    }

    override fun getLayer(): Int = mLayer

    override fun draw(canvas: Canvas) {
        canvas.save()

        mListener?.draw(this, canvas)

        val bitmap = mTemplate.getBitmaps()[getIndex()]
        val matrix = mTemplate.getMatrix()
        canvas.drawBitmap(bitmap, matrix, mPaint)
        canvas.restore()
    }
}
