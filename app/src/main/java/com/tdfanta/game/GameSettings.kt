package com.tdfanta.game

import com.tdfanta.game.entity.tower.Canon
import com.tdfanta.game.entity.tower.GlueTower
import com.tdfanta.game.entity.tower.Mortar
import com.tdfanta.game.entity.tower.SimpleLaser

object GameSettings {
    const val START_CREDITS = 500
    const val START_LIVES = 20
    const val DIFFICULTY_MODIFIER = 8e-4f
    const val DIFFICULTY_EXPONENT = 1.9f
    const val DIFFICULTY_LINEAR = 20f
    const val MIN_HEALTH_MODIFIER = 0.5f
    const val REWARD_MODIFIER = 0.4f
    const val REWARD_EXPONENT = 0.5f
    const val MIN_REWARD_MODIFIER = 1f
    const val EARLY_BONUS_MODIFIER = 3f
    const val EARLY_BONUS_EXPONENT = 0.6f
    const val TOWER_AGE_MODIFIER = 0.97f
    const val WEAK_AGAINST_DAMAGE_MODIFIER = 3.0f
    const val STRONG_AGAINST_DAMAGE_MODIFIER = 0.33f
    const val MIN_SPEED_MODIFIER = 0.05f

    @JvmField
    val BUILD_MENU_TOWER_NAMES = arrayOf(
        Canon.ENTITY_NAME,
        SimpleLaser.ENTITY_NAME,
        Mortar.ENTITY_NAME,
        GlueTower.ENTITY_NAME,
    )
}
