package com.tdfanta.game.view.game

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.tdfanta.game.TDFantaApplication
import com.tdfanta.game.GameFactory
import com.tdfanta.game.R
import com.tdfanta.game.business.game.GameLoader
import com.tdfanta.game.business.game.GameSaver
import com.tdfanta.game.business.tower.TowerSelector
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.theme.ActivityType
import com.tdfanta.game.view.BaseGameActivity
import com.tdfanta.game.view.ApplySafeInsetsHandler

class GameActivity : BaseGameActivity() {
    private val mGameLoader: GameLoader
    private val mGameSaver: GameSaver
    private val mGameEngine: GameEngine
    private val mTowerSelector: TowerSelector
    private val mBackButtonControl: BackButtonControl

    private var mBackButtonToast: Toast? = null

    private lateinit var viewTowerDefense: GameView

    init {
        val factory: GameFactory = TDFantaApplication.getInstance().getGameFactory()
        mGameLoader = factory.getGameLoader()
        mGameSaver = factory.getGameSaver()
        mGameEngine = factory.getGameEngine()
        mTowerSelector = factory.getTowerSelector()
        mBackButtonControl = BackButtonControl(TDFantaApplication.getInstance())
    }

    override fun getActivityType(): ActivityType = ActivityType.Game

    override fun onCreate(savedInstanceState: Bundle?) {
        mGameLoader.autoLoadGame()

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)

        findViewById<android.view.View>(android.R.id.content).setOnApplyWindowInsetsListener(ApplySafeInsetsHandler())

        val config = resources.configuration

        if ((config.screenLayout and android.content.res.Configuration.SCREENLAYOUT_LONG_MASK) ==
            android.content.res.Configuration.SCREENLAYOUT_LONG_YES
        ) {
            window.insetsController?.show(WindowInsets.Type.statusBars())
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewTowerDefense = findViewById(R.id.view_tower_defense)
    }

    override fun onResume() {
        super.onResume()
        mGameEngine.start()
    }

    override fun onPause() {
        super.onPause()
        mGameSaver.autoSaveGame()
        mGameEngine.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewTowerDefense.close()

        mBackButtonToast?.cancel()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mTowerSelector.isTowerSelected()) {
                mTowerSelector.selectTower(null)
                return true
            }
            when (mBackButtonControl.backButtonPressed()) {
                BackButtonControl.BackButtonAction.DO_NOTHING -> return true
                BackButtonControl.BackButtonAction.SHOW_TOAST -> {
                    mBackButtonToast = showBackButtonToast()
                    return true
                }

                else -> return super.onKeyDown(keyCode, event)
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun showBackButtonToast(): Toast {
        val message = getString(R.string.press_back_button_again_toast)
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.show()
        return toast
    }
}
