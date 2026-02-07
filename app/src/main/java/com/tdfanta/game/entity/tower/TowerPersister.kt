package com.tdfanta.game.entity.tower

import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.entity.EntityPersister
import com.tdfanta.game.entity.plateau.Plateau
import com.tdfanta.game.util.container.KeyValueStore

open class TowerPersister : EntityPersister() {
    override fun writeEntityData(entity: Entity): KeyValueStore? {
        val tower = entity as Tower

        if (!tower.isBuilt()) {
            return null
        }

        val data = super.writeEntityData(tower)!!
        data.putInt("plateauId", tower.getPlateau().getEntityId())
        data.putInt("value", tower.getValue())
        data.putInt("level", tower.getLevel())
        data.putFloat("damageInflicted", tower.getDamageInflicted())

        val aimer = tower.getAimer()
        if (aimer != null) {
            data.putString("strategy", aimer.getStrategy().toString())
            data.putBoolean("lockTarget", aimer.doesLockTarget())
        }

        return data
    }

    override fun readEntityData(entity: Entity, entityData: KeyValueStore) {
        super.readEntityData(entity, entityData)
        val tower = entity as Tower

        while (tower.getLevel() < entityData.getInt("level")) {
            tower.enhance()
        }

        tower.setPlateau(tower.getGameEngine().getEntityById(entityData.getInt("plateauId")) as Plateau)
        tower.setValue(entityData.getInt("value"))
        tower.setDamageInflicted(entityData.getFloat("damageInflicted"))
        tower.setBuilt()

        val aimer = tower.getAimer()
        if (aimer != null) {
            aimer.setStrategy(TowerStrategy.valueOf(entityData.getString("strategy")))
            aimer.setLockTarget(entityData.getBoolean("lockTarget"))
        }
    }
}
