package com.tdfanta.game.engine.theme

import android.content.res.Configuration
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.tdfanta.game.Preferences
import com.tdfanta.game.R
import com.tdfanta.game.engine.render.Renderer
import java.util.concurrent.CopyOnWriteArrayList

class ThemeManager(context: Context, private val mRenderer: Renderer) :
    SharedPreferences.OnSharedPreferenceChangeListener {
    interface Listener {
        fun themeChanged(theme: Theme)
    }

    private enum class ThemeMode {
        System,
        Light,
        Dark,
    }

    private val mContext = context.applicationContext
    private val mPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(mContext)
    private val mListeners = CopyOnWriteArrayList<Listener>()
    private val mLightTheme = Theme(mContext, R.string.theme_light, R.style.OriginalTheme)
    private val mDarkTheme = Theme(mContext, R.string.theme_dark, R.style.DarkTheme)
    private lateinit var mTheme: Theme

    init {
        migrateLegacyThemePreference()
        mPreferences.registerOnSharedPreferenceChangeListener(this)
        updateTheme()
    }

    fun getTheme(): Theme = mTheme

    fun addListener(listener: Listener) {
        mListeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        mListeners.remove(listener)
    }

    fun refreshThemeFromSystem() {
        if (resolveThemeMode() == ThemeMode.System) {
            updateTheme()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (Preferences.THEME_MODE == key || Preferences.THEME_INDEX == key) {
            updateTheme()
        }
    }

    private fun updateTheme() {
        val theme = when (resolveThemeMode()) {
            ThemeMode.System -> resolveSystemTheme()
            ThemeMode.Light -> mLightTheme
            ThemeMode.Dark -> mDarkTheme
        }

        setTheme(theme)
    }

    private fun resolveSystemTheme(): Theme {
        val uiMode = mContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        return if (uiMode == Configuration.UI_MODE_NIGHT_YES) {
            mDarkTheme
        } else {
            mLightTheme
        }
    }

    private fun resolveThemeMode(): ThemeMode {
        val themeModeValue = mPreferences.getString(Preferences.THEME_MODE, null)

        if (themeModeValue != null) {
            return parseThemeMode(themeModeValue)
        }

        return resolveLegacyThemeMode()
    }

    private fun parseThemeMode(themeModeValue: String): ThemeMode {
        return when (themeModeValue) {
            Preferences.THEME_MODE_LIGHT -> ThemeMode.Light
            Preferences.THEME_MODE_DARK -> ThemeMode.Dark
            Preferences.THEME_MODE_SYSTEM -> ThemeMode.System
            else -> ThemeMode.System
        }
    }

    private fun resolveLegacyThemeMode(): ThemeMode {
        val legacyThemeIndex = mPreferences.getString(Preferences.THEME_INDEX, "0") ?: "0"

        return if (legacyThemeIndex == "1") {
            ThemeMode.Dark
        } else {
            ThemeMode.Light
        }
    }

    private fun migrateLegacyThemePreference() {
        if (mPreferences.contains(Preferences.THEME_MODE)) {
            return
        }

        if (!mPreferences.contains(Preferences.THEME_INDEX)) {
            return
        }

        val migratedThemeMode = when (resolveLegacyThemeMode()) {
            ThemeMode.Dark -> Preferences.THEME_MODE_DARK
            else -> Preferences.THEME_MODE_LIGHT
        }

        mPreferences.edit().putString(Preferences.THEME_MODE, migratedThemeMode).apply()
    }

    private fun setTheme(theme: Theme) {
        if (!::mTheme.isInitialized || mTheme !== theme) {
            mTheme = theme
            mRenderer.setBackgroundColor(mTheme.getColor(R.attr.backgroundColor))

            for (listener in mListeners) {
                listener.themeChanged(theme)
            }
        }
    }
}
