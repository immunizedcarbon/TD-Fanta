package com.tdfanta.game.engine.logic.entity

import com.tdfanta.game.engine.logic.GameEngine

abstract class EntityFactory {
    abstract fun create(gameEngine: GameEngine): Entity
}
