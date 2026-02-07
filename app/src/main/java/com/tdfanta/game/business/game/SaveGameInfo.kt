package com.tdfanta.game.business.game

import android.graphics.Bitmap
import java.io.File
import java.util.Date

class SaveGameInfo(
    private val mFolder: File,
    private val mDate: Date,
    private val mScore: Int,
    private val mWave: Int,
    private val mLives: Int,
    private val mScreenshot: Bitmap,
) {
    fun getFolder(): File = mFolder

    fun getDate(): Date = mDate

    fun getScore(): Int = mScore

    fun getWave(): Int = mWave

    fun getLives(): Int = mLives

    fun getScreenshot(): Bitmap = mScreenshot
}
