package com.tdfanta.game.engine.render

import android.graphics.Matrix
import android.graphics.RectF
import com.tdfanta.game.util.math.Vector2

class Viewport {
    private lateinit var mScreenMatrix: Matrix
    private lateinit var mScreenMatrixInverse: Matrix
    private var mGameWidth = 0f
    private var mGameHeight = 0f
    private var mScreenWidth = 0f
    private var mScreenHeight = 0f
    private lateinit var mGameClipRect: RectF
    private lateinit var mScreenGameRect: RectF

    fun setGameSize(width: Int, height: Int) {
        mGameWidth = width.toFloat()
        mGameHeight = height.toFloat()
        mGameClipRect = RectF(-0.5f, -0.5f, mGameWidth - 0.5f, mGameHeight - 0.5f)
        calcScreenMatrix()
    }

    fun setScreenSize(width: Int, height: Int) {
        mScreenWidth = width.toFloat()
        mScreenHeight = height.toFloat()
        calcScreenMatrix()
    }

    fun getScreenMatrix(): Matrix = mScreenMatrix

    fun getGameClipRect(): RectF = mGameClipRect

    fun getScreenGameRect(): RectF = mScreenGameRect

    fun screenToGame(pos: Vector2): Vector2 {
        val points = floatArrayOf(pos.x(), pos.y())
        mScreenMatrixInverse.mapPoints(points)
        return Vector2(points[0], points[1])
    }

    private fun calcScreenMatrix() {
        mScreenMatrix = Matrix()

        val tileSize = kotlin.math.min(mScreenWidth / mGameWidth, mScreenHeight / mGameHeight)
        val width = tileSize * mGameWidth
        val height = tileSize * mGameHeight

        val left = (mScreenWidth - width) / 2f
        val top = 0f
        val right = left + width
        val bottom = top + height

        mScreenGameRect = RectF(left, top, right, bottom)

        mScreenMatrix.postTranslate(0.5f, 0.5f)
        mScreenMatrix.postScale(tileSize, tileSize)

        val paddingLeft = (mScreenWidth - width) / 2f
        val paddingBottom = mScreenHeight - height
        mScreenMatrix.postTranslate(paddingLeft, paddingBottom)

        mScreenMatrix.postScale(1f, -1f)
        mScreenMatrix.postTranslate(0f, mScreenHeight)

        mScreenMatrixInverse = Matrix()
        mScreenMatrix.invert(mScreenMatrixInverse)
    }
}
