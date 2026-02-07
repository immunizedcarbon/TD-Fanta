package com.tdfanta.game

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.tdfanta.game.view.game.GameActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class IntegrationTest {
    @get:Rule
    val activityRule: ActivityScenarioRule<GameActivity> = ActivityScenarioRule(GameActivity::class.java)

    @Test
    fun integrationTest() {
        val simulator = DefaultGameSimulator(TDFantaApplication.getInstance().getGameFactory())
        simulator.startSimulation()
        simulator.waitForFinished()
    }
}
