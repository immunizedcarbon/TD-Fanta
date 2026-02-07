package com.tdfanta.game.business.tower

import com.tdfanta.game.GameSettings
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.entity.EntityTypes
import com.tdfanta.game.entity.tower.Tower

class TowerAging(private val mGameEngine: GameEngine) {
    fun ageTowers() {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { ageTowers() }
            return
        }

        val towers = mGameEngine
            .getEntitiesByType(EntityTypes.TOWER)
            .cast(Tower::class.java)

        while (towers.hasNext()) {
            val tower = towers.next()
            ageTower(tower)
        }
    }

    fun ageTower(tower: Tower) {
        if (mGameEngine.isThreadChangeNeeded()) {
            mGameEngine.post { ageTower(tower) }
            return
        }

        var value = tower.getValue()
        value = kotlin.math.round(value * GameSettings.TOWER_AGE_MODIFIER).toInt()
        tower.setValue(value)
    }
}
