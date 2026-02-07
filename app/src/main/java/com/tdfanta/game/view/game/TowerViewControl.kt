package com.tdfanta.game.view.game

import android.content.ClipData
import android.graphics.Canvas
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import com.tdfanta.game.TDFantaApplication
import com.tdfanta.game.GameFactory
import com.tdfanta.game.GameSettings
import com.tdfanta.game.business.game.GameLoader
import com.tdfanta.game.business.game.ScoreBoard
import com.tdfanta.game.business.tower.TowerInserter
import com.tdfanta.game.engine.logic.entity.EntityRegistry
import com.tdfanta.game.entity.tower.Tower

internal class TowerViewControl(private val mTowerViews: List<TowerView>) :
    GameLoader.Listener,
    ScoreBoard.Listener,
    View.OnTouchListener {
    private val mScoreBoard: ScoreBoard
    private val mGameLoader: GameLoader
    private val mTowerInserter: TowerInserter
    private val mEntityRegistry: EntityRegistry

    private val mHandler: Handler = Handler(Looper.getMainLooper())

    init {
        val factory: GameFactory = TDFantaApplication.getInstance().getGameFactory()
        mScoreBoard = factory.getScoreBoard()
        mGameLoader = factory.getGameLoader()
        mTowerInserter = factory.getTowerInserter()
        mEntityRegistry = factory.getEntityRegistry()

        mGameLoader.addListener(this)
        mScoreBoard.addListener(this)

        for (towerView in mTowerViews) {
            towerView.setOnTouchListener(this)
        }

        // Keep tower previews initialized even if gameLoaded() happened before this controller was created.
        updateTowerSlots()
    }

    fun close() {
        mGameLoader.removeListener(this)
        mScoreBoard.removeListener(this)
        mHandler.removeCallbacksAndMessages(null)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) {
            return false
        }

        val towerView = v as TowerView
        if (mScoreBoard.getCredits() < towerView.getTowerValue()) {
            return true
        }

        mTowerInserter.insertTower(towerView.getTowerName())

        val shadowBuilder: View.DragShadowBuilder = object : View.DragShadowBuilder() {
            override fun onProvideShadowMetrics(shadowSize: Point, shadowTouchPoint: Point) {
                shadowSize.set(1, 1)
                shadowTouchPoint.set(0, 0)
            }

            override fun onDrawShadow(canvas: Canvas) {
            }
        }
        val data = ClipData.newPlainText("", "")
        val dragStarted = towerView.startDragAndDrop(data, shadowBuilder, towerView, 0)
        if (!dragStarted) {
            mTowerInserter.cancel()
        }

        return true
    }

    override fun gameLoaded() {
        mHandler.post { updateTowerSlots() }
    }

    override fun creditsChanged(credits: Int) {
        mHandler.post { updateTowerEnabled() }
    }

    override fun bonusChanged(waveBonus: Int, earlyBonus: Int) {
    }

    override fun livesChanged(lives: Int) {
    }

    private fun updateTowerSlots() {
        for (i in mTowerViews.indices) {
            val previewTower = mEntityRegistry.createEntity(GameSettings.BUILD_MENU_TOWER_NAMES[i]) as Tower
            mTowerViews[i].setPreviewTower(previewTower)
        }

        updateTowerEnabled()
    }

    private fun updateTowerEnabled() {
        for (towerView in mTowerViews) {
            towerView.isEnabled = mScoreBoard.getCredits() >= towerView.getTowerValue()
        }
    }

}
