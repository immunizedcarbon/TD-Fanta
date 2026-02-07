package com.tdfanta.game.entity.plateau

import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.entity.EntityTypes
import com.tdfanta.game.util.iterator.Predicate

abstract class Plateau protected constructor(gameEngine: GameEngine) : Entity(gameEngine) {
    private var mOccupied = false

    final override fun getEntityType(): Int = EntityTypes.PLATEAU

    fun isOccupied(): Boolean = mOccupied

    fun setOccupied(occupied: Boolean) {
        mOccupied = occupied
    }

    companion object {
        @JvmStatic
        fun unoccupied(): Predicate<Plateau> = Predicate { value -> !value.mOccupied }
    }
}
