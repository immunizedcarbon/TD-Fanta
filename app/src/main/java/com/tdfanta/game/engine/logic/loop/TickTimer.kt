package com.tdfanta.game.engine.logic.loop

import com.tdfanta.game.engine.logic.GameEngine

class TickTimer {
    private var mReloadValue = 0f
    private var mValue = 0f

    fun setInterval(interval: Float) {
        mValue = GameEngine.TARGET_FRAME_RATE * interval
        mReloadValue = mValue
    }

    fun reset() {
        mValue = mReloadValue
    }

    fun tick(): Boolean {
        mValue -= 1f

        if (mValue <= 0f) {
            mValue += mReloadValue
            return true
        }

        return false
    }

    companion object {
        @JvmStatic
        fun createInterval(interval: Float): TickTimer {
            val timer = TickTimer()
            timer.setInterval(interval)
            return timer
        }
    }
}
