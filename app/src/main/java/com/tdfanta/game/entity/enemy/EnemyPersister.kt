package com.tdfanta.game.entity.enemy

import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.entity.EntityPersister
import com.tdfanta.game.util.container.KeyValueStore

open class EnemyPersister : EntityPersister() {
    override fun writeEntityData(entity: Entity): KeyValueStore {
        val data = super.writeEntityData(entity)!!

        val enemy = entity as Enemy
        data.putFloat("health", enemy.getHealth())
        data.putFloat("maxHealth", enemy.getMaxHealth())
        data.putVectorList("wayPoints", enemy.getWayPoints())
        data.putInt("wayPointIndex", enemy.getWayPointIndex())
        data.putInt("waveNumber", enemy.getWaveNumber())
        data.putInt("reward", enemy.getReward())
        data.putBoolean("teleported", enemy.wasTeleported())

        return data
    }

    override fun readEntityData(entity: Entity, entityData: KeyValueStore) {
        super.readEntityData(entity, entityData)

        val enemy = entity as Enemy
        enemy.setHealth(entityData.getFloat("health"), entityData.getFloat("maxHealth"))
        enemy.setReward(entityData.getInt("reward"))
        enemy.setWaveNumber(entityData.getInt("waveNumber"))
        enemy.setupPath(entityData.getVectorList("wayPoints"), entityData.getInt("wayPointIndex"))

        if (entityData.getBoolean("teleported")) {
            enemy.finishTeleport()
        }
    }
}
