package com.tdfanta.game.view.load

import android.os.Bundle
import android.util.TypedValue
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import com.tdfanta.game.TDFantaApplication
import com.tdfanta.game.GameFactory
import com.tdfanta.game.R
import com.tdfanta.game.business.game.GameLoader
import com.tdfanta.game.business.game.SaveGameInfo
import com.tdfanta.game.business.game.SaveGameRepository
import com.tdfanta.game.engine.theme.ActivityType
import com.tdfanta.game.view.BaseGameActivity
import com.tdfanta.game.view.ApplySafeInsetsHandler

class LoadGameActivity : BaseGameActivity(), AdapterView.OnItemClickListener {
    private val mGameLoader: GameLoader
    private val mSaveGameRepository: SaveGameRepository

    private lateinit var mAdapter: SaveGamesAdapter

    init {
        val factory: GameFactory = TDFantaApplication.getInstance().getGameFactory()
        mGameLoader = factory.getGameLoader()
        mSaveGameRepository = factory.getSaveGameRepository()
    }

    override fun getActivityType(): ActivityType = ActivityType.Normal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_load_menu)

        mAdapter = SaveGamesAdapter(this, mSaveGameRepository)

        val gridSavegames: GridView = findViewById(R.id.grid_savegames)
        gridSavegames.onItemClickListener = this
        gridSavegames.adapter = mAdapter
        registerForContextMenu(gridSavegames)

        val additionalPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            20f,
            resources.displayMetrics,
        ).toInt()

        gridSavegames.setOnApplyWindowInsetsListener(ApplySafeInsetsHandler(additionalPadding))
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val saveGameInfo: SaveGameInfo = mAdapter.getItem(position)
        mGameLoader.loadGame(mSaveGameRepository.getGameStateFile(saveGameInfo))

        finish()
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu?.add(0, CONTEXT_MENU_DELETE_ID, 0, R.string.delete)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.itemId == CONTEXT_MENU_DELETE_ID) {
            val info = item.menuInfo as? AdapterView.AdapterContextMenuInfo ?: return false
            val saveGameInfo = mSaveGameRepository.getSaveGameInfos()[info.position]
            mSaveGameRepository.deleteSaveGame(saveGameInfo)
            mAdapter.notifyDataSetChanged()
            return true
        }

        return false
    }

    companion object {
        const val CONTEXT_MENU_DELETE_ID = 0
    }
}
