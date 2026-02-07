package com.tdfanta.game.engine.logic.loop

import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

class FrameRateLogger {
    private val mLoopCount = AtomicInteger()
    private val mRenderCount = AtomicInteger()
    private var mLastOutputTime = 0L

    fun incrementLoopCount() {
        mLoopCount.incrementAndGet()
    }

    fun incrementRenderCount() {
        mRenderCount.incrementAndGet()
    }

    fun outputFrameRate() {
        val currentTime = System.currentTimeMillis()
        val sinceLastOutput = currentTime - mLastOutputTime

        if (sinceLastOutput >= LOG_INTERVAL) {
            var loopCount = mLoopCount.getAndSet(0).toLong()
            var renderCount = mRenderCount.getAndSet(0).toLong()

            loopCount = loopCount * 1000 / sinceLastOutput
            renderCount = renderCount * 1000 / sinceLastOutput
            Log.d(TAG, String.format("loop: %1\$sHz; render: %2\$sHz", loopCount, renderCount))

            mLastOutputTime = currentTime
        }
    }

    companion object {
        private val TAG = FrameRateLogger::class.java.simpleName
        private const val LOG_INTERVAL = 5000
    }
}
