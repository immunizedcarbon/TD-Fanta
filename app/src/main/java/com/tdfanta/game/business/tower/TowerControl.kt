package com.tdfanta.game.business.tower

import com.tdfanta.game.business.game.ScoreBoard
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.EntityRegistry
import com.tdfanta.game.entity.plateau.Plateau
import com.tdfanta.game.entity.tower.Aimer
import com.tdfanta.game.entity.tower.Tower
import com.tdfanta.game.entity.tower.TowerStrategy

class TowerControl(
    private val mGameEngine: GameEngine,
    private val mScoreBoard: ScoreBoard,
    private val mTowerSelector: TowerSelector,
    private val mEntityRegistry: EntityRegistry,
) {
    fun upgradeTower() {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { upgradeTower() }
            return
        }

        val selectedTower = mTowerSelector.getSelectedTower()
        if (selectedTower == null || !selectedTower.isUpgradeable()) {
            return
        }

        val upgradeCost = selectedTower.getUpgradeCost()
        if (upgradeCost > mScoreBoard.getCredits()) {
            return
        }

        val upgradedTower = mEntityRegistry.createEntity(selectedTower.getUpgradeName()) as Tower
        mTowerSelector.showTowerInfo(upgradedTower)
        mScoreBoard.takeCredits(upgradeCost)
        val plateau: Plateau = selectedTower.getPlateau()
        selectedTower.remove()
        upgradedTower.setPlateau(plateau)
        upgradedTower.setValue(selectedTower.getValue() + upgradeCost)
        upgradedTower.setBuilt()
        mGameEngine.add(upgradedTower)

        val upgradedTowerAimer: Aimer? = upgradedTower.getAimer()
        val selectedTowerAimer: Aimer? = selectedTower.getAimer()
        if (upgradedTowerAimer != null && selectedTowerAimer != null) {
            upgradedTowerAimer.setLockTarget(selectedTowerAimer.doesLockTarget())
            upgradedTowerAimer.setStrategy(selectedTowerAimer.getStrategy())
        }
    }

    fun enhanceTower() {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { enhanceTower() }
            return
        }

        val selectedTower = mTowerSelector.getSelectedTower()
        if (selectedTower != null && selectedTower.isEnhanceable()) {
            if (selectedTower.getEnhanceCost() <= mScoreBoard.getCredits()) {
                mScoreBoard.takeCredits(selectedTower.getEnhanceCost())
                selectedTower.enhance()
                mTowerSelector.updateTowerInfo()
            }
        }
    }

    fun cycleTowerStrategy() {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { cycleTowerStrategy() }
            return
        }

        val selectedTower = mTowerSelector.getSelectedTower() ?: return
        val selectedTowerAimer = selectedTower.getAimer() ?: return

        val values = TowerStrategy.values().toList()
        var index = values.indexOf(selectedTowerAimer.getStrategy()) + 1

        if (index >= values.size) {
            index = 0
        }

        selectedTowerAimer.setStrategy(values[index])
        mTowerSelector.updateTowerInfo()
    }

    fun toggleLockTarget() {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { toggleLockTarget() }
            return
        }

        val selectedTower = mTowerSelector.getSelectedTower() ?: return
        val selectedTowerAimer = selectedTower.getAimer() ?: return

        val lock = selectedTowerAimer.doesLockTarget()
        selectedTowerAimer.setLockTarget(!lock)
        mTowerSelector.updateTowerInfo()
    }

    fun sellTower() {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { sellTower() }
            return
        }

        val selectedTower = mTowerSelector.getSelectedTower()
        if (selectedTower != null) {
            mScoreBoard.giveCredits(selectedTower.getValue(), false)
            mGameEngine.remove(selectedTower)
        }
    }
}
