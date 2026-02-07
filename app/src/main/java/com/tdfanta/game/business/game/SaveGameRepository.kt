package com.tdfanta.game.business.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.tdfanta.game.util.container.KeyValueStore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

class SaveGameRepository(private val mContext: Context) {
    private val mSaveGameInfos = ArrayList<SaveGameInfo>()

    init {
        readSaveGameInfos()
    }

    fun getAutoSaveStateFile(): File = File(mContext.filesDir, AUTO_SAVE_STATE_FILE)

    fun getGameStateFile(saveGameInfo: SaveGameInfo): File = File(saveGameInfo.getFolder(), GAME_STATE_FILE)

    fun getSaveGameInfos(): List<SaveGameInfo> = Collections.unmodifiableList(mSaveGameInfos)

    fun createSaveGame(screenshot: Bitmap, score: Int, wave: Int, lives: Int): SaveGameInfo {
        val date = Date()

        val dateFormat = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US)
        val folder = File(
            mContext.filesDir.toString() + File.separator +
                "savegame" + File.separator +
                dateFormat.format(date),
        )

        folder.mkdirs()

        var screenshotScaled = screenshot

        try {
            Log.i(TAG, "Saving screenshot...")
            FileOutputStream(File(folder, SCREENSHOT_FILE), false).use { outputStream ->
                val destWidth = 600
                val origWidth = screenshotScaled.width

                if (destWidth < origWidth) {
                    val origHeight = screenshotScaled.height
                    val destHeight = (origHeight.toFloat() / (origWidth.toFloat() / destWidth)).toInt()
                    screenshotScaled = Bitmap.createScaledBitmap(screenshotScaled, destWidth, destHeight, false)
                }

                screenshotScaled.compress(Bitmap.CompressFormat.PNG, 30, outputStream)
                outputStream.flush()
            }
        } catch (e: IOException) {
            throw RuntimeException("Could not save screenshot!", e)
        }

        try {
            Log.i(TAG, "Saving game info...")
            val saveGameInfo = KeyValueStore()
            saveGameInfo.putInt("version", SaveGameMigrator.SAVE_GAME_VERSION)
            saveGameInfo.putDate("date", date)
            saveGameInfo.putInt("score", score)
            saveGameInfo.putInt("wave", wave)
            saveGameInfo.putInt("lives", lives)

            FileOutputStream(File(folder, GAME_INFO_FILE), false).use { outputStream ->
                saveGameInfo.toStream(outputStream)
            }
        } catch (e: Exception) {
            throw RuntimeException("Could not save game info!", e)
        }

        val saveGameInfo = SaveGameInfo(folder, date, score, wave, lives, screenshotScaled)
        mSaveGameInfos.add(0, saveGameInfo)
        return saveGameInfo
    }

    fun deleteSaveGame(saveGameInfo: SaveGameInfo) {
        if (!mSaveGameInfos.contains(saveGameInfo)) {
            throw RuntimeException("Unknown save game!")
        }

        deleteSaveGame(saveGameInfo.getFolder())
        mSaveGameInfos.remove(saveGameInfo)
    }

    private fun readSaveGameInfos() {
        val rootdir = File(
            mContext.filesDir.toString() + File.separator +
                "savegame" + File.separator,
        )

        val fileArray = rootdir.listFiles()

        if (fileArray == null || fileArray.isEmpty()) {
            Log.i(TAG, "No save games found.")
            return
        }

        val fileList = fileArray.toMutableList()
        fileList.sortDescending()

        for (file in fileList) {
            if (!isSaveGameFolder(rootdir, file)) {
                continue
            }
            val saveGameInfo = readSaveGameInfo(file)

            if (saveGameInfo != null) {
                mSaveGameInfos.add(saveGameInfo)
            }
        }
    }

    companion object {
        private val TAG = SaveGameRepository::class.java.simpleName

        private const val AUTO_SAVE_STATE_FILE = "autosave.json"

        private const val GAME_INFO_FILE = "info.json"
        private const val GAME_STATE_FILE = "state.json"
        private const val SCREENSHOT_FILE = "screen.png"
        private val SAVE_GAME_FOLDER_REGEX = Regex("\\d{17}")

        private fun deleteSaveGame(folder: File) {
            Log.i(TAG, "Deleting save game: ${folder.name}")
            val files = listOf(GAME_STATE_FILE, GAME_INFO_FILE, SCREENSHOT_FILE)

            for (file in files) {
                if (!File(folder, file).delete()) {
                    Log.e(TAG, "Failed to delete file: $file")
                }
            }

            if (!folder.delete()) {
                Log.e(TAG, "Failed to delete save game: ${folder.name}")
            }
        }

        private fun readSaveGameInfo(folder: File): SaveGameInfo? {
            return try {
                Log.i(TAG, "Reading save game:${folder.name}")
                val gameInfoStore = FileInputStream(File(folder, GAME_INFO_FILE)).use { inputStream ->
                    KeyValueStore.fromStream(inputStream)
                }

                val date = gameInfoStore.getDate("date")
                val score = gameInfoStore.getInt("score")
                val wave = gameInfoStore.getInt("wave")
                val lives = gameInfoStore.getInt("lives")

                val screenshot = BitmapFactory.decodeFile(File(folder, SCREENSHOT_FILE).absolutePath)

                SaveGameInfo(folder, date, score, wave, lives, screenshot)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read save game!")
                null
            }
        }

        private fun isSaveGameFolder(rootDir: File, folder: File): Boolean {
            if (!folder.isDirectory || !SAVE_GAME_FOLDER_REGEX.matches(folder.name)) {
                return false
            }

            return try {
                val rootPath = rootDir.canonicalFile.toPath()
                val folderPath = folder.canonicalFile.toPath()
                folderPath.startsWith(rootPath)
            } catch (_: IOException) {
                false
            }
        }
    }
}
