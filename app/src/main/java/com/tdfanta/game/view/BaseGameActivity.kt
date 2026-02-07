package com.tdfanta.game.view

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.tdfanta.game.TDFantaApplication
import com.tdfanta.game.GameFactory
import com.tdfanta.game.engine.theme.ActivityType
import com.tdfanta.game.engine.theme.Theme
import com.tdfanta.game.engine.theme.ThemeManager

abstract class BaseGameActivity : FragmentActivity(), ThemeManager.Listener {
    private val mThemeManager: ThemeManager by lazy { getGameFactory().getThemeManager() }

    protected abstract fun getActivityType(): ActivityType

    protected fun getGameFactory(): GameFactory = TDFantaApplication.getInstance().getGameFactory()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(mThemeManager.getTheme().getActivityThemeId(getActivityType()))
        super.onCreate(savedInstanceState)
        mThemeManager.addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mThemeManager.removeListener(this)
    }

    override fun themeChanged(theme: Theme) {
        recreate()
    }
}
