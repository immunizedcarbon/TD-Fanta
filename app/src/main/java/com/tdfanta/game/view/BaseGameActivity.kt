package com.tdfanta.game.view

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.tdfanta.game.TDFantaApplication
import com.tdfanta.game.GameFactory
import com.tdfanta.game.engine.theme.ActivityType
import com.tdfanta.game.engine.theme.Theme
import com.tdfanta.game.engine.theme.ThemeManager

abstract class BaseGameActivity : FragmentActivity(), ThemeManager.Listener {
    private val mThemeManager: ThemeManager by lazy { getGameFactory().getThemeManager() }
    private var mPendingRecreate = false

    protected abstract fun getActivityType(): ActivityType

    protected fun getGameFactory(): GameFactory = TDFantaApplication.getInstance().getGameFactory()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(mThemeManager.getTheme().getActivityThemeId(getActivityType()))
        super.onCreate(savedInstanceState)
        mThemeManager.addListener(this)
    }

    override fun onResume() {
        super.onResume()

        if (mPendingRecreate) {
            mPendingRecreate = false
            recreate()
            return
        }

        mThemeManager.refreshThemeFromSystem()
    }

    override fun onDestroy() {
        super.onDestroy()
        mThemeManager.removeListener(this)
    }

    override fun themeChanged(theme: Theme) {
        if (isFinishing || isDestroyed) {
            return
        }

        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            recreate()
        } else {
            mPendingRecreate = true
        }
    }
}
