package com.tdfanta.game.engine.render.sprite

import com.tdfanta.game.engine.logic.loop.TickTimer

class AnimatedSprite(layer: Int, template: SpriteTemplate) : SpriteInstance(layer, template) {
    private val mTimer = TickTimer()
    private var mSequenceIndex = 0
    private lateinit var mSequence: IntArray

    override fun getIndex(): Int = mSequence[mSequenceIndex]

    fun getSequenceIndex(): Int = mSequenceIndex

    fun setFrequency(frequency: Float) {
        setInterval(1f / frequency)
    }

    fun setInterval(interval: Float) {
        mTimer.setInterval(interval / mSequence.size)
    }

    fun setSequence(sequence: IntArray) {
        mSequence = sequence
        reset()
    }

    fun setSequenceForward() {
        val bitmapCount = getTemplate().getBitmapCount()
        val sequence = IntArray(bitmapCount)

        for (i in sequence.indices) {
            sequence[i] = i
        }

        setSequence(sequence)
    }

    fun setSequenceForwardBackward() {
        val bitmapCount = getTemplate().getBitmapCount()
        val sequence = IntArray(bitmapCount * 2 - 2)

        for (i in sequence.indices) {
            sequence[i] = if (i < bitmapCount) {
                i
            } else {
                bitmapCount * 2 - 2 - i
            }
        }

        setSequence(sequence)
    }

    fun setSequenceBackward() {
        val bitmapCount = getTemplate().getBitmapCount()
        val sequence = IntArray(bitmapCount)

        for (i in sequence.indices) {
            sequence[i] = bitmapCount - 1 - i
        }

        setSequence(sequence)
    }

    fun reset() {
        mTimer.reset()
        mSequenceIndex = 0
    }

    fun tick(): Boolean {
        var ret = false

        if (mTimer.tick()) {
            if (mSequenceIndex >= mSequence.size - 1) {
                mSequenceIndex = 0
                ret = true
            } else {
                mSequenceIndex++
            }
        }

        return ret
    }
}
