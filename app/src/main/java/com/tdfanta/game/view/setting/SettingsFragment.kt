package com.tdfanta.game.view.setting

import android.app.AlertDialog
import android.content.SharedPreferences
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.tdfanta.game.TDFantaApplication
import com.tdfanta.game.GameFactory
import com.tdfanta.game.Preferences
import com.tdfanta.game.R
import com.tdfanta.game.business.game.GameLoader
import com.tdfanta.game.business.game.HighScores
import com.tdfanta.game.business.game.TutorialControl

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val mGameLoader: GameLoader
    private val mHighScores: HighScores
    private val mTutorialControl: TutorialControl
    init {
        val factory: GameFactory = TDFantaApplication.getInstance().getGameFactory()
        mGameLoader = factory.getGameLoader()
        mHighScores = factory.getHighScores()
        mTutorialControl = factory.getTutorialControl()
    }

    override fun onCreatePreferences(savedInstanceState: android.os.Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        val sharedPreferences = preferenceManager.sharedPreferences
        migrateLegacyBackButtonPreference(sharedPreferences)
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        setupThemePreference()
        setupResetHighscores()
        setupResetTutorial()
    }

    override fun onDestroy() {
        super.onDestroy()

        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != null && Preferences.THEME_MODE == key) {
            updateThemePreferenceSummary()
        }
    }

    private fun setupThemePreference() {
        val preference = findPreference<Preference>(Preferences.THEME_MODE) ?: return
        updateThemePreferenceSummary()

        preference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            showThemeDialog()
            true
        }
    }

    private fun updateThemePreferenceSummary() {
        val preference = findPreference<Preference>(Preferences.THEME_MODE) ?: return
        val sharedPreferences = preferenceManager.sharedPreferences ?: return
        val currentValue = getCurrentThemeModeValue(sharedPreferences)

        val values = resources.getStringArray(R.array.theme_mode_entry_values)
        val entries = resources.getStringArray(R.array.theme_mode_entries)
        val index = values.indexOf(currentValue)
        preference.summary = if (index >= 0) entries[index] else entries.firstOrNull() ?: ""
    }

    private fun showThemeDialog() {
        val context = context ?: return
        val sharedPreferences = preferenceManager.sharedPreferences ?: return

        val values = resources.getStringArray(R.array.theme_mode_entry_values)
        val entries = resources.getStringArray(R.array.theme_mode_entries)
        val currentValue = getCurrentThemeModeValue(sharedPreferences)
        val selectedIndex = values.indexOf(currentValue).let { if (it >= 0) it else 0 }

        AlertDialog.Builder(context)
            .setTitle(R.string.theme)
            .setSingleChoiceItems(entries, selectedIndex) { dialog, which ->
                dialog.dismiss()
                val newValue = values.getOrNull(which) ?: return@setSingleChoiceItems
                applyThemeModeSelection(newValue)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setIcon(R.drawable.settings)
            .show()
    }

    private fun applyThemeModeSelection(newValue: String) {
        val sharedPreferences = preferenceManager.sharedPreferences ?: return
        val currentValue = getCurrentThemeModeValue(sharedPreferences)

        if (newValue == currentValue) {
            return
        }

        persistThemeMode(newValue, sharedPreferences)
    }

    private fun persistThemeMode(newValue: String, sharedPreferences: SharedPreferences) {
        sharedPreferences.edit()
            .putString(Preferences.THEME_MODE, newValue)
            .apply()
    }

    private fun getCurrentThemeModeValue(sharedPreferences: SharedPreferences): String {
        val mode = sharedPreferences.getString(Preferences.THEME_MODE, null)
        if (mode != null) {
            return mode
        }

        val legacyIndex = sharedPreferences.getString(Preferences.THEME_INDEX, "0") ?: "0"
        return if (legacyIndex == "1") {
            Preferences.THEME_MODE_DARK
        } else {
            Preferences.THEME_MODE_LIGHT
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

    private fun migrateLegacyBackButtonPreference(sharedPreferences: SharedPreferences?) {
        if (sharedPreferences == null || sharedPreferences.contains(Preferences.BACK_BUTTON_CLOSE_ENABLED)) {
            return
        }

        val legacyMode = sharedPreferences.getString(Preferences.BACK_BUTTON_MODE, null) ?: return
        val enabled = legacyMode == "ENABLED" || legacyMode == "TWICE"

        sharedPreferences.edit()
            .putBoolean(Preferences.BACK_BUTTON_CLOSE_ENABLED, enabled)
            .apply()
    }

    companion object {
        private const val PREF_RESET_HIGHSCORES = "reset_highscores"
        private const val PREF_START_TUTORIAL = "start_tutorial"
    }
}
