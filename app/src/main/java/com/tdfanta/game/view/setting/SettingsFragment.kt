package com.tdfanta.game.view.setting

import android.app.AlertDialog
import android.content.SharedPreferences
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.tdfanta.game.TDFantaApplication
import com.tdfanta.game.GameFactory
import com.tdfanta.game.Preferences
import com.tdfanta.game.R
import com.tdfanta.game.business.game.GameLoader
import com.tdfanta.game.business.game.GameState
import com.tdfanta.game.business.game.HighScores
import com.tdfanta.game.business.game.TutorialControl

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val mGameLoader: GameLoader
    private val mGameState: GameState
    private val mHighScores: HighScores
    private val mTutorialControl: TutorialControl
    private val mListPreferenceKeys = ArrayList<String>()

    init {
        val factory: GameFactory = TDFantaApplication.getInstance().getGameFactory()
        mGameLoader = factory.getGameLoader()
        mGameState = factory.getGameState()
        mHighScores = factory.getHighScores()
        mTutorialControl = factory.getTutorialControl()
    }

    override fun onCreatePreferences(savedInstanceState: android.os.Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        registerListPreference(Preferences.BACK_BUTTON_MODE)
        registerListPreference(Preferences.THEME_INDEX)
        setupChangeThemeConfirmationDialog()
        setupResetHighscores()
        setupResetTutorial()
    }

    override fun onDestroy() {
        super.onDestroy()

        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != null && mListPreferenceKeys.contains(key)) {
            updateListPreferenceSummary(key)
        }

        if (key != null && Preferences.THEME_INDEX == key) {
            mGameLoader.restart()
        }
    }

    private fun registerListPreference(key: String) {
        mListPreferenceKeys.add(key)
        updateListPreferenceSummary(key)
    }

    private fun updateListPreferenceSummary(key: String) {
        val preference = findPreference<ListPreference>(key) ?: return
        preference.summary = preference.entry
    }

    private fun setupChangeThemeConfirmationDialog() {
        val themePreference = findPreference<ListPreference>(Preferences.THEME_INDEX) ?: return
        themePreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            if (!mGameState.isGameStarted()) {
                return@OnPreferenceChangeListener true
            }

            AlertDialog.Builder(preference.context)
                .setTitle(R.string.change_theme)
                .setMessage(R.string.change_theme_warning)
                .setPositiveButton(android.R.string.ok) { _, _ -> themePreference.value = newValue.toString() }
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(R.drawable.alert)
                .show()
            false
        }
    }

    private fun setupResetHighscores() {
        val preference = findPreference<Preference>(PREF_RESET_HIGHSCORES) ?: return
        preference.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference1 ->
            AlertDialog.Builder(preference1.context)
                .setTitle(R.string.reset_highscores)
                .setMessage(R.string.reset_highscores_warning)
                .setPositiveButton(android.R.string.ok) { _, _ -> mHighScores.clearHighScores() }
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(R.drawable.alert)
                .show()
            true
        }
    }

    private fun setupResetTutorial() {
        val preference = findPreference<Preference>(PREF_START_TUTORIAL) ?: return
        preference.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference1 ->
            AlertDialog.Builder(preference1.context)
                .setTitle(R.string.start_tutorial)
                .setMessage(R.string.start_tutorial_warning)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    mTutorialControl.restart()
                    mGameLoader.restart()
                    activity?.finish()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(R.drawable.alert)
                .show()
            true
        }
    }

    companion object {
        private const val PREF_RESET_HIGHSCORES = "reset_highscores"
        private const val PREF_START_TUTORIAL = "start_tutorial"
    }
}
