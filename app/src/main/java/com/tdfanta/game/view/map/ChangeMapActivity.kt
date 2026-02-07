package com.tdfanta.game.view.map

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import com.tdfanta.game.TDFantaApplication
import com.tdfanta.game.GameFactory
import com.tdfanta.game.R
import com.tdfanta.game.business.game.GameLoader
import com.tdfanta.game.business.game.HighScores
import com.tdfanta.game.business.game.MapRepository
import com.tdfanta.game.engine.theme.ActivityType
import com.tdfanta.game.view.BaseGameActivity
import com.tdfanta.game.view.ApplySafeInsetsHandler

class ChangeMapActivity : BaseGameActivity(), AdapterView.OnItemClickListener {
    private val mGameLoader: GameLoader
    private val mMapRepository: MapRepository
    private val mHighScores: HighScores

    init {
        val factory: GameFactory = TDFantaApplication.getInstance().getGameFactory()
        mGameLoader = factory.getGameLoader()
        mMapRepository = factory.getMapRepository()
        mHighScores = factory.getHighScores()
    }

    override fun getActivityType(): ActivityType = ActivityType.Normal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_map)

        val adapter = MapsAdapter(this, mMapRepository, mHighScores)

        val gridMaps: GridView = findViewById(R.id.grid_maps)
        gridMaps.onItemClickListener = this
        gridMaps.adapter = adapter

        val additionalPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            20f,
            resources.displayMetrics,
        ).toInt()

        gridMaps.setOnApplyWindowInsetsListener(ApplySafeInsetsHandler(additionalPadding))
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        mGameLoader.loadMap(mMapRepository.getMapInfos()[position].getMapId())
        finish()
    }
}
