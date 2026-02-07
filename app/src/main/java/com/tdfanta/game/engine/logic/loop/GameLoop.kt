package com.tdfanta.game.engine.logic.loop

import android.util.Log
import com.tdfanta.game.engine.logic.entity.EntityStore
import com.tdfanta.game.engine.render.Renderer
import com.tdfanta.game.util.container.SafeCollection
import java.util.concurrent.CopyOnWriteArrayList

class GameLoop(
    private val mRenderer: Renderer,
    private val mFrameRateLogger: FrameRateLogger,
    private val mMessageQueue: MessageQueue,
    private val mEntityStore: EntityStore,
) : Runnable {
    private val mTickListeners: MutableCollection<TickListener> = SafeCollection()
    private val mErrorListeners: MutableCollection<ErrorListener> = CopyOnWriteArrayList()

    private var mGameTicksPerLoop = 1
    private var mGameThread: Thread? = null
    @Volatile private var mRunning = false

    fun registerErrorListener(listener: ErrorListener) {
        mErrorListeners.add(listener)
    }

    fun add(listener: TickListener) {
        mTickListeners.add(listener)
    }

    fun remove(listener: TickListener) {
        mTickListeners.remove(listener)
    }

    fun clear() {
        mTickListeners.clear()
    }

    fun isRunning(): Boolean = mRunning

    fun start() {
        if (!mRunning) {
            Log.i(TAG, "Starting game loop")
            mRunning = true
            mGameThread = Thread(this)
            mGameThread?.start()
        }
    }

    fun stop() {
        if (mRunning) {
            Log.i(TAG, "Stopping game loop")
            mRunning = false

            try {
                mGameThread?.join()
            } catch (e: InterruptedException) {
                throw RuntimeException("Could not stop game thread!", e)
            }
        }
    }

    fun setTicksPerLoop(ticksPerLoop: Int) {
        mGameTicksPerLoop = ticksPerLoop
    }

    fun isThreadChangeNeeded(): Boolean = Thread.currentThread() != mGameThread

    override fun run() {
        var timeNextTick = System.currentTimeMillis()
        var skipFrameCount = 0
        var loopCount = 0

        try {
            while (mRunning) {
                executeCycle()

                timeNextTick += TICK_TIME.toLong()
                val sleepTime = (timeNextTick - System.currentTimeMillis()).toInt()

                if (sleepTime > 0 || skipFrameCount >= MAX_FRAME_SKIPS) {
                    mRenderer.invalidate()
                    skipFrameCount = 0
                } else {
                    skipFrameCount++
                }

                if (sleepTime > 0) {
                    Thread.sleep(sleepTime.toLong())
                } else {
                    timeNextTick = System.currentTimeMillis() // resync
                }

                loopCount++
            }

            // process messages a last time (needed to save game just before loop stops)
            mMessageQueue.processMessages()
        } catch (e: Throwable) {
            mRunning = false
            val exception = if (e is Exception) e else RuntimeException(e)
            notifyErrorListeners(loopCount, exception)
            Log.e(TAG, "Game loop crashed: ${e.javaClass.simpleName}")
            throw RuntimeException("Error in game loop!", e)
        }
    }

    private fun executeCycle() {
        mRenderer.lock()
        for (i in 0 until mGameTicksPerLoop) {
            executeTick()
            mMessageQueue.processMessages()
        }
        mRenderer.unlock()

        mFrameRateLogger.incrementLoopCount()
        mFrameRateLogger.outputFrameRate()
    }

    private fun executeTick() {
        mMessageQueue.tick()
        mEntityStore.tick()

        for (listener in mTickListeners) {
            listener.tick()
        }
    }

    private fun notifyErrorListeners(loopCount: Int, e: Exception) {
        for (listener in mErrorListeners) {
            listener.error(e, loopCount)
        }
    }

    companion object {
        private val TAG = GameLoop::class.java.simpleName
        const val TARGET_FRAME_RATE = 30
        private const val TICK_TIME = 1000 / TARGET_FRAME_RATE
        private const val MAX_FRAME_SKIPS = 1
    }
}
