package com.tdfanta.game.engine.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.tdfanta.game.R

class SoundFactory(
    private val mContext: Context,
    private val mSoundManager: SoundManager,
) {
    private val mSoundPool = createSoundPool()
    private val mSoundMap = HashMap<Int, Int>()

    init {
        // FIXME: This is a workaround because the first explosion effect has no sound otherwise
        createSound(R.raw.explosive3_bghgh)
    }

    fun createSound(resId: Int): Sound {
        if (!mSoundMap.containsKey(resId)) {
            val soundId = mSoundPool.load(mContext, resId, 0)
            mSoundMap[resId] = soundId
        }

        val soundId = checkNotNull(mSoundMap[resId]) { "Sound resource was not loaded: $resId" }
        return Sound(mSoundManager, mSoundPool, soundId)
    }

    companion object {
        private const val MAX_STREAMS = 8

        private fun createSoundPool(): SoundPool {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build()
            return SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(MAX_STREAMS)
                .build()
        }
    }
}
