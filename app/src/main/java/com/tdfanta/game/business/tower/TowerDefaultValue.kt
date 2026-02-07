package com.tdfanta.game.business.tower

import com.tdfanta.game.engine.logic.entity.EntityRegistry
import com.tdfanta.game.entity.tower.Tower

class TowerDefaultValue(private val mEntityRegistry: EntityRegistry) {
    private val mTowerDefaultValue = HashMap<String, Int>()

    fun getDefaultValue(name: String): Int {
        if (!mTowerDefaultValue.containsKey(name)) {
            val tower = mEntityRegistry.createEntity(name) as Tower
            mTowerDefaultValue[name] = tower.getValue()
        }

        return mTowerDefaultValue[name]!!
    }
}
