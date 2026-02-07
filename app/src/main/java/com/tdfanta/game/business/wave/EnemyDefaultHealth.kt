package com.tdfanta.game.business.wave

import com.tdfanta.game.engine.logic.entity.EntityRegistry
import com.tdfanta.game.entity.enemy.Enemy

class EnemyDefaultHealth(private val mEntityRegistry: EntityRegistry) {
    private val mEnemyDefaultHealth = HashMap<String, Float>()

    fun getDefaultHealth(name: String): Float {
        if (!mEnemyDefaultHealth.containsKey(name)) {
            val enemy = mEntityRegistry.createEntity(name) as Enemy
            mEnemyDefaultHealth[name] = enemy.getMaxHealth()
        }

        return mEnemyDefaultHealth[name]!!
    }
}
