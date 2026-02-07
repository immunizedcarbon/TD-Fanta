package com.tdfanta.game.view.setting

import android.os.Bundle
import com.tdfanta.game.engine.theme.ActivityType
import com.tdfanta.game.view.BaseGameActivity
import com.tdfanta.game.view.ApplySafeInsetsHandler

class SettingsActivity : BaseGameActivity() {
    override fun getActivityType(): ActivityType = ActivityType.Normal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commit()

        findViewById<android.view.View>(android.R.id.content).setOnApplyWindowInsetsListener(ApplySafeInsetsHandler())
    }
}
