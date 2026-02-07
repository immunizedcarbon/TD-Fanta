package com.tdfanta.game.engine.logic.entity

import com.tdfanta.game.util.container.KeyValueStore

abstract class EntityPersister {
    open fun writeEntityData(entity: Entity): KeyValueStore? {
        val entityData = KeyValueStore()

        entityData.putInt("id", entity.getEntityId())
        entityData.putString("name", entity.getEntityName())
        entityData.putVector("position", entity.getPosition())

        return entityData
    }

    open fun readEntityData(entity: Entity, entityData: KeyValueStore) {
        if (!entity.getEntityName().equals(entityData.getString("name"))) {
            throw RuntimeException("Got invalid data!")
        }

        entity.setEntityId(entityData.getInt("id"))
        entity.setPosition(entityData.getVector("position"))
    }
}
