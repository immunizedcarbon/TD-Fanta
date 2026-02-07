package com.tdfanta.game.engine.render

import android.graphics.Canvas

interface Drawable {
    fun getLayer(): Int

    fun draw(canvas: Canvas)
}
