package com.tdfanta.game.view.map

import android.app.Activity
import android.content.res.Resources
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.tdfanta.game.R
import com.tdfanta.game.business.game.HighScores
import com.tdfanta.game.business.game.MapInfo
import com.tdfanta.game.business.game.MapRepository
import java.lang.ref.WeakReference
import java.text.DecimalFormat

internal class MapsAdapter(
    activity: Activity,
    mapRepository: MapRepository,
    private val mHighScores: HighScores,
) : BaseAdapter() {
    private val mActivityRef = WeakReference(activity)
    private val mMapInfos: List<MapInfo> = mapRepository.getMapInfos()

    private class ViewHolder(view: View) {
        val imgThumb: ImageView = view.findViewById(R.id.img_thumb)
        val txtName: TextView = view.findViewById(R.id.txt_name)
        val txtHighscore: TextView = view.findViewById(R.id.txt_highscore)
    }

    override fun getCount(): Int = mMapInfos.size

    override fun getItem(position: Int): Any = mMapInfos[position]

    override fun getItemId(position: Int): Long = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val activity = mActivityRef.get()

        if (activity == null) {
            return convertView ?: View(parent.context)
        }

        val mapItemView = convertView ?: LayoutInflater.from(activity).inflate(R.layout.item_map, parent, false)

        val resources: Resources = activity.resources
        val mapInfo = mMapInfos[position]
        val viewHolder = ViewHolder(mapItemView)

        viewHolder.txtName.text = resources.getString(mapInfo.getMapNameResId())

        val fmt = DecimalFormat("###,###,###,###")
        val highScore = fmt.format(mHighScores.getHighScore(mapInfo.getMapId()))
        viewHolder.txtHighscore.text = resources.getString(R.string.score) + ": " + highScore

        if (!sThumbCache.containsKey(mapInfo.getMapId())) {
            val generator = MapThumbGenerator()
            val thumb = generator.generateThumb(resources, mapInfo.getMapDataResId())
            sThumbCache[mapInfo.getMapId()] = thumb
        }

        viewHolder.imgThumb.setImageBitmap(sThumbCache[mapInfo.getMapId()])

        return mapItemView
    }

    companion object {
        private val sThumbCache = HashMap<String, Bitmap>()
    }
}
