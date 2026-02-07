package com.tdfanta.game.engine.logic.loop

import com.tdfanta.game.engine.logic.persistence.Persister
import com.tdfanta.game.util.container.KeyValueStore
import java.util.ArrayList

class MessageQueue : Persister {
    private data class MessageEntry(val mMessage: Message, val mDueTickCount: Long)

    private val mQueue = ArrayList<MessageEntry>()
    private var mTickCount = 0

    fun getTickCount(): Int = mTickCount

    @Synchronized
    fun post(message: Message) {
        postAfterTicks(message, 0)
    }

    @Synchronized
    fun postAfterTicks(message: Message, ticks: Int) {
        val dueTickCount = mTickCount.toLong() + ticks

        for (i in mQueue.indices) {
            if (dueTickCount < mQueue[i].mDueTickCount) {
                mQueue.add(i, MessageEntry(message, dueTickCount))
                return
            }
        }

        mQueue.add(MessageEntry(message, dueTickCount))
    }

    @Synchronized
    fun clear() {
        mQueue.clear()
    }

    @Synchronized
    fun tick() {
        mTickCount++
    }

    @Synchronized
    fun processMessages() {
        while (mQueue.isNotEmpty() && mTickCount.toLong() >= mQueue[0].mDueTickCount) {
            val messageEntry = mQueue.removeAt(0)
            messageEntry.mMessage.execute()
        }
    }

    override fun resetState() {
        mTickCount = 0
    }

    override fun writeState(gameState: KeyValueStore) {
        gameState.putInt("tickCount", mTickCount)
    }

    override fun readState(gameState: KeyValueStore) {
        mTickCount = gameState.getInt("tickCount")
    }
}
