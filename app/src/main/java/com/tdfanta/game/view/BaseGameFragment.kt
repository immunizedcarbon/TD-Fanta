package com.tdfanta.game.view

import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.tdfanta.game.Preferences

open class BaseGameFragment : Fragment() {
    protected fun updateMenuTransparency() {
        val fragmentView = view

        if (fragmentView != null) {
            val fragmentActivity = activity ?: return
            val preferences = PreferenceManager.getDefaultSharedPreferences(fragmentActivity)
            val transparentMenusEnabled =
                preferences.getBoolean(Preferences.TRANSPARENT_MENUS_ENABLED, false)

            if (transparentMenusEnabled) {
                fragmentView.alpha = 0.73f
            } else {
                fragmentView.alpha = 1.0f
            }
        }
    }
}
