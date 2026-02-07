package com.tdfanta.game.engine.render.sprite

import android.graphics.Canvas
import com.tdfanta.game.util.math.Vector2

object SpriteTransformer {
    @JvmStatic
    fun translate(canvas: Canvas, position: Vector2) {
        translate(canvas, position.x(), position.y())
    }

    @JvmStatic
    fun translate(canvas: Canvas, x: Float, y: Float) {
        canvas.translate(x, y)
    }

    @JvmStatic
    fun rotate(canvas: Canvas, angle: Float) {
        canvas.rotate(angle)
    }

    @JvmStatic
    fun scale(canvas: Canvas, scale: Float) {
        canvas.scale(scale, scale)
    }
}
