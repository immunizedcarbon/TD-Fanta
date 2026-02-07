package com.tdfanta.game.engine.render.sprite

import android.graphics.Bitmap
import android.graphics.Matrix
import com.tdfanta.game.util.math.Vector2
import java.util.Arrays

class SpriteTemplate(vararg bitmaps: Bitmap) {
    private val mBitmaps: List<Bitmap> = Arrays.asList(*bitmaps)
    private val mMatrix = Matrix()

    fun getBitmaps(): List<Bitmap> = mBitmaps

    fun getBitmapCount(): Int = mBitmaps.size

    fun getMatrix(): Matrix = mMatrix

    fun setMatrix(src: Matrix) {
        mMatrix.set(src)
    }

    fun setMatrix(width: Float?, height: Float?, center: Vector2?, rotate: Float?) {
        val aspect = mBitmaps[0].width.toFloat() / mBitmaps[0].height

        var resolvedWidth = width
        var resolvedHeight = height

        if (resolvedWidth == null && resolvedHeight == null) {
            resolvedHeight = 1f
        }

        if (resolvedWidth == null) {
            resolvedWidth = resolvedHeight!! * aspect
        }

        if (resolvedHeight == null) {
            resolvedHeight = resolvedWidth / aspect
        }

        val resolvedCenter = center ?: Vector2(resolvedWidth / 2f, resolvedHeight / 2f)
        val scaleX = resolvedWidth / mBitmaps[0].width
        val scaleY = resolvedHeight / mBitmaps[0].height

        mMatrix.reset()

        mMatrix.postScale(1f, -1f)
        mMatrix.postTranslate(0f, mBitmaps[0].height.toFloat())

        mMatrix.postScale(scaleX, scaleY)
        mMatrix.postTranslate(-resolvedCenter.x(), -resolvedCenter.y())

        if (rotate != null) {
            mMatrix.postRotate(rotate)
        }
    }
}
