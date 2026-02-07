package com.tdfanta.game.engine.logic.persistence

import com.tdfanta.game.util.container.KeyValueStore

class GamePersister {
    private val mPersisterList = ArrayList<Persister>()

    fun registerPersister(persister: Persister) {
        mPersisterList.add(persister)
    }

    fun resetState() {
        for (persister in mPersisterList) {
            persister.resetState()
        }
    }

    fun readState(gameState: KeyValueStore) {
        for (persister in mPersisterList) {
            persister.readState(gameState)
        }
    }

    fun writeState(gameState: KeyValueStore) {
        for (persister in mPersisterList) {
            persister.writeState(gameState)
        }
    }
}
