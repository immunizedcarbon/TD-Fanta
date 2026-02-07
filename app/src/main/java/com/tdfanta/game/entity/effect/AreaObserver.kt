package com.tdfanta.game.entity.effect

import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.loop.TickTimer
import com.tdfanta.game.entity.EntityTypes
import com.tdfanta.game.entity.enemy.Enemy
import com.tdfanta.game.util.math.Vector2
import java.util.HashSet

class AreaObserver(
    private val mGameEngine: GameEngine,
    private val mPosition: Vector2,
    private val mRange: Float,
    private val mListener: Listener,
) : Entity.Listener {
    interface Listener {
        fun enemyEntered(enemy: Enemy)

        fun enemyExited(enemy: Enemy)
    }

    private var mFinished = false

    private val mUpdateTimer = TickTimer.createInterval(0.1f)
    private val mEnemiesInArea = HashSet<Enemy>()

    fun tick() {
        if (mFinished) {
            return
        }

        if (!mUpdateTimer.tick()) {
            return
        }

        checkForExitedEnemies()
        checkForEnteredEnemies()
    }

    fun clean() {
        for (enemy in mEnemiesInArea) {
            enemy.removeListener(this)
            mListener.enemyExited(enemy)
        }

        mEnemiesInArea.clear()
        mFinished = true
    }

    private fun checkForExitedEnemies() {
        val it = mEnemiesInArea.iterator()
        while (it.hasNext()) {
            val enemy = it.next()
            if (enemy.getDistanceTo(mPosition) > mRange) {
                it.remove()
                enemy.removeListener(this)
                mListener.enemyExited(enemy)
            }
        }
    }

    private fun checkForEnteredEnemies() {
        val enemies = mGameEngine.getEntitiesByType(EntityTypes.ENEMY)
            .filter(Entity.inRange(mPosition, mRange))
            .cast(Enemy::class.java)

        while (enemies.hasNext()) {
            val enemy = enemies.next()

            if (!mEnemiesInArea.contains(enemy)) {
                mEnemiesInArea.add(enemy)
                enemy.addListener(this)
                mListener.enemyEntered(enemy)
            }
        }
    }

    override fun entityRemoved(entity: Entity) {
        val enemy = entity as Enemy
        mEnemiesInArea.remove(enemy)
        enemy.removeListener(this)
        mListener.enemyExited(enemy)
    }
}
