package com.tdfanta.game.view.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.tdfanta.game.TDFantaApplication
import com.tdfanta.game.GameFactory
import com.tdfanta.game.R
import com.tdfanta.game.engine.theme.Theme
import com.tdfanta.game.entity.tower.Tower

class TowerView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var mPreviewTower: Tower? = null

    private var mTextColor = 0
    private var mTextColorDisabled = 0
    private val mPaintText: Paint
    private val mScreenMatrix: Matrix

    init {
        if (!isInEditMode) {
            val factory: GameFactory = TDFantaApplication.getInstance().getGameFactory()
            val theme: Theme = factory.getThemeManager().getTheme()
            mTextColor = theme.getColor(R.attr.textColor)
            mTextColorDisabled = theme.getColor(R.attr.textDisabledColor)
        }

        val density = context.resources.displayMetrics.density
        mPaintText = Paint()
        mPaintText.color = mTextColor
        mPaintText.textAlign = Paint.Align.CENTER
        mPaintText.textSize = TEXT_SIZE * density

        mScreenMatrix = Matrix()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        mPaintText.color = if (enabled) mTextColor else mTextColorDisabled
        postInvalidate()
    }

    fun setPreviewTower(tower: Tower) {
        mPreviewTower = tower
        postInvalidate()
    }

    fun getTowerValue(): Int {
        if (mPreviewTower == null) {
            return 0
        }

        return mPreviewTower!!.getValue()
    }

    fun getTowerName(): String = mPreviewTower!!.getEntityName()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mScreenMatrix.reset()

        val tileSize = kotlin.math.min(w, h).toFloat()
        mScreenMatrix.postTranslate(DRAW_SIZE / 2f, DRAW_SIZE / 2f)
        mScreenMatrix.postScale(tileSize / DRAW_SIZE, tileSize / DRAW_SIZE)

        val paddingLeft = (w - tileSize) / 2f
        val paddingTop = (h - tileSize) / 2f
        mScreenMatrix.postTranslate(paddingLeft, paddingTop)

        mScreenMatrix.postScale(1f, -1f)
        mScreenMatrix.postTranslate(0f, h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mPreviewTower != null) {
            canvas.save()
            canvas.concat(mScreenMatrix)
            mPreviewTower!!.preview(canvas)
            canvas.restore()

            canvas.drawText(
                Integer.toString(mPreviewTower!!.getValue()),
                width / 2.0f,
                height / 2.0f - (mPaintText.ascent() + mPaintText.descent()) / 2f,
                mPaintText,
            )
        }
    }

    companion object {
        private const val TEXT_SIZE = 20f
        private const val DRAW_SIZE = 1.3f
    }
}
