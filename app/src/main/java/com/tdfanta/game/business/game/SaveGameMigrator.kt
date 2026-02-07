package com.tdfanta.game.business.game

import android.util.Log
import com.tdfanta.game.util.container.KeyValueStore

class SaveGameMigrator {
    private fun interface Migrator {
        fun migrate(gameState: KeyValueStore): Boolean
    }

    private val mMigratorList = ArrayList<Migrator>()

    init {
        mMigratorList.add(Migrator { gameState -> migrateToVersion2(gameState) })
    }

    fun migrate(gameState: KeyValueStore): Boolean {
        var version = gameState.getInt("version")

        if (version > SAVE_GAME_VERSION) {
            Log.w(TAG, "Save game version higher than required version!")
            return false
        }

        while (version < SAVE_GAME_VERSION) {
            val result = mMigratorList[version - 1].migrate(gameState)

            if (!result) {
                Log.w(TAG, "Migration failed.")
                return false
            }

            version++
        }

        gameState.putInt("version", SAVE_GAME_VERSION)
        return true
    }

    private fun migrateToVersion2(gameState: KeyValueStore): Boolean {
        if (gameState.getInt("lives") < 0) {
            gameState.putInt("finalScore", gameState.getInt("creditsEarned"))
        } else {
            gameState.putInt("finalScore", 0)
        }

        return true
    }

    companion object {
        private val TAG = GameLoader::class.java.simpleName
        const val SAVE_GAME_VERSION = 2
    }
}
