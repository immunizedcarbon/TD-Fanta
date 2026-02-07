package com.tdfanta.game.view.stats

import android.os.Bundle
import android.util.TypedValue
import android.widget.GridView
import com.tdfanta.game.TDFantaApplication
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.entity.EntityRegistry
import com.tdfanta.game.engine.theme.ActivityType
import com.tdfanta.game.engine.theme.Theme
import com.tdfanta.game.engine.theme.ThemeManager
import com.tdfanta.game.view.BaseGameActivity
import com.tdfanta.game.view.ApplySafeInsetsHandler

class EnemyStatsActivity : BaseGameActivity(), ThemeManager.Listener {
    private val mTheme: Theme
    private val mEntityRegistry: EntityRegistry

    init {
        val app = TDFantaApplication.getInstance()
        mTheme = app.getGameFactory().getGameEngine().getThemeManager().getTheme()
        mEntityRegistry = app.getGameFactory().getEntityRegistry()
    }

    override fun getActivityType(): ActivityType = ActivityType.Normal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enemy_stats)

        val adapter = EnemiesAdapter(this, mTheme, mEntityRegistry)

        val gridEnemies: GridView = findViewById(R.id.grid_enemies)
        gridEnemies.adapter = adapter

        val additionalPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            20f,
            resources.displayMetrics,
        ).toInt()

        gridEnemies.setOnApplyWindowInsetsListener(ApplySafeInsetsHandler(additionalPadding))
    }
}
