package com.tdfanta.game.business.game

import com.tdfanta.game.engine.logic.GameEngine
import java.util.concurrent.CopyOnWriteArrayList

class GameSpeed(private val mGameEngine: GameEngine) {
    interface Listener {
        fun gameSpeedChanged()
    }

    private val mListeners = CopyOnWriteArrayList<Listener>()
    private var mFastForwardActive = false
    private var mFastForwardMultiplier = MIN_FAST_FORWARD_SPEED

    fun isFastForwardActive(): Boolean = mFastForwardActive

    fun setFastForwardActive(active: Boolean) {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { setFastForwardActive(active) }
            return
        }

        if (mFastForwardActive != active) {
            mFastForwardActive = active
            updateTicks()
        }
    }

    fun fastForwardMultiplier(): Int = mFastForwardMultiplier

    fun cycleFastForward() {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { cycleFastForward() }
            return
        }

        mFastForwardMultiplier = if (mFastForwardMultiplier < MAX_FAST_FORWARD_SPEED) {
            mFastForwardMultiplier * 2
        } else {
            MIN_FAST_FORWARD_SPEED
        }

        updateTicks()
    }

    fun addListener(listener: Listener) {
        mListeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        mListeners.remove(listener)
    }

    private fun updateTicks() {
        if (mFastForwardActive) {
            mGameEngine.setTicksPerLoop(mFastForwardMultiplier)
        } else {
            mGameEngine.setTicksPerLoop(1)
        }

        for (listener in mListeners) {
            listener.gameSpeedChanged()
        }
    }

    companion object {
        private const val MIN_FAST_FORWARD_SPEED = 2
        private const val MAX_FAST_FORWARD_SPEED = 32
    }
}
