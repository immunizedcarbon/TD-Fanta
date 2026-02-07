package com.tdfanta.game.business.tower

import com.tdfanta.game.business.game.GameState
import com.tdfanta.game.business.game.ScoreBoard
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.entity.EntityRegistry
import com.tdfanta.game.entity.EntityTypes
import com.tdfanta.game.entity.plateau.Plateau
import com.tdfanta.game.entity.tower.Tower
import com.tdfanta.game.util.math.Vector2
import java.util.concurrent.CopyOnWriteArrayList

class TowerInserter(
    private val mGameEngine: GameEngine,
    private val mGameState: GameState,
    private val mEntityRegistry: EntityRegistry,
    private val mTowerSelector: TowerSelector,
    private val mTowerAging: TowerAging,
    private val mScoreBoard: ScoreBoard,
) {
    interface Listener {
        fun towerInserted()
    }

    private val mTowerDefaultValue: TowerDefaultValue = TowerDefaultValue(mEntityRegistry)

    private var mInsertedTower: Tower? = null
    private var mCurrentPlateau: Plateau? = null
    private val mListeners = CopyOnWriteArrayList<Listener>()

    fun insertTower(towerName: String) {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { insertTower(towerName) }
            return
        }

        if (mInsertedTower == null && !mGameState.isGameOver() &&
            mScoreBoard.getCredits() >= mTowerDefaultValue.getDefaultValue(towerName)
        ) {
            showTowerLevels()
            mInsertedTower = mEntityRegistry.createEntity(towerName) as Tower
        }
    }

    fun setPosition(position: Vector2) {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { setPosition(position) }
            return
        }

        if (mInsertedTower != null) {
            val closestPlateau = mGameEngine.getEntitiesByType(EntityTypes.PLATEAU)
                .cast(Plateau::class.java)
                .filter(Plateau.unoccupied())
                .min(Entity.distanceTo(position))

            if (closestPlateau != null) {
                if (mCurrentPlateau == null) {
                    mGameEngine.add(mInsertedTower!!)
                    mTowerSelector.selectTower(mInsertedTower)
                }

                mCurrentPlateau = closestPlateau
                mInsertedTower!!.setPosition(mCurrentPlateau!!.getPosition())
            } else {
                cancel()
            }
        }
    }

    fun buyTower() {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { buyTower() }
            return
        }

        if (mInsertedTower != null && mCurrentPlateau != null) {
            mInsertedTower!!.setPlateau(mCurrentPlateau)
            mInsertedTower!!.setBuilt()

            mScoreBoard.takeCredits(mInsertedTower!!.getValue())
            mTowerAging.ageTower(mInsertedTower!!)

            mTowerSelector.selectTower(null)
            hideTowerLevels()

            mCurrentPlateau = null
            mInsertedTower = null

            for (listener in mListeners) {
                listener.towerInserted()
            }
        }
    }

    fun cancel() {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { cancel() }
            return
        }

        if (mInsertedTower != null) {
            mGameEngine.remove(mInsertedTower!!)

            hideTowerLevels()
            mCurrentPlateau = null
            mInsertedTower = null
        }
    }

    fun addListener(listener: Listener) {
        mListeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        mListeners.remove(listener)
    }

    private fun showTowerLevels() {
        val towers = mGameEngine.getEntitiesByType(EntityTypes.TOWER).cast(Tower::class.java)

        while (towers.hasNext()) {
            val tower = towers.next()
            tower.showLevel()
        }
    }

    private fun hideTowerLevels() {
        val towers = mGameEngine.getEntitiesByType(EntityTypes.TOWER).cast(Tower::class.java)

        while (towers.hasNext()) {
            val tower = towers.next()
            tower.hideLevel()
        }
    }

}
