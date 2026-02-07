package com.tdfanta.game.engine.render.sprite

import android.graphics.Canvas

fun interface SpriteTransformation {
    fun draw(sprite: SpriteInstance, canvas: Canvas)
}
