package com.tdfanta.game.view.load

import android.app.Activity
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.tdfanta.game.R
import com.tdfanta.game.business.game.SaveGameInfo
import com.tdfanta.game.business.game.SaveGameRepository
import com.tdfanta.game.util.StringUtils
import java.lang.ref.WeakReference
import java.text.DateFormat
import java.util.Locale

class SaveGamesAdapter(activity: Activity, saveGameRepository: SaveGameRepository) : BaseAdapter() {
    private val mActivityRef = WeakReference(activity)
    private val mSaveGameInfos: List<SaveGameInfo> = saveGameRepository.getSaveGameInfos()

    private class ViewHolder(view: View) {
        val imgThumb: ImageView = view.findViewById(R.id.img_thumb)
        val txtDatetime: TextView = view.findViewById(R.id.txt_datetime)
        val txtScore: TextView = view.findViewById(R.id.txt_score)
        val txtWaveNumber: TextView = view.findViewById(R.id.txt_waveNumber)
        val txtLives: TextView = view.findViewById(R.id.txt_lives)
    }

    override fun getCount(): Int = mSaveGameInfos.size

    override fun getItem(position: Int): SaveGameInfo = mSaveGameInfos[position]

    override fun getItemId(position: Int): Long = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val activity = mActivityRef.get()

        if (activity == null) {
            return convertView ?: View(parent.context)
        }

        val sgItemView = convertView ?: LayoutInflater.from(activity).inflate(R.layout.item_savegame, parent, false)

        val resources: Resources = activity.resources
        val saveGameInfo = mSaveGameInfos[position]
        val viewHolder = ViewHolder(sgItemView)

        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
        viewHolder.txtDatetime.text = dateFormat.format(saveGameInfo.getDate())
        var tmp = resources.getString(R.string.score) + ": " + StringUtils.formatSuffix(saveGameInfo.getScore())
        viewHolder.txtScore.text = tmp
        tmp = resources.getString(R.string.wave) + ": " + StringUtils.formatSuffix(saveGameInfo.getWave())
        viewHolder.txtWaveNumber.text = tmp
        tmp = resources.getString(R.string.lives) + ": " + StringUtils.formatSuffix(saveGameInfo.getLives())
        viewHolder.txtLives.text = tmp

        viewHolder.imgThumb.setImageBitmap(saveGameInfo.getScreenshot())

        return sgItemView
    }
}
