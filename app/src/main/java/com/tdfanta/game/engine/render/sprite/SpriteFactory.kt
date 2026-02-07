package com.tdfanta.game.engine.render.sprite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.tdfanta.game.engine.theme.ThemeManager

class SpriteFactory(
    private val mContext: Context,
    private val mThemeManager: ThemeManager,
) {
    fun createTemplate(attrId: Int, spriteCount: Int): SpriteTemplate {
        val resourceId = mThemeManager.getTheme().getResourceId(attrId)

        val sheet = BitmapFactory.decodeResource(mContext.resources, resourceId)
        val spriteWidth = sheet.width / spriteCount
        val spriteHeight = sheet.height

        val sprites = Array(spriteCount) { index ->
            Bitmap.createBitmap(sheet, spriteWidth * index, 0, spriteWidth, spriteHeight)
        }

        return SpriteTemplate(*sprites)
    }

    fun createStatic(layer: Int, template: SpriteTemplate): StaticSprite =
        StaticSprite(layer, template)

    fun createAnimated(layer: Int, template: SpriteTemplate): AnimatedSprite =
        AnimatedSprite(layer, template)

    fun createReplication(original: SpriteInstance): ReplicatedSprite = ReplicatedSprite(original)
}
