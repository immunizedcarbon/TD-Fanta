package com.tdfanta.game.engine.logic.entity

import android.util.SparseArray
import com.tdfanta.game.util.container.SafeMultiMap
import com.tdfanta.game.util.iterator.StreamIterator

class EntityStore {
    private val mEntities = SafeMultiMap<Entity>()
    private val mEntityIdMap = SparseArray<Entity>()
    private val mStaticData = HashMap<Class<out Entity>, Any?>()

    fun getStaticData(entity: Entity): Any? {
        if (!mStaticData.containsKey(entity.javaClass)) {
            mStaticData[entity.javaClass] = entity.initStatic()
        }

        return mStaticData[entity.javaClass]
    }

    fun getAll(): StreamIterator<Entity> = mEntities.iterator()

    fun getByType(typeId: Int): StreamIterator<Entity> = mEntities.get(typeId).iterator()

    fun getById(entityId: Int): Entity? = mEntityIdMap[entityId]

    fun add(entity: Entity) {
        mEntities.add(entity.getEntityType(), entity)
        if (entity.getEntityId() > 0) {
            mEntityIdMap.put(entity.getEntityId(), entity)
        }
        entity.init()
    }

    fun remove(entity: Entity) {
        mEntities.remove(entity.getEntityType(), entity)
        mEntityIdMap.remove(entity.getEntityId())
        entity.clean()
    }

    fun tick() {
        for (entity in mEntities) {
            entity.tick()
        }
    }

    fun clear() {
        for (entity in mEntities) {
            mEntities.remove(entity.getEntityType(), entity)
            entity.clean()
        }

        mStaticData.clear()
    }
}
