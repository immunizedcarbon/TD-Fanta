package com.tdfanta.game

import android.app.Application

class TDFantaApplication : Application() {
    private lateinit var mGameFactory: GameFactory

    override fun onCreate() {
        super.onCreate()
        sInstance = this
        mGameFactory = GameFactory(applicationContext)
    }

    fun getGameFactory(): GameFactory = mGameFactory

    companion object {
        private var sInstance: TDFantaApplication? = null

        @JvmStatic
        fun getInstance(): TDFantaApplication = checkNotNull(sInstance)
    }
}
