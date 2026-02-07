package com.tdfanta.game.engine.theme

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

    private val mPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    private val mAvailableThemes = ArrayList<Theme>()
    private val mListeners = CopyOnWriteArrayList<Listener>()
    private lateinit var mTheme: Theme

    init {
        mPreferences.registerOnSharedPreferenceChangeListener(this)
        initThemes(context)
        updateTheme()
    }

    private fun initThemes(context: Context) {
        mAvailableThemes.add(Theme(context, R.string.theme_original, R.style.OriginalTheme))
        mAvailableThemes.add(Theme(context, R.string.theme_dark, R.style.DarkTheme))
        mAvailableThemes.add(Theme(context, R.string.theme_colour, R.style.ColourTheme))
    }

    fun getTheme(): Theme = mTheme

    fun addListener(listener: Listener) {
        mListeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        mListeners.remove(listener)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (Preferences.THEME_INDEX == key) {
            updateTheme()
        }
    }

    private fun updateTheme() {
        val themeIndex = Integer.parseInt(mPreferences.getString(Preferences.THEME_INDEX, "0") ?: "0")
        val theme = mAvailableThemes[themeIndex]
        setTheme(theme)
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
