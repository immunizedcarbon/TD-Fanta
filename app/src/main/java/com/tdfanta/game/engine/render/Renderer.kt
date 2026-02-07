package com.tdfanta.game.engine.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.tdfanta.game.engine.logic.loop.FrameRateLogger
import com.tdfanta.game.util.container.SafeMultiMap
import com.tdfanta.game.util.math.Vector2
import java.lang.ref.WeakReference
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class Renderer(
    private val mViewport: Viewport,
    private val mFrameRateLogger: FrameRateLogger,
) {
    private val mDrawables = SafeMultiMap<Drawable>()
    private val mLock: Lock = ReentrantLock(true)

    private var mBackgroundColor = 0
    private var mViewRef = WeakReference<android.view.View?>(null)

    fun setView(view: android.view.View?) {
        mViewRef = WeakReference(view)
    }

    fun add(obj: Drawable) {
        mDrawables.add(obj.getLayer(), obj)
    }

    fun remove(obj: Drawable) {
        mDrawables.remove(obj.getLayer(), obj)
    }

    fun clear() {
        mDrawables.clear()
    }

    fun lock() {
        mLock.lock()
    }

    fun unlock() {
        mLock.unlock()
    }

    fun invalidate() {
        val view = mViewRef.get()
        view?.postInvalidate()
    }

    fun getScreenshot(): Bitmap {
        val mapRect = mViewport.getScreenGameRect()
        val bitmap = Bitmap.createBitmap(
            kotlin.math.round(mapRect.width()).toInt(),
            kotlin.math.round(mapRect.height()).toInt(),
            Bitmap.Config.ARGB_8888,
        )
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }

    fun draw(canvas: Canvas) {
        mLock.lock()

        val clearColor = if (mBackgroundColor != 0) mBackgroundColor else Color.BLACK
        canvas.drawColor(clearColor)
        canvas.concat(mViewport.getScreenMatrix())
        canvas.clipRect(mViewport.getGameClipRect())
        canvas.drawColor(mBackgroundColor)

        for (obj in mDrawables) {
            obj.draw(canvas)
        }

        mLock.unlock()

        mFrameRateLogger.incrementRenderCount()
    }

    fun setBackgroundColor(backgroundColor: Int) {
        mBackgroundColor = backgroundColor
    }

    fun isPositionVisible(position: Vector2): Boolean =
        mViewport.getGameClipRect().contains(position.x(), position.y())
}
