package com.tdfanta.game.business.game

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.tdfanta.game.Preferences
import com.tdfanta.game.R
import com.tdfanta.game.business.tower.TowerInserter
import com.tdfanta.game.business.tower.TowerSelector
import com.tdfanta.game.business.wave.WaveManager

class TutorialControl(
    context: Context,
    towerInserter: TowerInserter,
    towerSelector: TowerSelector,
    waveManager: WaveManager,
) : TowerInserter.Listener, WaveManager.Listener, TowerSelector.Listener {
    interface TutorialView {
        fun showHint(textId: Int, showSkipButton: Boolean)

        fun tutorialFinished()
    }

    private enum class State {
        BuildTower,
        Credits,
        TowerOptions1,
        TowerOptions2,
        TowerOptions3,
        TowerOptions4,
        TowerOptions5,
        Enemies,
        Finish,
        Idle,
        ;

        fun next(): State {
            val vals = values()
            return vals[(ordinal + 1) % vals.size]
        }
    }

    private val mPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private var mView: TutorialView? = null
    private lateinit var mState: State

    init {
        towerInserter.addListener(this)
        waveManager.addListener(this)
        towerSelector.addListener(this)
    }

    fun restart() {
        mPreferences.edit()
            .putBoolean(Preferences.TUTORIAL_ENABLED, true)
            .apply()

        initialize()
    }

    fun setView(view: TutorialView?) {
        mView = view

        if (mView != null) {
            initialize()
        }
    }

    fun gotItClicked() {
        mState = mState.next()
        activate()

        if (mState == State.Idle) {
            mPreferences.edit()
                .putBoolean(Preferences.TUTORIAL_ENABLED, false)
                .apply()
        }
    }

    fun skipClicked() {
        mState = State.Finish
        activate()
    }

    override fun towerInserted() {
        if (mState == State.BuildTower) {
            mState = mState.next()
            activate()
        }
    }

    override fun towerInfoShown() {
        if (mState == State.TowerOptions1) {
            mState = mState.next()
            activate()
        }
    }

    override fun waveStarted() {
        if (mState == State.TowerOptions5) {
            mState = mState.next()
            activate()
        }
    }

    override fun waveNumberChanged() {
    }

    override fun nextWaveReadyChanged() {
    }

    override fun remainingEnemiesCountChanged() {
    }

    private fun initialize() {
        mState = if (mPreferences.getBoolean(Preferences.TUTORIAL_ENABLED, true)) {
            State.BuildTower
        } else {
            State.Idle
        }

        activate()
    }

    private fun activate() {
        val view = mView ?: return

        when (mState) {
            State.BuildTower -> view.showHint(R.string.tutorial_build_tower, true)
            State.Credits -> view.showHint(R.string.tutorial_credits, false)
            State.TowerOptions1 -> view.showHint(R.string.tutorial_tower_options_1, false)
            State.TowerOptions2 -> view.showHint(R.string.tutorial_tower_options_2, false)
            State.TowerOptions3 -> view.showHint(R.string.tutorial_tower_options_3, false)
            State.TowerOptions4 -> view.showHint(R.string.tutorial_tower_options_4, false)
            State.TowerOptions5 -> view.showHint(R.string.tutorial_next_wave, false)
            State.Enemies -> view.showHint(R.string.tutorial_enemies, false)
            State.Finish -> view.showHint(R.string.tutorial_finish, false)
            State.Idle -> view.tutorialFinished()
        }
    }
}
