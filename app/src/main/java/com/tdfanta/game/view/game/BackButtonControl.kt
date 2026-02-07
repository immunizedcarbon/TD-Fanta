package com.tdfanta.game.view.game

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.tdfanta.game.Preferences

class BackButtonControl(context: Context) {
    enum class BackButtonAction {
        DO_NOTHING,
        SHOW_TOAST,
        EXIT,
    }

    private enum class BackButtonMode {
        DISABLED,
        ENABLED,
        TWICE,
    }

    private val mPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private var mLastBackButtonPress = 0L

    fun backButtonPressed(): BackButtonAction {
        val timeNow = System.currentTimeMillis()

        return when (getBackButtonMode()) {
            BackButtonMode.ENABLED -> BackButtonAction.EXIT
            BackButtonMode.TWICE -> {
                if (timeNow < mLastBackButtonPress + BACK_TWICE_INTERVAL) {
                    BackButtonAction.EXIT
                } else {
                    mLastBackButtonPress = timeNow
                    BackButtonAction.SHOW_TOAST
                }
            }

            else -> BackButtonAction.DO_NOTHING
        }
    }

    private fun getBackButtonMode(): BackButtonMode {
        if (mPreferences.contains(Preferences.BACK_BUTTON_CLOSE_ENABLED)) {
            return getBackButtonModeFromCheckbox()
        }

        return getLegacyBackButtonMode()
    }

    private fun getBackButtonModeFromCheckbox(): BackButtonMode {
        return try {
            if (mPreferences.getBoolean(Preferences.BACK_BUTTON_CLOSE_ENABLED, false)) {
                BackButtonMode.ENABLED
            } else {
                BackButtonMode.DISABLED
            }
        } catch (_: ClassCastException) {
            BackButtonMode.DISABLED
        }
    }

    private fun getLegacyBackButtonMode(): BackButtonMode {
        val backModeString = mPreferences.getString(Preferences.BACK_BUTTON_MODE, null)
        val modeName = backModeString ?: return BackButtonMode.DISABLED

        return try {
            BackButtonMode.valueOf(modeName)
        } catch (_: Exception) {
            BackButtonMode.DISABLED
        }
    }

    companion object {
        private const val BACK_TWICE_INTERVAL = 2000L
    }
}
