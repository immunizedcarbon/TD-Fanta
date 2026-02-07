package com.tdfanta.game.view.game

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.tdfanta.game.TDFantaApplication
import com.tdfanta.game.GameFactory
import com.tdfanta.game.R
import com.tdfanta.game.business.game.GameLoader
import com.tdfanta.game.business.game.GameSaver
import com.tdfanta.game.business.game.GameState
import com.tdfanta.game.business.game.SaveGameRepository
import com.tdfanta.game.engine.theme.ActivityType
import com.tdfanta.game.view.BaseGameActivity
import com.tdfanta.game.view.load.LoadGameActivity
import com.tdfanta.game.view.map.ChangeMapActivity
import com.tdfanta.game.view.setting.SettingsActivity
import com.tdfanta.game.view.stats.EnemyStatsActivity

class MenuActivity : BaseGameActivity(), View.OnClickListener, View.OnTouchListener {
    private val mSaveGameRepository: SaveGameRepository
    private val mGameLoader: GameLoader
    private val mGameSaver: GameSaver
    private val mGameState: GameState

    private lateinit var activityMenu: View
    private lateinit var menuLayout: View

    private lateinit var btnRestart: Button
    private lateinit var btnChangeMap: Button
    private lateinit var btnSaveGame: Button
    private lateinit var btnLoadGame: Button
    private lateinit var btnEnemyStats: Button
    private lateinit var btnSettings: Button

    private lateinit var changeMapLauncher: ActivityResultLauncher<Intent>
    private lateinit var loadGameLauncher: ActivityResultLauncher<Intent>
    private lateinit var enemyStatsLauncher: ActivityResultLauncher<Intent>
    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>

    init {
        val factory: GameFactory = TDFantaApplication.getInstance().getGameFactory()
        mSaveGameRepository = factory.getSaveGameRepository()
        mGameLoader = factory.getGameLoader()
        mGameSaver = factory.getGameSaver()
        mGameState = factory.getGameState()
    }

    override fun getActivityType(): ActivityType = ActivityType.Popup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerActivityLaunchers()
        setContentView(R.layout.activity_menu)

        btnRestart = findViewById(R.id.btn_restart)
        btnChangeMap = findViewById(R.id.btn_change_map)
        btnSaveGame = findViewById(R.id.btn_save_game)
        btnLoadGame = findViewById(R.id.btn_load_game)
        btnEnemyStats = findViewById(R.id.btn_enemy_stats)
        btnSettings = findViewById(R.id.btn_settings)

        activityMenu = findViewById(R.id.activity_menu)
        menuLayout = findViewById(R.id.menu_layout)

        btnRestart.setOnClickListener(this)
        btnChangeMap.setOnClickListener(this)
        btnSaveGame.setOnClickListener(this)
        btnLoadGame.setOnClickListener(this)
        btnEnemyStats.setOnClickListener(this)
        btnSettings.setOnClickListener(this)
        btnSaveGame.isEnabled = mGameState.isGameStarted()
        btnLoadGame.isEnabled = !mSaveGameRepository.getSaveGameInfos().isEmpty()

        activityMenu.setOnTouchListener(this)
        menuLayout.setOnTouchListener(this)
    }

    override fun onClick(view: View) {
        if (view === btnRestart) {
            mGameLoader.restart()
            finish()
            return
        }

        if (view === btnChangeMap) {
            val intent = Intent(this, ChangeMapActivity::class.java)
            changeMapLauncher.launch(intent)
            return
        }

        if (view === btnSaveGame) {
            mGameSaver.saveGame()
            btnLoadGame.isEnabled = true
            Toast.makeText(this, getString(R.string.game_saved), Toast.LENGTH_SHORT).show()
            return
        }

        if (view === btnLoadGame) {
            val intent = Intent(this, LoadGameActivity::class.java)
            loadGameLauncher.launch(intent)
            return
        }

        if (view === btnEnemyStats) {
            val intent = Intent(this, EnemyStatsActivity::class.java)
            enemyStatsLauncher.launch(intent)
            return
        }

        if (view === btnSettings) {
            val intent = Intent(this, SettingsActivity::class.java)
            settingsLauncher.launch(intent)
            return
        }
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        if (view === menuLayout) {
            return true
        }

        if (view === activityMenu) {
            finish()
            return true
        }

        return false
    }

    private fun registerActivityLaunchers() {
        changeMapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { finish() }
        loadGameLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { finish() }
        enemyStatsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { finish() }
        settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { finish() }
    }
}
