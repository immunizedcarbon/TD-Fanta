package com.tdfanta.game.engine.sound

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.tdfanta.game.Preferences

class SoundManager(context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {
    private val mPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    private var mSoundEnabled = false

    init {
        mPreferences.registerOnSharedPreferenceChangeListener(this)
        updateSoundEnabled()
    }

    fun isSoundEnabled(): Boolean = mSoundEnabled

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (Preferences.SOUND_ENABLED == key) {
            updateSoundEnabled()
        }
    }

    private fun updateSoundEnabled() {
        mSoundEnabled = mPreferences.getBoolean(Preferences.SOUND_ENABLED, true)
    }
}
