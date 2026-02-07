package com.tdfanta.game

import com.tdfanta.game.business.game.ScoreBoard
import com.tdfanta.game.business.tower.TowerControl
import com.tdfanta.game.business.tower.TowerInserter
import com.tdfanta.game.business.tower.TowerSelector
import com.tdfanta.game.business.wave.WaveManager
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.loop.TickTimer
import com.tdfanta.game.engine.logic.map.MapPath
import com.tdfanta.game.entity.EntityTypes
import com.tdfanta.game.entity.plateau.Plateau
import com.tdfanta.game.entity.tower.Tower
import com.tdfanta.game.entity.tower.TowerStrategy
import com.tdfanta.game.util.iterator.Function
import com.tdfanta.game.util.iterator.StreamIterator
import com.tdfanta.game.util.math.Intersections
import com.tdfanta.game.util.math.Vector2
import java.util.Random

class DefaultGameSimulator(gameFactory: GameFactory) : GameSimulator(gameFactory) {
    private val towerTiers = TowerTiers(gameFactory)
    private val random = Random()
    private val autoSaveAndLoadTimer = TickTimer.createInterval(300f)
    private val saveAndLoadTimer = TickTimer.createInterval(120f)
    private val simulationTickTimer = TickTimer.createInterval(2f)

    private var saveAndLoadState = 0
    private var allTowersUpgraded = false

    override fun tick() {
        if (simulationTickTimer.tick()) {
            tryUpgradeTower()
            tryEnhanceTower()
            tryBuildTower()
            tryStartNextWave()
        }

        if (autoSaveAndLoadTimer.tick()) {
            autoSaveAndLoad()
        }

        if (saveAndLoadTimer.tick()) {
            when (++saveAndLoadState) {
                1 -> saveGame()
                2 -> loadGame()
                3 -> deleteSaveGame()
                10 -> saveAndLoadState = 0
            }
        }
    }

    private fun tryUpgradeTower() {
        val scoreBoard: ScoreBoard = getGameFactory().getScoreBoard()
        val towerSelector: TowerSelector = getGameFactory().getTowerSelector()
        val towerControl: TowerControl = getGameFactory().getTowerControl()

        allTowersUpgraded = true
        val iterator = getTowers()

        while (iterator.hasNext()) {
            val tower = iterator.next()

            if (!tower.isUpgradeable()) {
                continue
            }

            allTowersUpgraded = false

            if (tower.getUpgradeCost() > scoreBoard.getCredits()) {
                continue
            }

            if (tower.getLevel() > 1) {
                continue
            }

            val upgradedTowerCount = getTowers()
                .filter { t -> tower.getUpgradeName() == t.getEntityName() }
                .count()

            if (upgradedTowerCount >= TARGET_TOWER_COUNT_PER_TYPE_AND_TIER) {
                continue
            }

            val position = tower.getPosition()

            towerSelector.selectTower(tower)
            towerControl.upgradeTower()

            randomizeTowerStrategy(position)
        }
    }

    private fun tryEnhanceTower() {
        val towerSelector: TowerSelector = getGameFactory().getTowerSelector()
        val towerControl: TowerControl = getGameFactory().getTowerControl()
        val scoreBoard: ScoreBoard = getGameFactory().getScoreBoard()

        val iterator = getTowers()

        while (iterator.hasNext()) {
            val tower = iterator.next()

            if (!tower.isEnhanceable()) {
                continue
            }

            if (tower.getEnhanceCost() > scoreBoard.getCredits()) {
                continue
            }

            val enhancedTowerCount = getTowers()
                .filter { t -> tower.getEntityName() == t.getEntityName() }
                .filter { t -> t.getLevel() > 1 }
                .count()

            if (tower.isUpgradeable() && enhancedTowerCount >= TARGET_TOWER_COUNT_PER_TYPE_AND_TIER - 1) {
                continue
            }

            if (!tower.isUpgradeable() && !allTowersUpgraded) {
                continue
            }

            towerSelector.selectTower(tower)
            towerControl.enhanceTower()
            return
        }
    }

    private fun tryBuildTower() {
        val towerInserter: TowerInserter = getGameFactory().getTowerInserter()
        val scoreBoard: ScoreBoard = getGameFactory().getScoreBoard()

        val iterator = towerTiers.getBuildableTowers()

        while (iterator.hasNext()) {
            val tower = iterator.next()

            if (tower.getValue() > scoreBoard.getCredits()) {
                continue
            }

            val builtTowerCount = getTowers()
                .filter { t -> tower.getEntityName() == t.getEntityName() }
                .count()

            if (builtTowerCount >= TARGET_TOWER_COUNT_PER_TYPE_AND_TIER) {
                continue
            }

            val selectedPlateau = findTowerPlateau(tower) ?: return

            towerInserter.insertTower(tower.getEntityName())
            towerInserter.setPosition(selectedPlateau.getPosition())
            towerInserter.buyTower()

            randomizeTowerStrategy(selectedPlateau.getPosition())
        }
    }

    private fun randomizeTowerStrategy(position: Vector2) {
        val towerSelector: TowerSelector = getGameFactory().getTowerSelector()
        val towerControl: TowerControl = getGameFactory().getTowerControl()

        val toggleLock = random.nextBoolean()
        val cycleStrategyCount = random.nextInt(TowerStrategy.values().size)

        towerSelector.selectTowerAt(position)

        if (toggleLock) {
            towerControl.toggleLockTarget()
        }

        for (i in 0 until cycleStrategyCount) {
            towerControl.cycleTowerStrategy()
        }
    }

    private fun findTowerPlateau(tower: Tower): Plateau? {
        val paths: List<MapPath> = checkNotNull(getGameFactory().getGameEngine().getGameMap()).getPaths()
        val range = tower.getRange()

        val distanceCovered = Function<Plateau, Float> { plateau ->
            var covered = 0f

            for (path in paths) {
                for (line in Intersections.getPathSectionsInRange(path.getWayPoints(), plateau.getPosition(), range)) {
                    covered += line.length()
                }
            }

            covered
        }

        val maxDistanceCovered = getFreePlateaus()
            .map(distanceCovered)
            .max { input -> input }
            ?: return null

        return getFreePlateaus()
            .filter { value -> distanceCovered.apply(value) > maxDistanceCovered * 0.8f }
            .random(random)
    }

    private fun tryStartNextWave() {
        val waveManager: WaveManager = getGameFactory().getWaveManager()

        if (waveManager.isNextWaveReady() && waveManager.getRemainingEnemiesCount() < 50) {
            waveManager.startNextWave()
        }
    }

    private fun getTowers(): StreamIterator<Tower> {
        val gameEngine: GameEngine = getGameFactory().getGameEngine()

        return gameEngine.getEntitiesByType(EntityTypes.TOWER)
            .cast(Tower::class.java)
    }

    private fun getFreePlateaus(): StreamIterator<Plateau> {
        val gameEngine: GameEngine = getGameFactory().getGameEngine()

        return gameEngine.getEntitiesByType(EntityTypes.PLATEAU)
            .cast(Plateau::class.java)
            .filter(Plateau.unoccupied())
    }

    companion object {
        private const val TARGET_TOWER_COUNT_PER_TYPE_AND_TIER = 2
    }
}
