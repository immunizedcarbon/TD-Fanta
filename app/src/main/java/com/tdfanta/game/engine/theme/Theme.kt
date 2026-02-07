package com.tdfanta.game.engine.theme

import android.content.Context
import com.tdfanta.game.R

class Theme(
    private val mContext: Context,
    private val mThemeNameId: Int,
    private val mThemeStyleId: Int,
) {
    fun getActivityThemeId(type: ActivityType): Int {
        val attrId = when (type) {
            ActivityType.Game -> R.attr.gameActivityStyle
            ActivityType.Popup -> R.attr.popupActivityStyle
            ActivityType.Normal -> R.attr.normalActivityStyle
        }

        return getResourceId(attrId)
    }

    fun getName(): String = mContext.resources.getString(mThemeNameId)

    fun getColor(attrId: Int): Int =
        mContext.obtainStyledAttributes(mThemeStyleId, intArrayOf(attrId)).use {
            it.getColor(0, 0)
        }

    fun getResourceId(attrId: Int): Int =
        mContext.obtainStyledAttributes(mThemeStyleId, intArrayOf(attrId)).use {
            it.getResourceId(0, 0)
        }
}

private inline fun <T> android.content.res.TypedArray.use(block: (android.content.res.TypedArray) -> T): T {
    return try {
        block(this)
    } finally {
        recycle()
    }
}
