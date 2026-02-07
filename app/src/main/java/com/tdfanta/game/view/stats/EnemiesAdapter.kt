package com.tdfanta.game.view.stats

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.entity.EntityRegistry
import com.tdfanta.game.engine.render.Viewport
import com.tdfanta.game.engine.theme.Theme
import com.tdfanta.game.entity.EntityTypes
import com.tdfanta.game.entity.enemy.Enemy
import com.tdfanta.game.entity.enemy.EnemyProperties
import java.lang.ref.WeakReference
import java.text.DecimalFormat

class EnemiesAdapter(activity: Activity, private val mTheme: Theme, entityRegistry: EntityRegistry) : BaseAdapter() {
    private val mEnemies: MutableList<Enemy>

    private val mActivityRef = WeakReference(activity)

    init {
        mEnemies = ArrayList()
        for (name in entityRegistry.getEntityNamesByType(EntityTypes.ENEMY)) {
            mEnemies.add(entityRegistry.createEntity(name) as Enemy)
        }
    }

    private class ViewHolder(view: View) {
        val imgEnemy: ImageView = view.findViewById(R.id.img_enemy)
        val txtName: TextView = view.findViewById(R.id.txt_name)
        val txtHealth: TextView = view.findViewById(R.id.txt_health)
        val txtSpeed: TextView = view.findViewById(R.id.txt_speed)
        val txtReward: TextView = view.findViewById(R.id.txt_reward)
        val txtWeakAgainst: TextView = view.findViewById(R.id.txt_weak_against)
        val txtStrongAgainst: TextView = view.findViewById(R.id.txt_strong_against)
    }

    override fun getCount(): Int = mEnemies.size

    override fun getItem(position: Int): Any = mEnemies[position]

    override fun getItemId(position: Int): Long = 0

    override fun areAllItemsEnabled(): Boolean = false

    override fun isEnabled(position: Int): Boolean = false

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val activity = mActivityRef.get()

        if (activity == null) {
            return convertView ?: View(parent.context)
        }

        val enemyItemView = convertView ?: LayoutInflater.from(activity).inflate(R.layout.item_enemy, parent, false)

        val enemy = mEnemies[position]
        val enemyProperties: EnemyProperties = enemy.getEnemyProperties()

        val viewHolder = ViewHolder(enemyItemView)

        var tmp = activity.getString(enemy.getTextId())
        viewHolder.txtName.text = tmp

        val fmt = DecimalFormat()
        tmp = fmt.format(enemyProperties.getHealth())
        viewHolder.txtHealth.text = tmp

        val fmt2 = DecimalFormat("#0 '%'")
        tmp = fmt2.format(enemyProperties.getSpeed() * 100)
        viewHolder.txtSpeed.text = tmp

        tmp = fmt.format(enemyProperties.getReward())
        viewHolder.txtReward.text = tmp

        tmp = TextUtils.join("\n", enemyProperties.getWeakAgainst())
        viewHolder.txtWeakAgainst.text = if (tmp.isNotEmpty()) tmp else activity.getString(R.string.none)
        viewHolder.txtWeakAgainst.setTextColor(mTheme.getColor(R.attr.weakAgainstColor))

        tmp = TextUtils.join("\n", enemyProperties.getStrongAgainst())
        viewHolder.txtStrongAgainst.text = if (tmp.isNotEmpty()) tmp else activity.getString(R.string.none)
        viewHolder.txtStrongAgainst.setTextColor(mTheme.getColor(R.attr.strongAgainstColor))

        val bmp = createPreviewBitmap(enemy)
        viewHolder.imgEnemy.setImageBitmap(bmp)

        return enemyItemView
    }

    private fun createPreviewBitmap(enemy: Enemy): Bitmap {
        val viewport = Viewport()
        viewport.setGameSize(1, 1)
        viewport.setScreenSize(120, 120)

        val bitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.concat(viewport.getScreenMatrix())
        enemy.drawPreview(canvas)

        return bitmap
    }
}
