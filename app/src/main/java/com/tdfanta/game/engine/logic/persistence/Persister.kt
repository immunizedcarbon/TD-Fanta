package com.tdfanta.game.engine.logic.persistence

import com.tdfanta.game.util.container.KeyValueStore

interface Persister {
    fun resetState()

    fun readState(gameState: KeyValueStore)

    fun writeState(gameState: KeyValueStore)
}
