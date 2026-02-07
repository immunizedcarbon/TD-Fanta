package com.tdfanta.game.engine.sound

import android.media.SoundPool

class Sound(
    private val mSoundManager: SoundManager,
    private val mSoundPool: SoundPool,
    private val mSoundId: Int,
) {
    private var mVolume = 1f

    fun setVolume(volume: Float) {
        mVolume = volume
    }

    fun play() {
        if (mSoundManager.isSoundEnabled()) {
            mSoundPool.play(mSoundId, mVolume, mVolume, 0, 0, 1f)
        }
    }
}
