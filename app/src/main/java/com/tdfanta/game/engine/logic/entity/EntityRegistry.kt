package com.tdfanta.game.engine.logic.entity

import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.persistence.Persister
import com.tdfanta.game.util.container.KeyValueStore

class EntityRegistry(private val mGameEngine: GameEngine) : Persister {
    private data class Registration(
        val mType: Int,
        val mName: String,
        val mFactory: EntityFactory,
        val mPersister: EntityPersister?,
    )

    private val mRegistrations = HashMap<String, Registration>()
    private var mNextEntityId = 0

    fun registerEntity(factory: EntityFactory, persister: EntityPersister?) {
        val entity = factory.create(mGameEngine)

        mRegistrations[entity.getEntityName()] = Registration(
            entity.getEntityType(),
            entity.getEntityName(),
            factory,
            persister,
        )
    }

    fun createEntity(name: String): Entity {
        val registration = mRegistrations[name]
        assert(registration != null)
        val entity = registration!!.mFactory.create(mGameEngine)
        entity.setEntityId(mNextEntityId++)
        return entity
    }

    fun getEntityNamesByType(type: Int): Set<String> {
        val result = HashSet<String>()

        for (registration in mRegistrations.values) {
            if (registration.mType == type) {
                result.add(registration.mName)
            }
        }

        return result
    }

    override fun resetState() {
        mNextEntityId = 1
    }

    override fun readState(gameState: KeyValueStore) {
        mNextEntityId = gameState.getInt("nextEntityId")

        for (data in gameState.getStoreList("entities")) {
            val registration = mRegistrations[data.getString("name")]
            assert(registration != null)

            val entity = registration!!.mFactory.create(mGameEngine)
            registration.mPersister!!.readEntityData(entity, data)
            mGameEngine.add(entity)
        }
    }

    override fun writeState(gameState: KeyValueStore) {
        gameState.putInt("nextEntityId", mNextEntityId)

        val iterator = mGameEngine.getAllEntities()
        while (iterator.hasNext()) {
            val entity = iterator.next()
            val registration = mRegistrations[entity.getEntityName()] ?: continue

            val persister = registration.mPersister ?: continue
            gameState.appendStore("entities", persister.writeEntityData(entity)!!)
        }
    }
}
