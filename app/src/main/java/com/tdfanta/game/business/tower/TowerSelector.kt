package com.tdfanta.game.business.tower

import com.tdfanta.game.business.game.ScoreBoard
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.entity.EntityTypes
import com.tdfanta.game.entity.tower.Tower
import com.tdfanta.game.util.math.Vector2
import java.util.concurrent.CopyOnWriteArrayList

class TowerSelector(
    private val mGameEngine: GameEngine,
    private val mScoreBoard: ScoreBoard,
) : ScoreBoard.Listener, Entity.Listener, Tower.Listener {
    interface TowerInfoView {
        fun showTowerInfo(towerInfo: TowerInfo)

        fun hideTowerInfo()
    }

    interface TowerBuildView {
        fun toggleTowerBuildView()

        fun hideTowerBuildView()
    }

    interface Listener {
        fun towerInfoShown()
    }

    private var mTowerInfoView: TowerInfoView? = null
    private var mTowerBuildView: TowerBuildView? = null

    private var mControlsEnabled = false
    private var mTowerInfo: TowerInfo? = null
    private var mSelectedTower: Tower? = null

    private val mListeners = CopyOnWriteArrayList<Listener>()

    init {
        mScoreBoard.addListener(this)
    }

    fun setTowerInfoView(view: TowerInfoView?) {
        mTowerInfoView = view
    }

    fun setTowerBuildView(view: TowerBuildView?) {
        mTowerBuildView = view
    }

    fun addListener(listener: Listener) {
        mListeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        mListeners.remove(listener)
    }

    fun isTowerSelected(): Boolean = mSelectedTower != null

    fun getTowerInfo(): TowerInfo? = mTowerInfo

    fun toggleTowerBuildView() {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { toggleTowerBuildView() }
            return
        }

        hideTowerInfoView()
        mTowerBuildView?.toggleTowerBuildView()
    }

    fun selectTowerAt(position: Vector2) {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { selectTowerAt(position) }
            return
        }

        val closest = mGameEngine
            .getEntitiesByType(EntityTypes.TOWER)
            .min(Entity.distanceTo(position)) as Tower?

        if (closest != null && closest.getDistanceTo(position) < 0.6f) {
            selectTower(closest)
        } else {
            selectTower(null)
        }
    }

    fun selectTower(tower: Tower?) {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { selectTower(tower) }
            return
        }

        hideTowerInfoView()
        hideTowerBuildView()

        if (tower != null) {
            if (mSelectedTower == tower) {
                showTowerInfoView()
            } else {
                setSelectedTower(tower)
            }
        } else {
            setSelectedTower(null)
        }
    }

    fun setControlsEnabled(enabled: Boolean) {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { setControlsEnabled(enabled) }
            return
        }

        mControlsEnabled = enabled

        if (mTowerInfo != null) {
            updateTowerInfo()
        }
    }

    fun showTowerInfo(tower: Tower) {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { showTowerInfoView() }
            return
        }

        setSelectedTower(tower)
        showTowerInfoView()
    }

    fun updateTowerInfo() {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { updateTowerInfo() }
            return
        }

        if (mTowerInfo != null) {
            showTowerInfoView()
        }
    }

    override fun entityRemoved(entity: Entity) {
        selectTower(null)
    }

    override fun damageInflicted(totalDamage: Float) {
        updateTowerInfo()
    }

    override fun propertiesChanged() {
        updateTowerInfo()
    }

    override fun creditsChanged(credits: Int) {
        if (mTowerInfo != null) {
            updateTowerInfo()
        }
    }

    override fun bonusChanged(waveBonus: Int, earlyBonus: Int) {
    }

    override fun livesChanged(lives: Int) {
    }

    fun getSelectedTower(): Tower? = mSelectedTower

    private fun setSelectedTower(tower: Tower?) {
        if (mSelectedTower != null) {
            mSelectedTower!!.removeListener(this as Tower.Listener)
            mSelectedTower!!.removeListener(this as Entity.Listener)
            mSelectedTower!!.hideRange()
        }

        mSelectedTower = tower

        if (mSelectedTower != null) {
            mSelectedTower!!.addListener(this as Tower.Listener)
            mSelectedTower!!.addListener(this as Entity.Listener)
            mSelectedTower!!.showRange()
        }
    }

    private fun showTowerInfoView() {
        mTowerInfo = TowerInfo(
            mSelectedTower!!,
            mScoreBoard.getCredits(),
            mControlsEnabled,
        )

        if (mTowerInfoView != null) {
            mTowerInfoView!!.showTowerInfo(mTowerInfo!!)

            for (listener in mListeners) {
                listener.towerInfoShown()
            }
        }
    }

    private fun hideTowerInfoView() {
        mTowerInfo = null
        mTowerInfoView?.hideTowerInfo()
    }

    private fun hideTowerBuildView() {
        mTowerBuildView?.hideTowerBuildView()
    }
}
