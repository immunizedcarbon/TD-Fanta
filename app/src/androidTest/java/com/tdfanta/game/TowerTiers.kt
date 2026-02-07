package com.tdfanta.game

import com.tdfanta.game.engine.logic.entity.EntityRegistry
import com.tdfanta.game.entity.tower.Tower
import com.tdfanta.game.util.iterator.StreamIterator

class TowerTiers(private val gameFactory: GameFactory) {
    private val towerTierCache = HashMap<String, Int>()

    internal fun getBuildableTowers(): StreamIterator<Tower> {
        val entityRegistry: EntityRegistry = gameFactory.getEntityRegistry()

        return StreamIterator.fromIterable(GameSettings.BUILD_MENU_TOWER_NAMES.asList())
            .map { name -> entityRegistry.createEntity(name) as Tower }
    }

    internal fun getTowerTier(tower: Tower): Int {
        if (towerTierCache.isEmpty()) {
            initTowerTierCache()
        }

        return checkNotNull(towerTierCache[tower.getEntityName()])
    }

    private fun initTowerTierCache() {
        val entityRegistry = gameFactory.getEntityRegistry()
        val iterator = getBuildableTowers()
        towerTierCache.clear()

        while (iterator.hasNext()) {
            var tier = 1
            var tower = iterator.next()
            towerTierCache[tower.getEntityName()] = tier

            while (tower.isUpgradeable()) {
                tower = entityRegistry.createEntity(tower.getUpgradeName()) as Tower
                tier += 1
                towerTierCache[tower.getEntityName()] = tier
            }
        }
    }
}
